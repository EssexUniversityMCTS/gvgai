package tracks;

import core.competition.CompetitionParameters;
import core.game.Game;
import core.player.AbstractMultiPlayer;
import core.player.AbstractPlayer;
import core.player.Player;
import core.vgdl.VGDLFactory;
import core.vgdl.VGDLParser;
import core.vgdl.VGDLRegistry;
import core.vgdl.VGDLViewer;
import ontology.Types;

import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * Created by mballa on 04/04/2017.
 */
public class OnlineMachine{

    public static Game toPlay;
    public static VGDLViewer view; // TODO set reference to Game's view
    public static boolean wait = true;
    public static Dimension dimension;

    public static boolean VERBOSE = false;



    /**
     * Reads and launches a game for a human to be played. Graphics always on.
     *
     * @param game_file
     *            game description file.
     * @param level_file
     *            file with the level to be played.
     */
    public static double[] playOneGame(String game_file, String level_file, String actionFile, int randomSeed) {
        String agentName = "tracks.singlePlayer.tools.human.Agent";
        boolean visuals = true;
        return runOneGame(game_file, level_file, visuals, agentName, actionFile, randomSeed, 0);
    }


    /**
     * Reads and launches a game for a bot to be played. Graphics can be on or
     * off.
     *
     * @param game_file
     *            game description file.
     * @param level_file
     *            file with the level to be played.
     * @param visuals
     *            true to show the graphics, false otherwise.
     * @param agentNames
     *            names (inc. package) where the tracks are otherwise.
     *            Names separated by space.
     * @param actionFile
     *            filename of the files where the actions of these players, for
     *            this game, should be recorded.
     * @param randomSeed
     *            sampleRandom seed for the sampleRandom generator.
     * @param playerID
     *            ID of the human player
     */
    public static double[] runOneGame(String game_file, String level_file, boolean visuals, String agentNames,
                                      String actionFile, int randomSeed, int playerID) {
        VGDLFactory.GetInstance().init(); // This always first thing to do.
        VGDLRegistry.GetInstance().init();

        if (VERBOSE)
            System.out.println(" ** Playing game " + game_file + ", level " + level_file + " **");

        //if (CompetitionParameters.OS_WIN)
        //{
        //    System.out.println(" * WARNING: Time limitations based on WALL TIME on Windows * ");
        //}

        // First, we create the game to be played..
        toPlay = new VGDLParser().parseGame(game_file);
        toPlay.buildLevel(level_file, randomSeed);

        // Warm the game up.
        ArcadeMachine.warmUp(toPlay, CompetitionParameters.WARMUP_TIME);

        // Create the players.
        String[] names = agentNames.split(" ");
        int no_players = toPlay.no_players;
        if (no_players > 1 && no_players != names.length) {
            // We fill with more human players
            String[] newNames = new String[no_players];
            System.arraycopy(names, 0, newNames, 0, names.length);
            for (int i = names.length; i < no_players; ++i)
                newNames[i] = "tracks.multiPlayer.tools.human.Agent";
            names = newNames;
        }

        boolean humans[] = new boolean[no_players];
        boolean anyHuman = false;

        // System.out.println("Number of players: " + no_players);

        Player[] players;
        if (no_players > 1) {
            // multi player games
            players = new AbstractMultiPlayer[no_players];
        } else {
            // single player games
            players = new AbstractPlayer[no_players];
        }

        for (int i = 0; i < no_players; i++) {

            humans[i] = isHuman(names[i]);
            anyHuman |= humans[i];

            if (no_players > 1) {
                // multi player
                players[i] = ArcadeMachine.createMultiPlayer(names[i], actionFile, toPlay.getObservationMulti(),
                        randomSeed, i, humans[i]);
            } else {
                // single player
                players[i] = ArcadeMachine.createPlayer(names[i], actionFile, toPlay.getObservation(), randomSeed,
                        humans[i]);
            }

            if (players[i] == null) {
                // Something went wrong in the constructor, controller
                // disqualified
                if (no_players > 1) {
                    // multi player
                    toPlay.getAvatars()[i].disqualify(true);
                } else {
                    // single player
                    toPlay.disqualify();
                }

                // Get the score for the result.
                return toPlay.handleResult();
            }
        }

        // Then, play the game.
        double[] score;
        if (visuals) {
            dimension = toPlay.getScreenSize(); // TODO before starting the game can get the screen size here
            score = toPlay.playOnlineGame(players, randomSeed, anyHuman, playerID);
        }
        else
            score = toPlay.runGame(players, randomSeed);

        // Finally, when the game is over, we need to tear the players down.
        ArcadeMachine.tearPlayerDown(toPlay, players, actionFile, randomSeed, true);

        // This, the last thing to do in this method, always:
        toPlay.handleResult();
        toPlay.printResult();

        return toPlay.getFullResult();
    }

