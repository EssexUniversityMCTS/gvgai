package tracks.levelGeneration;

import tracks.ArcadeMachine;
import core.vgdl.VGDLFactory;
import core.vgdl.VGDLParser;
import core.vgdl.VGDLRegistry;
import core.competition.CompetitionParameters;
import core.game.Game;
import core.game.GameDescription;
import core.generator.AbstractLevelGenerator;
import core.player.AbstractPlayer;
import tools.ElapsedCpuTimer;
import tools.IO;
import tools.StatSummary;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by dperez on 19/03/2017.
 */
public class LevelGenMachine
{

    /**
     * Generate a level for a certain described game and test it against a
     * supplied agent
     *
     * @param gameFile game description file.
     * @param levelGenerator level generator class path.
     * @param levelFile file to save the generated level in it
     */
    public static boolean generateOneLevel(String gameFile, String levelGenerator, String levelFile) {
        VGDLFactory.GetInstance().init(); // This always first thing to do.
        VGDLRegistry.GetInstance().init();

        System.out.println(
                " ** Generating a level for " + gameFile + ", using level generator " + levelGenerator + " **");

        // First, we create the game to be played..
        Game toPlay = new VGDLParser().parseGame(gameFile);
        GameDescription description = new GameDescription(toPlay);
        AbstractLevelGenerator generator = createLevelGenerator(levelGenerator, description);
        String level = getGeneratedLevel(description, toPlay, generator);
        if (level == "" || level == null) {
            System.out.println("Empty Level Disqualified");
            toPlay.disqualify();

            // Get the score for the result.
            toPlay.handleResult();
            toPlay.printResult();
            return false;
        }

        HashMap<Character, ArrayList<String>> charMapping = generator.getLevelMapping();
        if (charMapping != null) {
            toPlay.setCharMapping(charMapping);
        }

        try {
            toPlay.buildStringLevel(level.split("\n"), 0);
        } catch (Exception e) {
            System.out.println("Undefined symbols or wrong number of avatars Disqualified ");
            toPlay.disqualify();

            // Get the score for the result.
            toPlay.handleResult();
            toPlay.printResult();
            return false;
        }

        if (levelFile != null) {
            saveLevel(level, levelFile, toPlay.getCharMapping());
        }

        return true;
    }


    /**
     * Generate multiple levels for a certain game
     * @param gameFile The game description file path
     * @param levelGenerator The current used level generator
     * @param levelFile array of level files to save the generated levels
     */
    public static void generateLevels(String gameFile, String levelGenerator, String[] levelFile) {
        VGDLFactory.GetInstance().init(); // This always first thing to do.
        VGDLRegistry.GetInstance().init();

        // First, we create the game to be played..
        Game toPlay = new VGDLParser().parseGame(gameFile);
        GameDescription description = new GameDescription(toPlay);
        AbstractLevelGenerator generator = createLevelGenerator(levelGenerator, description);
        HashMap<Character, ArrayList<String>> originalMapping = toPlay.getCharMapping();

        for (int i = 0; i < levelFile.length; i++) {
            System.out.println(" ** Generating a level " + (i + 1) + " for " + gameFile + ", using level generator "
                    + levelGenerator + " **");
            toPlay.reset();
            description.reset(toPlay);

            String level = getGeneratedLevel(description, toPlay, generator);
            if (level == "" || level == null) {
                toPlay.disqualify();

                // Get the score for the result.
                toPlay.handleResult();
                toPlay.printResult();
            }

            HashMap<Character, ArrayList<String>> charMapping = generator.getLevelMapping();
            if (charMapping != null) {
                toPlay.setCharMapping(charMapping);
            }
            try {
                toPlay.buildStringLevel(level.split("\n"), 0);
            } catch (Exception e) {
                System.out.println("Undefined symbols or wrong number of avatars Disqualified ");
                toPlay.disqualify();

                // Get the score for the result.
                toPlay.handleResult();
                toPlay.printResult();
            }
            if (levelFile != null) {
                saveLevel(level, levelFile[i], toPlay.getCharMapping());
            }
            toPlay.setCharMapping(originalMapping);
        }
    }


