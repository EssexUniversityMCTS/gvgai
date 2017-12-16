package tracks;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Random;

import core.vgdl.VGDLFactory;
import core.vgdl.VGDLParser;
import core.vgdl.VGDLRegistry;
import core.competition.CompetitionParameters;
import core.game.Game;
import core.game.StateObservation;
import core.game.StateObservationMulti;
import core.player.AbstractMultiPlayer;
import core.player.AbstractPlayer;
import core.player.Player;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.StatSummary;

/**
 * Created with IntelliJ IDEA. User: Diego Date: 06/11/13 Time: 11:24 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class ArcadeMachine {
    public static final boolean VERBOSE = false;

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
     * Reads and launches a game for a human to be played. Graphics always on.
     * 
     * @param game_file
     *            game description file.
     * @param level_file
     *            file with the level to be played.
     * @param actionFile
     *            to save the actions of the game.
     * @param randomSeed
     *            for the game to be played.
     */
    public static double[] playOneGameMulti(String game_file, String level_file, String actionFile, int randomSeed) {
		String agentName = "tracks.multiPlayer.tools.human.Agent";
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

		if (CompetitionParameters.OS_WIN)
		{
			System.out.println(" * WARNING: Time limitations based on WALL TIME on Windows * ");
		}

		// First, we create the game to be played..
		Game toPlay = new VGDLParser().parseGame(game_file);
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
			players[i] = ArcadeMachine.createMultiPlayer(names[i], actionFile, toPlay.getObservationMulti(i),
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
			toPlay.handleResult();
			toPlay.printResult();
			return toPlay.getFullResult();
			}
		}

		// Then, play the game.
		double[] score;
		if (visuals)
			score = toPlay.playGame(players, randomSeed, anyHuman, playerID);
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
    public static double[] replayGame(String game_file, String level_file, boolean visuals, String actionFile) {
		VGDLFactory.GetInstance().init(); // This always first thing to do.
		VGDLRegistry.GetInstance().init();

		// First, we create the game to be played..
		Game toPlay = new VGDLParser().parseGame(game_file);
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
			players[i] = ArcadeMachine.createMultiPlayer(agentName, null, toPlay.getObservationMulti(i), -1, i,
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
			score = toPlay.playGame(players, seed, false, 0);
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

	public static StatSummary performance;


    /**
     * Reads and launches a game for a bot to be played. It specifies which
     * levels to play and how many times. Filenames for saving actions can be
     * specified. Graphics always off.
     * 
     * @param game_file   game description file.
     * @param level_files  array of level file names to play.
     * @param level_times   how many times each level has to be played.
     * @param actionFiles names of the files where the actions of this player, for this
     *   game, should be recorded. Accepts null if no recording is desired. If not null,
     *   this array must contain as much String objects as level_files.length*level_times.
     */
    public static void runGames(String game_file, String[] level_files, int level_times, String agentName, String[] actionFiles) {
	VGDLFactory.GetInstance().init(); // This always first thing to do.
	VGDLRegistry.GetInstance().init();

	boolean recordActions = false;
	if (actionFiles != null) {
	    recordActions = true;
	    assert actionFiles.length >= level_files.length
		    * level_times : "runGames (actionFiles.length<level_files.length*level_times): "
			    + "you must supply an action file for each game instance to be played, or null.";
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

	for (String level_file : level_files) {
	    for (int i = 0; i < level_times; ++i) {
		if (VERBOSE)
		    System.out.println(" ** Playing game " + game_file + ", level " + level_file + " (" + (i + 1) + "/"
			    + level_times + ") **");

		// Determine the random seed, different for each game to be
		// played.
		int randomSeed = new Random().nextInt();

		// build the level in the game.
		toPlay.buildLevel(level_file, randomSeed);

		String filename = recordActions ? actionFiles[levelIdx * level_times + i] : null;

		// Warm the game up.
		ArcadeMachine.warmUp(toPlay, CompetitionParameters.WARMUP_TIME);

		// Create the player.
		String[] agentNames = agentName.split(" ");
		int no_players = agentNames.length;

		int disqCount = 0; // count how many players disqualified
		double[] score = new double[no_players]; // store scores for all
							 // the players

		Player[] players;
		if (no_players > 1) {
		    // multi player games
		    players = new AbstractMultiPlayer[no_players];
		} else {
		    // single player games
		    players = new AbstractPlayer[no_players];
		}

		for (int j = 0; j < no_players; j++) {
		    if (no_players > 1) {
			// multi player
			players[j] = ArcadeMachine.createMultiPlayer(agentNames[j], filename,
				toPlay.getObservationMulti(i), randomSeed, j, false);
		    } else {
			// single player
			players[j] = ArcadeMachine.createPlayer(agentNames[j], filename, toPlay.getObservation(),
				randomSeed, false);
		    }
		    score[j] = -1;
		    if (players[j] == null) {
				// Something went wrong in the constructor, controller
				// disqualified
				// toPlay.disqualify(j);
				toPlay.getAvatars()[j].disqualify(true);

				disqCount++;
		    }
		}

		// Play the game if at least 2 players in multiplayer games or
		// at least 1 in single player.
		// Get array of scores back.
		if ((no_players - disqCount) >= toPlay.no_players) {
		    score = toPlay.runGame(players, randomSeed);
		    //score = toPlay.playGame(players, randomSeed, false, 0);
		    toPlay.printResult();
		} else {
		    // Get the score for the result.
		    score = toPlay.handleResult();
		    toPlay.printResult();
		}

		// Finally, when the game is over, we need to tear the players
		// down.
		if (!ArcadeMachine.tearPlayerDown(toPlay, players, filename, randomSeed, true)) {
		    score = toPlay.handleResult();
		    toPlay.printResult();
		}

		// Get players stats
		for (Player player : players)
		    if (player != null) {
			int id = player.getPlayerID();
			scores[id].add(score[id]);
			victories[id].add(toPlay.getWinner(id) == Types.WINNER.PLAYER_WINS ? 1 : 0);
		    }

		// reset the game.
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
	System.out.println("Results in game " + game_file + ", " + vict + " , " + sc);
	 	//+ " , " + performance.mean());
    }

    /**
     * Creates a player given its name with package. This class calls the
     * constructor of the agent and initializes the action recording procedure.
     * PlayerID used is 0, default for single player games.
     * 
     * @param playerName
     *            name of the agent to create. It must be of the type
     *            "<agentPackage>.Agent".
     * @param actionFile
     *            filename of the file where the actions of this player, for
     *            this game, should be recorded.
     * @param so
     *            Initial state of the game to be played by the agent.
     * @param randomSeed
     *            Seed for the sampleRandom generator of the game to be played.
     * @param isHuman
     *            Indicates if the player is human
     * @return the player, created and initialized, ready to start playing the
     *         game.
     */
    public static AbstractPlayer createPlayer(String playerName, String actionFile, StateObservation so,
	    int randomSeed, boolean isHuman) {
        AbstractPlayer player = null;

        try {
            // create the controller.
            player = (AbstractPlayer) createController(playerName, 0, so);
            if (player != null)
            player.setup(actionFile, randomSeed, isHuman);
            // else System.out.println("No controller created.");

        } catch (Exception e) {
            // This probably happens because controller took too much time to be
            // created.
            e.printStackTrace();
            System.exit(1);
        }

        // System.out.println("Created player.");

        return player;
    }

    /**
     * Creates a player given its name with package for multiplayer. This class
     * calls the constructor of the agent and initializes the action recording
     * procedure. PlayerID used is 0, default for single player games.
     * 
     * @param playerName
     *            name of the agent to create. It must be of the type
     *            "<agentPackage>.Agent".
     * @param actionFile
     *            filename of the file where the actions of this player, for
     *            this game, should be recorded.
     * @param so
     *            Initial state of the game to be played by the agent.
     * @param randomSeed
     *            Seed for the sampleRandom generator of the game to be played.
     * @param isHuman
     *            Indicates if the player is human
     * @return the player, created and initialized, ready to start playing the
     *         game.
     */
    public static AbstractMultiPlayer createMultiPlayer(String playerName, String actionFile, StateObservationMulti so,
	    int randomSeed, int id, boolean isHuman) {
        AbstractMultiPlayer player = null;

        try {
            // create the controller.
            player = (AbstractMultiPlayer) createController(playerName, id, so);
            if (player != null) {
            player.setup(actionFile, randomSeed, isHuman);
            }

        } catch (Exception e) {
            // This probably happens because controller took too much time to be
            // created.
            e.printStackTrace();
            System.exit(1);
        }

        return player;
    }

    /**
     * Creates and initializes a new controller with the given name. Takes into
     * account the initialization time, calling the appropriate constructor with
     * the state observation and time due parameters.
     * 
     * @param playerName
     *            Name of the controller to instantiate.
     * @param so
     *            Initial state of the game to be played by the agent.
     * @return the player if it could be created, null otherwise.
     */

    protected static Player createController(String playerName, int playerID, StateObservation so)
	    throws RuntimeException {
        Player player = null;
        try {

            // Determine the time due for the controller creation.
            ElapsedCpuTimer ect = new ElapsedCpuTimer();
            ect.setMaxTimeMillis(CompetitionParameters.INITIALIZATION_TIME);

            if (so.getNoPlayers() < 2) { // single player
				// Get the class and the constructor with arguments
				// (StateObservation, long).
				Class<? extends AbstractPlayer> controllerClass = Class.forName(playerName)
					.asSubclass(AbstractPlayer.class);
				Class[] gameArgClass = new Class[] { StateObservation.class, ElapsedCpuTimer.class };
				Constructor controllerArgsConstructor = controllerClass.getConstructor(gameArgClass);

				// Call the constructor with the appropriate parameters.
				Object[] constructorArgs = new Object[] { so, ect.copy() };

				player = (AbstractPlayer) controllerArgsConstructor.newInstance(constructorArgs);
				player.setPlayerID(playerID);

            } else { // multi player
				// Get the class and the constructor with arguments
				// (StateObservation, long, int).
				Class<? extends AbstractMultiPlayer> controllerClass = Class.forName(playerName)
					.asSubclass(AbstractMultiPlayer.class);
				Class[] gameArgClass = new Class[] { StateObservationMulti.class, ElapsedCpuTimer.class, int.class };
				Constructor controllerArgsConstructor = controllerClass.getConstructor(gameArgClass);

				// Call the constructor with the appropriate parameters.
				Object[] constructorArgs = new Object[] { (StateObservationMulti) so.copy(), ect.copy(), playerID };

				player = (AbstractMultiPlayer) controllerArgsConstructor.newInstance(constructorArgs);
				player.setPlayerID(playerID);
            }

            // Check if we returned on time, and act in consequence.
            long timeTaken = ect.elapsedMillis();
            if (CompetitionParameters.TIME_CONSTRAINED && ect.exceededMaxTime()) {
				long exceeded = -ect.remainingTimeMillis();
				System.out.println("Controller initialization time out (" + exceeded + ").");

				return null;
            } else {
				if (VERBOSE)
					System.out.println("Controller initialization time: " + timeTaken + " ms.");
            }

            // This code can throw many exceptions (no time related):

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            System.err.println("Constructor " + playerName + "(StateObservation,long) not found in controller class:");
            System.exit(1);

        } catch (ClassNotFoundException e) {
            System.err.println("Class " + playerName + " not found for the controller:");
            e.printStackTrace();
            System.exit(1);

        } catch (InstantiationException e) {
            System.err.println("Exception instantiating " + playerName + ":");
            e.printStackTrace();
            System.exit(1);

        } catch (IllegalAccessException e) {
            System.err.println("Illegal access exception when instantiating " + playerName + ":");
            e.printStackTrace();
            System.exit(1);
        } catch (InvocationTargetException e) {
            System.err.println("Exception calling the constructor " + playerName + "(StateObservation,long):");
            e.printStackTrace();
            System.exit(1);
        }

        // System.out.println("Controller created. " + player.getPlayerID());

        return player;
    }



    /**
     * This methods takes the game and warms it up. This allows Java to finish
     * the runtime compilation process and optimize the code before the proper
     * game starts.
     * 
     * @param toPlay
     *            game to be warmed up.
     * @param howLong
     *            for how long the warming up process must last (in
     *            milliseconds).
     */
		@SuppressWarnings("unchecked")
    public static void warmUp(Game toPlay, long howLong) {
        ElapsedCpuTimer ect = new ElapsedCpuTimer();
        ect.setMaxTimeMillis(howLong);
        int playoutLength = 10;
        int copyStats = 0;
        int advStats = 0;
        int no_players = toPlay.no_players;

        StatSummary ss1 = new StatSummary();
        StatSummary ss2 = new StatSummary();

        boolean finish = ect.exceededMaxTime()
            || (copyStats > CompetitionParameters.WARMUP_CP && advStats > CompetitionParameters.WARMUP_ADV);

        ArrayList<Types.ACTIONS>[] actions = new ArrayList[no_players];
        StateObservation stateObs;

        if (no_players > 1) {
            // multi player
            stateObs = toPlay.getObservationMulti(0);
            for (int i = 0; i < no_players; i++) {
            actions[i] = ((StateObservationMulti) stateObs).getAvailableActions(i);
            }
        } else {
            // single player
            stateObs = toPlay.getObservation();
            actions[0] = stateObs.getAvailableActions();
        }

        // while(!ect.exceededMaxTime())
        while (!finish) {
            for (int i = 0; i < no_players; i++) {
            for (Types.ACTIONS action : actions[i]) {

                StateObservation stCopy = stateObs.copy();
                ElapsedCpuTimer ectAdv = new ElapsedCpuTimer();

                Types.ACTIONS[] acts = new Types.ACTIONS[no_players];
                for (int j = 0; j < no_players; j++) {
                if (j != i)
                    acts[j] = Types.ACTIONS.ACTION_NIL;
                }
                acts[i] = action;

                if (no_players > 1) {
                // multi player
                ((StateObservationMulti) stCopy).advance(acts);
                } else {
                stCopy.advance(action);
                }

                copyStats++;
                advStats++;

                if (ect.remainingTimeMillis() < CompetitionParameters.WARMUP_TIME * 0.5) {
                ss1.add(ectAdv.elapsedNanos());
                }

                for (int j = 0; j < playoutLength; j++) {

                int[] index = new int[no_players];
                Types.ACTIONS[] actionPO = new Types.ACTIONS[no_players];
                for (int k = 0; k < no_players; k++) {
                    index[k] = new Random().nextInt(actions[i].size());
                    actionPO[k] = actions[i].get(index[k]);
                }

                ectAdv = new ElapsedCpuTimer();

                if (no_players > 1) {
                    ((StateObservationMulti) stCopy).advance(actionPO);
                } else {
                    stCopy.advance(actionPO[0]);
                }

                advStats++;

                if (ect.remainingTimeMillis() < CompetitionParameters.WARMUP_TIME * 0.5) {
                    ss2.add(ectAdv.elapsedNanos());
                }
                }
            }

            finish = ect.exceededMaxTime()
                || (copyStats > CompetitionParameters.WARMUP_CP && advStats > CompetitionParameters.WARMUP_ADV);

            // if(VERBOSE)
            // System.out.println("[WARM-UP] Remaining time: " +
            // ect.remainingTimeMillis() +
            // " ms, copy() calls: " + copyStats + ", advance() calls: " +
            // advStats);
            }
        }

        if (VERBOSE) {
            System.out.println("[WARM-UP] Finished, copy() calls: " + copyStats + ", advance() calls: " + advStats
                + ", time (s): " + ect.elapsedSeconds());
            // System.out.println(ss1);
            // System.out.println(ss2);
        }

        // Reset input to delete warm-up effects.
        Game.ki.resetAll();
    }

    /**
     * Tears the player down. This initiates the saving of actions to file. It
     * should be called when the game played is over.
     * 
     * @param toPlay
     *            game played.
     * @param players
     *            players to be closed.
     * @param actionFile
     *            file where players' actions should be saved.
     * @param randomSeed
     *            random seed of the game.
     * @param record
     *            boolean, true if actions should be recorded, false otherwise
     * @return false if there was a timeout from the players. true otherwise.
     */
    public static boolean tearPlayerDown(Game toPlay, Player[] players, String actionFile, int randomSeed,
	    boolean record) {
        // This is finished, no more actions, close the writer.
        if (toPlay.no_players > 1) {
            // multi player, write actions to files.
            try {
            if ((actionFile != null && !actionFile.equals("") && record)) {
                BufferedWriter writer = new BufferedWriter(new FileWriter(new File(actionFile)));

                // write random seed and game ticks
                writer.write(randomSeed + " " + toPlay.getGameTick() + "\n");

                // get player specific information

                String scores = "", winState = "";
                String[] actions = new String[toPlay.getGameTick() + 1];
                for (int i = 0; i < actions.length; i++) {
                actions[i] = "";
                }

                for (Player p : players) {
                // scores for all players
                scores += toPlay.getScore(p.getPlayerID()) + " ";

                // win state for all players
                winState += (toPlay.getWinner(p.getPlayerID()) == Types.WINNER.PLAYER_WINS ? 1 : 0) + " ";

                // actions for all players (same line if during the same
                // game tick)
                int i = 0;
                for (Types.ACTIONS act : p.getAllActions()) {
                    actions[i] += act.toString() + " ";
                    i++;
                }
                }

                // write everything to file
                writer.write(scores + "\n" + winState + "\n");
                for (String action : actions) {
                writer.write(action + "\n");
                }

                writer.close();
            }
            } catch (IOException e) {
            e.printStackTrace();
            }
        } else {
            // single player, let the player do all of this.
            players[0].teardown(toPlay);
        }

        for (Player p : players) {
            // Determine the time due for the controller close up.
            ElapsedCpuTimer ect = new ElapsedCpuTimer();
            ect.setMaxTimeMillis(CompetitionParameters.TEAR_DOWN_TIME);

            // Inform about the result and the final game state.
            // Inform about the result and the final game state.
            if (toPlay.no_players > 1)
            p.resultMulti(toPlay.getObservationMulti(p.getPlayerID()).copy(), ect);
            else
            p.result(toPlay.getObservation(), ect);

            // Check if we returned on time, and act in consequence.
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

    public static final boolean isHuman(String agentName) {
		if (agentName.equalsIgnoreCase("tracks.multiPlayer.tools.human.Agent")
			|| agentName.equalsIgnoreCase("tracks.singlePlayer.tools.human.Agent"))
			return true;
		return false;
	}

}
