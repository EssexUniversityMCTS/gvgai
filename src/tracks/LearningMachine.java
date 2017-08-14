package tracks;

import core.competition.CompetitionParameters;
import core.game.Game;
import core.game.StateObservation;
import core.game.StateObservationMulti;
import core.player.LearningPlayer;
import core.player.Player;
import core.vgdl.VGDLFactory;
import core.vgdl.VGDLParser;
import core.vgdl.VGDLRegistry;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.StatSummary;

import java.io.IOException;
import java.util.Random;

/**
 * Created by Daniel on 07.03.2017.
 */
public class LearningMachine {
    public static final boolean VERBOSE = false;

    /**
     * Reads and launches a game for an agent to be played. Graphics can be on or off.
     *
     * @param game_file  game description file.
     * @param level_file file with the level to be played.
     * @param visuals    true to show the graphics, false otherwise. Training games have never graphics set to ON.
     * @param cmd  array with name of the script file to run for the client, plus agent and port
     * @param actionFile filename of the file where the actions of this player, for this game, should be recorded.
     * @param randomSeed sampleRandom seed for the sampleRandom generator.
     */
    public static double[] runOneGame(String game_file, String level_file, boolean visuals,
                                      String[] cmd, String actionFile, int randomSeed) throws IOException {
        VGDLFactory.GetInstance().init(); //This always first thing to do.
        VGDLRegistry.GetInstance().init();
        CompetitionParameters.IS_LEARNING = true;

        System.out.println(" ** Playing game " + game_file + ", level " + level_file + " **");

        //1. Create the player.
        LearningPlayer player = LearningMachine.createPlayer(cmd);
//
        //2. Play the training games.
        double[] finalScore = playOnce(player, actionFile, game_file, level_file, visuals, randomSeed);

        return finalScore;
    }

    /**
     * Reads and launches a game to be played on a series of both pre-determined and non
     * pre-determined levels.
     *
     * @param game_file  game description file.
     * @param level_files file with the level to be played.
     * @param cmd  array with name of the script file to run for the client, plus agent and port
     * @param actionFiles filename of the file where the actions of this player, for this game, should be recorded.
     */
    public static void runMultipleGames(String game_file, String[] level_files,
                                        String cmd[], String[] actionFiles, boolean visuals) throws IOException {
        VGDLFactory.GetInstance().init(); //This always first thing to do.
        VGDLRegistry.GetInstance().init();
        CompetitionParameters.IS_LEARNING = true;
        //Create the player.
        LearningPlayer player = LearningMachine.createPlayer(cmd);

        // Play the training games.
        runGames(game_file, level_files, 1, player, actionFiles, visuals);
    }

    /**
     * Play a given level of a given game once using a given player
     * @param player
     * @param actionFile
     * @param game_file
     * @param level_file
     * @param visuals
     * @param randomSeed
     * @return Score of players in the game (one player in a single player case)
     * @throws IOException
     */
    private static double[] playOnce(LearningPlayer player, String actionFile, String game_file, String level_file,
                                     boolean visuals, int randomSeed) throws IOException {
        //Create the game.
        Game toPlay = new VGDLParser().parseGame(game_file);
        toPlay.buildLevel(level_file, randomSeed);

        //Init the player for the game.
        if (player == null || LearningMachine.initPlayer(player, actionFile, randomSeed, false, toPlay.getObservation()) == null) {
            //Something went wrong in the constructor, controller disqualified
            toPlay.disqualify();
            //Get the score for the result.
            return toPlay.handleResult();
        }

        //Then, play the game.
        double[] score;

        Player[] players = new Player[]{player};
        if (visuals)
            score = toPlay.playGame(players, randomSeed, true, 0);
        else
            score = toPlay.runGame(players, randomSeed);

        //Finally, when the game is over, we need to tear the player down.
        LearningMachine.tearPlayerDown(player, toPlay);

        return score;
    }


