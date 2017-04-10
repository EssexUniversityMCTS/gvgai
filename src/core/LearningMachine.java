package core;

import core.competition.CompetitionParameters;
import core.game.*;
import core.player.*;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.StatSummary;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Daniel on 07.03.2017.
 */
public class LearningMachine {
    public static final boolean VERBOSE = false;


    /**
     * Reads and launches a game for a human to be played. Graphics always on.
     *
     * @param game_file  game description file.
     * @param level_file file with the level to be played.
     */
    public static double[] playOneGame(String game_file, String level_file, String actionFile, int randomSeed, boolean isTraining) throws IOException {
        String agentName = "controllers.human.Agent";
        boolean visuals = true;
        return runOneGame(game_file, level_file, visuals, agentName, actionFile, randomSeed, isTraining);
    }

    /**
     * Reads and launches a game for a bot to be played. Graphics can be on or off.
     *
     * @param game_file  game description file.
     * @param level_file file with the level to be played.
     * @param visuals    true to show the graphics, false otherwise. Training games have never graphics set to ON.
     * @param agentName  name (inc. package) where the controller is otherwise.
     * @param actionFile filename of the file where the actions of this player, for this game, should be recorded.
     * @param randomSeed sampleRandom seed for the sampleRandom generator.
     */
    public static double[] runOneGame(String game_file, String level_file, boolean visuals,
                                    String agentName, String actionFile, int randomSeed, boolean isTraining) throws IOException {
        int trainingPlays = 0;

        VGDLFactory.GetInstance().init(); //This always first thing to do.
        VGDLRegistry.GetInstance().init();

        System.out.println(" ** Playing game " + game_file + ", level " + level_file + " **");

        //Create the player.
        LearningPlayer player = LearningMachine.createPlayer(agentName);

//
        //2. Play the training games.
        System.out.print(trainingPlays + " ");
        double[] finalScore = playOnce(player, actionFile, game_file, level_file, visuals, randomSeed, isTraining);

        return finalScore;
    }

    private static double[] playOnce(LearningPlayer player, String actionFile, String game_file, String level_file,
                                   boolean visuals, int randomSeed, boolean isTraining) throws IOException {
        //Create the game.
        Game toPlay = new VGDLParser().parseGame(game_file);
        toPlay.buildLevel(level_file, randomSeed);

        //Init the player for the game.
        LearningMachine.initPlayer(player, actionFile, toPlay.getObservation(), randomSeed, isTraining);

        if (player == null) {
            //Something went wrong in the constructor, controller disqualified
            toPlay.disqualify();

            //Get the score for the result.
            return toPlay.handleResult();
        }

        //Then, play the game.
        double[] score;

        // TODO: Fix this little hack
        Player[] players = new Player[]{player};
        if (visuals)
            score = toPlay.playGame(players, randomSeed, false, 0);
        else
            score = toPlay.runGame(players, randomSeed);

        //Finally, when the game is over, we need to tear the player down.
        LearningMachine.tearPlayerDown(player, toPlay);

        return score;
    }

    /**
     * Tears the player down. This initiates the saving of actions to file.
     * It should be called when the game played is over.
     * @param toPlay game played.
     * @param players players to be closed.
     * @param actionFile file where players' actions should be saved.
     * @param randomSeed random seed of the game.
     * @param record boolean, true if actions should be recorded, false otherwise
     * @return false if there was a timeout from the players. true otherwise.
     */
    public static StatSummary performance;
    public static void runGames(String game_file, String[] level_files, int level_times,
                                LearningPlayer[] playerNames, String[] actionFiles)
    {
        VGDLFactory.GetInstance().init(); //This always first thing to do.
        VGDLRegistry.GetInstance().init();

        boolean recordActions = false;
        if(actionFiles != null)
        {
            recordActions = true;
            assert actionFiles.length >= level_files.length*level_times :
                    "runGames (actionFiles.length<level_files.length*level_times): " +
                            "you must supply an action file for each game instance to be played, or null.";
        }

        Game toPlay = new VGDLParser().parseGame(game_file);
        int levelIdx = 0;

        StatSummary[] victories = new StatSummary[toPlay.getNoPlayers()];
        StatSummary[] scores = new StatSummary[toPlay.getNoPlayers()];
        for (int i = 0; i < toPlay.getNoPlayers(); i++) {
            victories[i] = new StatSummary();
            scores[i] = new StatSummary();
        }
        performance = new StatSummary();

        for(String level_file : level_files)
        {
            for(int i = 0; i < level_times; ++i)
            {
                if(VERBOSE)
                    System.out.println(" ** Playing game " + game_file + ", level " + level_file + " ("+(i+1)+"/"+level_times+") **");

                //Determine the random seed, different for each game to be played.
                int randomSeed = new Random().nextInt();

                //build the level in the game.
                toPlay.buildLevel(level_file, randomSeed);

                String filename = recordActions ? actionFiles[levelIdx*level_times + i] : null;

                //Create the player.
                int no_players = playerNames.length;

                int disqCount = 0; //count how many players disqualified
                double[] score = new double[no_players]; //store scores for all the players

                LearningPlayer[] players;
                if (no_players > 1) {
                    //multi player games
                    players = new LearningPlayer[no_players];
                } else {
                    //single player games
                    players = new LearningPlayer[no_players];
                }

                for (int j = 0; j < no_players; j++) {
                    if (no_players > 1) {
                        //multi player
                        players[j] = LearningMachine.initMultiPlayer(playerNames[j], filename, toPlay.getObservationMulti(), randomSeed, j, false);
                    } else {
                        //single player
                        players[j] = LearningMachine.initPlayer(playerNames[j], actionFiles[0], toPlay.getObservation(), randomSeed, true);
                    }
                    score[j] = -1;
                    if (players[j] == null) {
                        //Something went wrong in the constructor, controller disqualified
                        //toPlay.disqualify(j);
                        toPlay.getAvatars()[j].disqualify(true);

                        disqCount++;
                    }
                }

                //Play the game if at least 2 players in multiplayer games or at least 1 in single player.
                //Get array of scores back.
                if ((no_players - disqCount) >= toPlay.no_players) {
                    score = toPlay.runGame(players, randomSeed);
                    //score = toPlay.playGame(players, randomSeed, false, 0);
                    toPlay.printResult();
                }
                else {
                    //Get the score for the result.
                    score = toPlay.handleResult();
                    toPlay.printResult();
                }

                //Finally, when the game is over, we need to tear the players down.
                try {
                    LearningMachine.tearMultiPlayerDown(players, toPlay);
                }catch (IOException e){
                    e.printStackTrace();
                }

                //Get players stats
                for (Player player : players)
                    if(player != null) {
                        int id = player.getPlayerID();
                        scores[id].add(score[id]);
                        victories[id].add(toPlay.getWinner(id) == Types.WINNER.PLAYER_WINS ? 1 : 0);
                    }

                //reset the game.
                toPlay.reset();
            }

            levelIdx++;
        }

        String vict = "", sc = "";
        for (int i = 0; i < toPlay.no_players; i++) {
            vict += victories[i].mean();
            sc += scores[i].mean();
            if (i != toPlay.no_players - 1) {
                vict += ", ";
                sc += ", ";
            }
        }
        System.out.println("Results in game " + game_file + ", " +
                vict + " , " + sc //);
                + ", " + performance.mean());
    }