    /**
     * Reads game description then generate level using the supplied generator.
     * It also launches the game for a human to be played. Graphics always on.
     *
     * @param gameFile
     *            the game description file
     * @param actionFile
     *            the action file name
     * @param levelFile
     *            a file to save the generated level
     */
    public static double playOneGeneratedLevel(String gameFile, String actionFile, String levelFile, int randomSeed) {
        String agentName = "tracks.singlePlayer.tools.human.Agent";
        boolean visuals = true;
        return runOneGeneratedLevel(gameFile, visuals, agentName, actionFile, levelFile, randomSeed, true);
    }


    /**
     * A player (human or bot) plays a generated level, which is passed by
     * parameter, in a determined game.
     *
     * @param gameFile game description file.
     * @param visuals true to show the graphics, false otherwise.
     * @param agentName name (inc. package) where the controller is otherwise.
     * @param actionFile  filename of the file where the actions of this player, for this game, should be recorded.
     * @param levelFile level file to play in
     * @param randomSeed random seed for the game to be played
     * @param isHuman indicates if the game is played by a human or a bot
     * @return score of the game plaayed
     */
    public static double runOneGeneratedLevel(String gameFile, boolean visuals, String agentName, String actionFile,
                                              String levelFile, int randomSeed, boolean isHuman) {
        VGDLFactory.GetInstance().init(); // This always first thing to do.
        VGDLRegistry.GetInstance().init();

        System.out.println(" ** Playing game " + gameFile + ", using generate level file " + levelFile + " **");

        // First, we create the game to be played..
        Game toPlay = new VGDLParser().parseGame(gameFile);
        String level = loadGeneratedFile(toPlay, levelFile);
        String[] levelLines = level.split("\n");

        toPlay.reset();
        toPlay.buildStringLevel(levelLines, 0);

        // Warm the game up.
        ArcadeMachine.warmUp(toPlay, CompetitionParameters.WARMUP_TIME);

        // Create the player.
        AbstractPlayer player = ArcadeMachine.createPlayer(agentName, actionFile, toPlay.getObservation(), randomSeed,
                isHuman);

        if (player == null) {
            // Something went wrong in the constructor, controller disqualified
            toPlay.disqualify();

            // Get the score for the result.
            double result = toPlay.handleResult()[0];
            toPlay.printResult();
            return result;
        }

        // Then, play the game.
        double score = 0.0;

        /**
         * playGame and runGame methods from the Game class take an array of
         * players as argument, including all players in the game. As this
         * method refers to single player games, an array is created containing
         * only one element: the player created earlier. To get back just 1
         * score for the player, the first element in the score array is
         * returned.
         */
        AbstractPlayer[] p = new AbstractPlayer[1];
        p[0] = player;

        if (visuals)
            score = toPlay.playGame(p, randomSeed, isHuman, 0)[0];
        else
            score = toPlay.runGame(p, randomSeed)[0];

        // Finally, when the game is over, we need to tear the player down.
        ArcadeMachine.tearPlayerDown(toPlay, p, actionFile, randomSeed, true);

        double result = toPlay.handleResult()[0];
        toPlay.printResult();
        return result;
    }