    /**
     * Reads and launches a game for a bot to be played. It specifies which levels to play and how many times.
     * Filenames for saving actions can be specified. Graphics always on.
     * @param game_file game description file.
     * @param level_files array of level file names to play.
     * @param level_times how many times each level has to be played.
     * @param actionFiles names of the files where the actions of this player, for this game, should be recorded. Accepts
     *                    null if no recording is desired. If not null, this array must contain as much String objects as
     *                    level_files.length*level_times.
     */
    public static StatSummary performance;
    public static void runGames(String game_file, String[] level_files, int level_times,
                                LearningPlayer player, String[] actionFiles, boolean visual) throws IOException {
        VGDLFactory.GetInstance().init(); //This always first thing to do.
        VGDLRegistry.GetInstance().init();
        CompetitionParameters.IS_LEARNING = true;
        boolean recordActions = false;
        if (actionFiles != null) {
            recordActions = true;
            assert actionFiles.length >= level_files.length * level_times :
                "runGames (actionFiles.length<level_files.length*level_times): " +
                    "you must supply an action file for each game instance to be played, or null.";
        }

        Game toPlay = new VGDLParser().parseGame(game_file);
        int levelIdx = 0;

        StatSummary[] victories = new StatSummary[toPlay.getNoPlayers()];
        StatSummary[] scores = new StatSummary[toPlay.getNoPlayers()];
        victories[0] = new StatSummary();
        scores[0] = new StatSummary();
        performance = new StatSummary();

        // Player array to hold the single player
        LearningPlayer[] players = new LearningPlayer[]{player};

        // Initialize the player
        boolean initSuccesful = players[0].startPlayerCommunication();
        if (!initSuccesful) {
            return;
        }
        // Establish the training and validation levels.
        boolean keepPlaying = true;
        String[] trainingLevels = new String[Types.NUM_TRAINING_LEVELS];
        int level_idx = 0;
        for(; level_idx < trainingLevels.length; level_idx++)
            trainingLevels[level_idx] = level_files[level_idx];

        String[] validationLevels = new String[Types.NUM_LEARNING_LEVELS - Types.NUM_TRAINING_LEVELS];
        for(int i = 0; i < validationLevels.length; i++, level_idx++)
            validationLevels[i] = level_files[level_idx];

        level_idx = 0;
        int levelOutcome = 0;
        System.out.println("[PHASE] Starting First Phase of Training in " + Types.NUM_TRAINING_LEVELS + " levels.");
        while(keepPlaying && level_idx < trainingLevels.length)
        {
            String level_file = trainingLevels[level_idx];
            for (int i = 0; keepPlaying && i < level_times; ++i) {
                levelOutcome = playOneLevel(game_file,level_file,i,false, visual, recordActions,level_idx,
                    players,actionFiles,toPlay,scores,victories);
//                System.err.println("levelOutcome="+levelOutcome);
                keepPlaying = (levelOutcome>=0);
            }
            level_idx++;
        }

        if(levelOutcome == Types.LEARNING_RESULT_DISQ)
            return;

        if(levelOutcome != Types.LEARNING_FINISH_ROUND) {
            //We only continue playing if the round is not over.
            System.out.println("[PHASE] Starting Second Phase of Training in " + Types.NUM_TRAINING_LEVELS + " levels.");
            while (levelOutcome >= 0) {
                // Play the selected level once
                levelOutcome = playOneLevel(game_file, level_files[levelOutcome], 0, false, visual, recordActions,
                    levelOutcome, players, actionFiles, toPlay, scores, victories);
            }
        }

        if(levelOutcome == Types.LEARNING_RESULT_DISQ)
            return;

        // Validation time
        // Establish the level files for level 3 and 4
        System.out.println("[PHASE] Starting Validation in " + validationLevels.length + " levels.");

        for (int valid_idx=0; valid_idx<CompetitionParameters.validation_times; valid_idx++) {
            level_idx = 0;
            levelOutcome = 0;
            keepPlaying = true;
//        System.err.println("At beginning, keepPlaying="+keepPlaying + ",level_idx="+level_idx);
            while (keepPlaying && level_idx < validationLevels.length) {
                String validation_level = validationLevels[level_idx];
                for (int i = 0; keepPlaying && i < level_times; ++i) {
//                System.err.println("validation_level=" + validation_level);
                    levelOutcome = playOneLevel(game_file, validation_level, i, true, visual, recordActions, level_idx + Types.NUM_TRAINING_LEVELS, players, actionFiles, toPlay, scores, victories);
                    keepPlaying = (levelOutcome != Types.LEARNING_RESULT_DISQ);
//                System.err.println("levelOutcome=" + levelOutcome + ", keepPlaying="+keepPlaying);
                }
                level_idx++;
            }
        }
        System.out.println("[PHASE] End Validation in " + validationLevels.length + " levels.");
        String vict = "", sc = "";
        for (int i = 0; i < toPlay.no_players; i++) {
            vict += victories[i].mean();
            sc += scores[i].mean();
            if (i != toPlay.no_players - 1) {
                vict += ", ";
                sc += ", ";
            }
        }

//        System.out.println("[LOG] Results in game " + game_file + ", " +
//                vict + " , " + sc);

        //Finally, when the game is over, we need to finish the communication with the client.
        player.finishPlayerCommunication();
    }