    /**
     * Creates a player given its name. This method starts the process that runs this client.
     *
     * @param cmd name of the agent to create.
     * @return the player, created but NOT initialized, ready to start playing the game.
     */
    private static LearningPlayer createPlayer(String cmd) throws IOException {

        Process client;

        ProcessBuilder builder = new ProcessBuilder(cmd);
        builder.redirectErrorStream(true);
        client = builder.start();

        return new LearningPlayer(client);
    }


//
//    private static void printLines(String name, InputStream ins) {
//        String line = null;
//        BufferedReader in = new BufferedReader(
//                new InputStreamReader(ins));
//
//            while ((line = in.readLine()) != null) {
//                System.out.println(name + " " + line);
//            }
//
//    }

    /**
     * Inits the player for a given game.
     *
     * @param player     Player to init.
     * @param actionFile filename of the file where the actions of this player, for this game, should be recorded.
     * @param so         Initial state of the game to be played by the agent.
     * @param randomSeed Seed for the sampleRandom generator of the game to be played.
     * @return the player, created and initialized, ready to start playing the game.
     */
    private static LearningPlayer initPlayer(LearningPlayer player, String actionFile, StateObservation so,
                                             int randomSeed, boolean isTraining) {


        //Determine the time due for the controller initialization.
        ElapsedCpuTimer ect = new ElapsedCpuTimer(CompetitionParameters.TIMER_TYPE);
        ect.setMaxTimeMillis(CompetitionParameters.INITIALIZATION_TIME);

        //Initialize the controller.
        player.getServerComm().init(so, ect);

        //Check if we returned on time, and act in consequence.
        long timeTaken = ect.elapsedMillis();
        if (ect.exceededMaxTime()) {
            long exceeded = -ect.remainingTimeMillis();
            System.out.println("Controller initialization time out (" + exceeded + ").");

            return null;
        } else {
            //System.out.println("Controller initialization time: " + timeTaken + " ms.");
        }

        //If we have a player, set it up for action recording.
        if (player != null)
            player.setup(actionFile, randomSeed, false);


        return player;
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

    // TODO: 05/04/2017 Daniel: Finish this up somehow
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
        //Determine the time due for the controller initialization.
        ElapsedCpuTimer ect = new ElapsedCpuTimer(CompetitionParameters.TIMER_TYPE);
        ect.setMaxTimeMillis(CompetitionParameters.TEAR_DOWN_TIME);

        player.finishGame(toPlay.getObservation(), ect);

        player.teardown(toPlay);
        //player.close();
    }

    /**
     * Tears multiple players down. This initiates the saving of actions to file.
     * It should be called when the game played is over.
     *
     * @param players list of players to be closed.
     */
    private static boolean tearMultiPlayerDown(Player[] players, Game toPlay) throws IOException {
        for (Player p : players) {
            //Determine the time due for the controller close up.
            ElapsedCpuTimer ect = new ElapsedCpuTimer(CompetitionParameters.TIMER_TYPE);
            ect.setMaxTimeMillis(CompetitionParameters.TEAR_DOWN_TIME);

            //Inform about the result and the final game state.
            if (toPlay.no_players > 1)
                p.resultMulti(toPlay.getObservationMulti().copy(), ect);
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