    /**
     * play a couple of generated levels for a certain game
     * @param gameFile The game description file path
     * @param actionFile  array of files to save the actions in
     * @param levelFile  array of level files to save the generated levels
     * @param isHuman indicates if the level will be played by a human or a bot.
     */
    public static void playGeneratedLevels(String gameFile, String[] actionFile, String[] levelFile, boolean isHuman) {
        String agentName = "tracks.singlePlayer.tools.human.Agent";

        VGDLFactory.GetInstance().init(); // This always first thing to do.
        VGDLRegistry.GetInstance().init();

        boolean recordActions = false;
        if (actionFile != null) {
            recordActions = true;
            assert actionFile.length >= levelFile.length : "runGames (actionFiles.length<level_files.length*level_times): "
                    + "you must supply an action file for each game instance to be played, or null.";
        }

        StatSummary scores = new StatSummary();

        Game toPlay = new VGDLParser().parseGame(gameFile);
        int levelIdx = 0;
        for (String file : levelFile) {
            System.out.println(" ** Playing game " + gameFile + ", level " + file + " **");

            // build the level in the game.
            String level = loadGeneratedFile(toPlay, file);
            String[] levelLines = level.split("\n");

            // Determine the random seed, different for each game to be played.
            int randomSeed = new Random().nextInt();

            toPlay.buildStringLevel(levelLines, randomSeed);

            String filename = recordActions ? actionFile[levelIdx] : null;

            // Warm the game up.
            ArcadeMachine.warmUp(toPlay, CompetitionParameters.WARMUP_TIME);

            // Create the player.
            AbstractPlayer player = ArcadeMachine.createPlayer(agentName, filename, toPlay.getObservation(), randomSeed,
                    isHuman);

            // Add player to player array.
            AbstractPlayer[] p = new AbstractPlayer[1];
            p[0] = player;

            double score = -1;
            if (player == null) {
                // Something went wrong in the constructor, controller
                // disqualified
                toPlay.disqualify();

                // Get the score for the result. PlayerID used 0, default in
                // single player games.
                score = toPlay.handleResult()[0];
                toPlay.printResult();

            } else {

                // Then, play the game.

                /**
                 * playGame method from Game class takes an array of players as
                 * argument, including all players in the game. As this method
                 * refers to single player games, an array is created containing
                 * only one element: the player created earlier. To get back
                 * just 1 score for the player, the first element in the score
                 * array is returned.
                 */
                score = toPlay.playGame(p, randomSeed, isHuman, 0)[0];
            }

            scores.add(score);

            // Finally, when the game is over, we need to tear the player down.
            if (player != null)
                ArcadeMachine.tearPlayerDown(toPlay, p, filename, randomSeed, true);

            // reset the game.
            toPlay.reset();

            levelIdx += 1;
        }

        System.out.println(" *** Results in game " + gameFile + " *** ");
        System.out.println(scores);
        System.out.println(" *********");
    }



    /// PRIVATE METHODS:

    /**
     * Generate AbstractLevelGenerator object to generate levels for the game
     * using the supplied class path.
     * @param levelGenerator class path for the supplied level generator
     * @param gd abstract object describes the game
     * @return AbstractLevelGenerator object.
     */
    protected static AbstractLevelGenerator createLevelGenerator(String levelGenerator, GameDescription gd)
            throws RuntimeException {
        AbstractLevelGenerator generator = null;
        try {
            // Get the class and the constructor with arguments
            // (StateObservation, long).
            Class<? extends AbstractLevelGenerator> controllerClass = Class.forName(levelGenerator)
                    .asSubclass(AbstractLevelGenerator.class);
            Class[] gameArgClass = new Class[] { GameDescription.class, ElapsedCpuTimer.class };
            Constructor controllerArgsConstructor = controllerClass.getConstructor(gameArgClass);

            // Determine the time due for the controller creation.
            ElapsedCpuTimer ect = new ElapsedCpuTimer();
            ect.setMaxTimeMillis(CompetitionParameters.LEVEL_INITIALIZATION_TIME);

            // Call the constructor with the appropriate parameters.
            Object[] constructorArgs = new Object[] { gd, ect.copy() };
            generator = (AbstractLevelGenerator) controllerArgsConstructor.newInstance(constructorArgs);

            // Check if we returned on time, and act in consequence.
            long timeTaken = ect.elapsedMillis();
            if (ect.exceededMaxTime()) {
                long exceeded = -ect.remainingTimeMillis();
                System.out.println("Generator initialization time out (" + exceeded + ").");

                return null;
            } else {
                System.out.println("Generator initialization time: " + timeTaken + " ms.");
            }

            // This code can throw many exceptions (no time related):

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            System.err.println(
                    "Constructor " + levelGenerator + "(StateObservation,long) not found in controller class:");
            System.exit(1);

        } catch (ClassNotFoundException e) {
            System.err.println("Class " + levelGenerator + " not found for the controller:");
            e.printStackTrace();
            System.exit(1);

        } catch (InstantiationException e) {
            System.err.println("Exception instantiating " + levelGenerator + ":");
            e.printStackTrace();
            System.exit(1);

        } catch (IllegalAccessException e) {
            System.err.println("Illegal access exception when instantiating " + levelGenerator + ":");
            e.printStackTrace();
            System.exit(1);
        } catch (InvocationTargetException e) {
            System.err.println("Exception calling the constructor " + levelGenerator + "(StateObservation,long):");
            e.printStackTrace();
            System.exit(1);
        }

        return generator;
    }