    /**
     * Runs a replay given a game, level and file with the actions to execute.
     *
     * @param game_file
     *            game description file.
     * @param level_file
     *            file with the level to be played.
     * @param visuals
     *            true to show the graphics, false otherwise.
     * @param actionFile
     *            name of the file where the actions of these players, for this
     *            game, must be read from. If the game is multi player, this
     *            file contains meta game information (winner, scores,
     *            timesteps, random seed) and names of all the files for player
     *            actions.
     *
     */
    public static double[] replayOnlineGame(String game_file, String level_file, boolean visuals, String actionFile) {
        VGDLFactory.GetInstance().init(); // This always first thing to do.
        VGDLRegistry.GetInstance().init();

        // First, we create the game to be played..
        toPlay = new VGDLParser().parseGame(game_file);
        toPlay.buildLevel(level_file, 0);

        String agentName;
        if (toPlay.getNoPlayers() > 1) {
            // multi player
            agentName = "tracks.multiPlayer.tools.replayer.Agent";
        } else {
            // single player
            agentName = "tracks.singlePlayer.tools.replayer.Agent";
        }

        // Second, create the player. Note: null as action_file and -1 as
        // sampleRandom seed
        // (we don't want to record anything from this execution).
        Player[] players;
        int no_players = toPlay.getNoPlayers();
        if (no_players > 1) {
            // multi player games
            players = new AbstractMultiPlayer[no_players];
        } else {
            // single player games
            players = new AbstractPlayer[no_players];
        }

        for (int i = 0; i < no_players; i++) {
            if (no_players > 1) {
                // multi player
                players[i] = ArcadeMachine.createMultiPlayer(agentName, null, toPlay.getObservationMulti(), -1, i,
                        false);
            } else {
                // single player
                players[i] = ArcadeMachine.createPlayer(agentName, null, toPlay.getObservation(), -1, false);
            }

            if (players[i] == null) {
                // Something went wrong in the constructor, controller
                // disqualified
                if (no_players > 1) {
                    // multi player
                    toPlay.getAvatars()[i].disqualify(true);
                } else {
                    // single player
                    toPlay.disqualify();
                }

                // Get the score for the result.
                double result[] = toPlay.handleResult();
                toPlay.printResult();
                return result;
            }
        }

        int seed = 0;
        int[] win = new int[no_players];
        double[] loggedScore = new double[no_players];
        int timesteps = 0;
        ArrayList<Types.ACTIONS> actions = new ArrayList<Types.ACTIONS>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(actionFile));

            // First line should be the sampleRandom seed, winner, score and
            // timesteps.
            if (no_players < 2) {
                // Single player file
                String[] firstLine = br.readLine().split(" ");
                seed = Integer.parseInt(firstLine[0]);
                win[0] = Integer.parseInt(firstLine[1]);
                loggedScore[0] = Double.parseDouble(firstLine[2]);
                timesteps = Integer.parseInt(firstLine[3]);

                System.out.println("Replaying game in " + game_file + ", " + level_file + " with seed " + seed
                        + " expecting player to win = " + (win[0] == 1) + "; score: " + loggedScore + "; timesteps: "
                        + timesteps);

                // The rest are the actions:
                String line = br.readLine();
                while (line != null) {
                    Types.ACTIONS nextAction = Types.ACTIONS.fromString(line);
                    actions.add(nextAction);

                    // next!
                    line = br.readLine();
                }

                // Assign the actions to the player. playerID used is 0, default
                // for single player games
                ((tracks.singlePlayer.tools.replayer.Agent) players[0]).setActions(actions);

            } else {
                // Multi player file

                // first line contains the sampleRandom seed and the timesteps.
                String[] firstLine = br.readLine().split(" ");
                seed = Integer.parseInt(firstLine[0]);
                timesteps = Integer.parseInt(firstLine[1]);

                // next line contain scores for all players, in order.
                String secondLine = br.readLine();
                String[] scores = secondLine.split(" ");
                for (int i = 0; i < no_players; i++) {
                    if (scores.length > i)
                        loggedScore[i] = Double.parseDouble(scores[i]);
                    else
                        loggedScore[i] = 0;
                }

                // next line contains win state for all players, in order.
                String thirdLine = br.readLine();
                String[] wins = thirdLine.split(" ");
                for (int i = 0; i < no_players; i++) {
                    if (wins.length > i)
                        win[i] = Integer.parseInt(wins[i]);
                    else
                        win[i] = 0;
                }

                // display information
                System.out.println("Replaying game in " + game_file + ", " + level_file + " with seed " + seed
                        + " expecting players' win states = " + thirdLine + "; scores: " + secondLine + "; timesteps: "
                        + timesteps);

                // next lines contain players actions, one line per game tick,
                // actions for players in order,
                // separated by spaces.
                ArrayList<ArrayList<Types.ACTIONS>> act = new ArrayList<>();
                for (int i = 0; i < no_players; i++) {
                    act.add(new ArrayList<Types.ACTIONS>());
                }
                String line = br.readLine();
                while (line != null) {
                    String[] acts = line.split(" ");
                    for (int i = 0; i < no_players; i++) {
                        Types.ACTIONS nextAction = acts.length > i ? Types.ACTIONS.fromString(acts[i])
                                : Types.ACTIONS.ACTION_NIL;
                        act.get(i).add(nextAction);
                    }
                    // next!
                    line = br.readLine();
                }

                // Assign the actions to the players.
                for (int i = 0; i < no_players; i++) {
                    ((tracks.multiPlayer.tools.replayer.Agent) players[i]).setActions(act.get(i));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        // Then, (re-)play the game.
        double[] score;
        if (visuals)
            score = toPlay.playOnlineGame(players, seed, false, 0);
        else
            score = toPlay.runGame(players, seed);

        // Finally, when the game is over, we need to tear the player down.
        // Actually in this case this might never do anything.
        ArcadeMachine.tearPlayerDown(toPlay, players, actionFile, seed, false);

        for (int i = 0; i < toPlay.getNoPlayers(); i++) {
            int actualWinner = (toPlay.getWinner(i) == Types.WINNER.PLAYER_WINS ? 1 : 0);
            if (actualWinner != win[i] || score[i] != loggedScore[i] || timesteps != toPlay.getGameTick())
                throw new RuntimeException("ERROR: Game Replay Failed.");
        }

        double result[] = toPlay.handleResult();
        toPlay.printResult();
        return result;
    }


    public static final boolean isHuman(String agentName) {
        if (agentName.equalsIgnoreCase("tracks.multiPlayer.tools.human.Agent")
                || agentName.equalsIgnoreCase("tracks.singlePlayer.tools.human.Agent"))
            return true;
        return false;
    }
}