    /**
     * Method used to play a single given level. It is also used to request player input in regards
     * to the next game to be played.
     *
     * @param game_file Game file to be used to play the game. Is sent by parent method.
     * @param level_file Level file to be used to play the game. Is sent by parent method.
     * @param level_time Integer denominating how many times the current level has been played in a row.
     *                   Is also sent from the exterior, and exists for debugging only.
     * @param isValidation Indicates if the level being played is a validation level
     * @param recordActions Boolean determining whether the actions should be recorded.
     * @param levelIdx Level index. Used for debugging.
     * @param players Array of Player-type objects. Used to play the game
     * @param actionFiles Files used to record the actions in for logging purposes.
     * @param toPlay The game to be played. Must be pre-initialized.
     * @param scores Array of scores to be modified. Is modified at the end of the level.
     * @param victories Array of victories to be modified. Is modified at the end of the level.
     * @return Next level to be played as chosen by the player, or a random substituent.
     * @throws IOException
     */
    public static int playOneLevel(String game_file, String level_file, int level_time, boolean isValidation, boolean isVisual, boolean recordActions,
                                   int levelIdx, LearningPlayer[] players, String[] actionFiles, Game toPlay, StatSummary[] scores,
                                   StatSummary[] victories) throws IOException{
        if (VERBOSE)
            System.out.println(" ** Playing game " + game_file + ", level " + level_file + " (" + level_time + ") **");

        // Create a new random seed for the next level.
        int randomSeed = new Random().nextInt();

        //build the level in the game.
        toPlay.buildLevel(level_file, randomSeed);

        String filename = recordActions ? actionFiles[levelIdx * level_time] : null; // TODO: 22/05/17 check this

        // Score array to hold handled results.
        double[] score;

        // Initialize the new learningPlayer instance.
        LearningPlayer learningPlayer = LearningMachine.initPlayer(players[0], actionFiles[0], randomSeed, isValidation, toPlay.getObservation());

        // If the player cannot be initialized, disqualify the controller
        if (learningPlayer == null) {
            System.out.println("Something went wrong in the constructor, controller disqualified");
            //Something went wrong in the constructor, controller disqualified
            toPlay.getAvatars()[0].disqualify(true);
            toPlay.handleResult();
            toPlay.printLearningResult(levelIdx, isValidation);
            return -1;
        }
        players[0] = learningPlayer;

        //Play the game
        //Get array of scores back.
        if(isVisual) {
            score = toPlay.playGame(players, randomSeed, false, 0);
        } else {
            score = toPlay.playOnlineGame(players, randomSeed, false, 0);
//            score = toPlay.runGame(players, randomSeed);
        }
        toPlay.printLearningResult(levelIdx, isValidation);

        //Finally, when the game is over, we need to tear the player down.
        LearningMachine.tearPlayerDown(players[0], toPlay);

        //Get player stats
        if (players[0] != null) {
            scores[0].add(score[0]);
            victories[0].add(toPlay.getWinner(0) == Types.WINNER.PLAYER_WINS ? 1 : 0);
        }

        // Send results to player and save their choice of next level to be played
        // First create a new observation
        StateObservation so = toPlay.getObservation();

        // Sends results to player and retrieve the next level to be played
        int level = players[0].result(so);
//        System.out.println("LearningMachine required level="+level);
        //reset the game.
        toPlay.reset();

        return level;
    }