    /**
     * Generate a level for the described game using the supplied level
     * generator.
     *
     * @param gd Abstract description of game elements
     * @param game Current game object.
     * @param generator Current level generator.
     * @return String of symbols contains the generated level. Same as Level Description File string.
     */
    private static String getGeneratedLevel(GameDescription gd, Game game, AbstractLevelGenerator generator) {
        ElapsedCpuTimer ect = new ElapsedCpuTimer();
        ect.setMaxTimeMillis(CompetitionParameters.LEVEL_ACTION_TIME);

        String level = generator.generateLevel(gd, ect.copy());

        if (ect.exceededMaxTime()) {
            long exceeded = -ect.remainingTimeMillis();

            if (ect.elapsedMillis() > CompetitionParameters.LEVEL_ACTION_TIME_DISQ) {
                // The agent took too long to replay. The game is over and the
                // agent is disqualified
                System.out.println("Too long: " + "(exceeding " + (exceeded) + "ms): controller disqualified.");
                level = "";
            } else {
                System.out.println("Overspent: " + "(exceeding " + (exceeded) + "ms): applying Empty Level.");
                level = " ";
            }
        }

        return level;
    }


    /**
     * Saves a level string to a file
     * @param level current level to save
     * @param levelFile saved file
     */
    private static void saveLevel(String level, String levelFile, HashMap<Character, ArrayList<String>> charMapping) {
        try {
            if (levelFile != null) {
                BufferedWriter writer = new BufferedWriter(new FileWriter(levelFile));
                writer.write("LevelMapping");
                writer.newLine();
                for (Map.Entry<Character, ArrayList<String>> e : charMapping.entrySet()) {
                    writer.write("    " + e.getKey() + " > ");
                    for (String s : e.getValue()) {
                        writer.write(s + " ");
                    }
                    writer.newLine();
                }
                writer.newLine();
                writer.write("LevelDescription");
                writer.newLine();
                writer.write(level);
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load a generated level file.
     * @param currentGame Current Game object to se the Level Mapping
     * @param levelFile The generated level file path
     * @return Level String to be loaded
     */
    protected static String loadGeneratedFile(Game currentGame, String levelFile) {
        HashMap<Character, ArrayList<String>> levelMapping = new HashMap<Character, ArrayList<String>>();
        String level = "";
        int mode = 0;
        String[] lines = new IO().readFile(levelFile);
        for (String line : lines) {
            if (line.equals("LevelMapping")) {
                mode = 0;
            } else if (line.equals("LevelDescription")) {
                mode = 1;
            } else {
                switch (mode) {
                    case 0:
                        if (line.trim().length() == 0) {
                            continue;
                        }
                        String[] sides = line.split(">");
                        ArrayList<String> sprites = new ArrayList<String>();
                        for (String sprite : sides[1].trim().split(" ")) {
                            if (sprite.trim().length() == 0) {
                                continue;
                            } else {
                                sprites.add(sprite.trim());
                            }
                        }
                        levelMapping.put(sides[0].trim().charAt(0), sprites);
                        break;
                    case 1:
                        level += line + "\n";
                        break;
                }
            }
        }
        currentGame.setCharMapping(levelMapping);
        return level;
    }


}