    /**
     * Creates a player given its name. This method starts the process that runs this client.
     *
     * @param cmd name of the script to execute, with parameters (agent name and port).
     *            If cmd[0] is null, we (the server) is not starting the communication, the client is, via sockets.
     * @return the player, created but NOT initialized, ready to start playing the game.
     */
    private static LearningPlayer createPlayer(String[] cmd) throws IOException {
        String scriptName = cmd[0];

        if(scriptName != null) {
            Process client;
            ProcessBuilder builder;
            if (cmd.length == 5) {
                builder = new ProcessBuilder(cmd[0], cmd[1], cmd[2], cmd[3], cmd[4]);
            } else {
                builder = new ProcessBuilder(cmd[0], cmd[1], cmd[2]);
            }
            builder.redirectErrorStream(true);
            client = builder.start();
            return new LearningPlayer(client, cmd[2]);
        }else{
            assert (CompetitionParameters.USE_SOCKETS);
            return new LearningPlayer(null, cmd[2]);
        }

    }

    /**
     * Inits the player for a given game.
     *
     * @param player     Player to start.
     * @param actionFile filename of the file where the actions of this player, for this game, should be recorded.
     * @param randomSeed Seed for the sampleRandom generator of the game to be played.
     * @param isValidation true if playing a validation level.
     * @return the player, created and initialized, ready to start playing the game.
     */
    private static LearningPlayer initPlayer(LearningPlayer player, String actionFile, int randomSeed, boolean isValidation, StateObservation so) {
        //If we have a player, set it up for action recording.
        if (player != null)
            player.setup(actionFile, randomSeed, false);

        //Send Init message.
        if(player.init(so, isValidation))
            return player;

        return null;//Disqualified.
    }

    /**
     * Creates a player given its name with package for multiplayer. This class calls the constructor of the agent
     * and initializes the action recording procedure. PlayerID used is 0, default for single player games.
     * @param playerName name of the agent to create. It must be of the type "<agentPackage>.Agent".
     * @param actionFile filename of the file where the actions of this player, for this game, should be recorded.
     * @param so Initial state of the game to be played by the agent.
     * @param randomSeed Seed for the sampleRandom generator of the game to be played.
     * @param isHuman Indicates if the player is human
     * @return the player, created and initialized, ready to start playing the game.
     */
    // Not useful for singleLearning
    private static LearningPlayer initMultiPlayer(LearningPlayer playerName, String actionFile, StateObservationMulti so, int randomSeed, int id, boolean isHuman)
    {
        return playerName;
    }

    /**
     * Tears the player down. This initiates the saving of actions to file.
     * It should be called when the game played is over.
     *
     * @param player player to be closed.
     */
    private static void tearPlayerDown(LearningPlayer player, Game toPlay) throws IOException {
        player.teardown(toPlay);
    }

    /**
     * Tears multiple players down. This initiates the saving of actions to file.
     * It should be called when the game played is over.
     * Not useful for singleLearning
     * @param players list of players to be closed.
     */
    private static boolean tearMultiPlayerDown(Player[] players, Game toPlay) throws IOException {
        for (Player p : players) {
            //Determine the time due for the controller close up.
            ElapsedCpuTimer ect = new ElapsedCpuTimer();
            ect.setMaxTimeMillis(CompetitionParameters.TEAR_DOWN_TIME);

            //Inform about the result and the final game state.
            if (toPlay.no_players > 1)
                p.resultMulti(toPlay.getObservationMulti(p.getPlayerID()).copy(), ect);
            else
                p.result(toPlay.getObservation(), ect);

            //Check if we returned on time, and act in consequence.
            long timeTaken = ect.elapsedMillis();
            if (ect.exceededMaxTime()) {
                long exceeded = -ect.remainingTimeMillis();
                System.out.println("Controller tear down time out (" + exceeded + ").");

                toPlay.disqualify(p.getPlayerID());
                return false;
            }

            if (VERBOSE)
                System.out.println("Controller tear down time: " + timeTaken + " ms.");
            return true;
        }

        return true;
    }


}