package tracks;

import core.vgdl.VGDLFactory;
import core.vgdl.VGDLParser;
import core.vgdl.VGDLRegistry;
import core.competition.CompetitionParameters;
import core.content.ParameterContent;
import core.game.*;
import core.player.AbstractMultiPlayer;
import core.player.AbstractPlayer;
import core.player.Player;

import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;

/**
 * Created with IntelliJ IDEA. User: Diego Date: 06/11/13 Time: 11:24 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class DesignMachine {

    public int[] numValuesGenome;
    public ParameterContent[] parameterContents;
    public String[] parameterStrings;
    public GameSpace toPlay;

    public Player[] players;

    /**
     * Creates a Game Space for the game passed as parameter.
     * @param game_file game to create the game space for.
     */
    public DesignMachine(String game_file)
    {
        VGDLFactory.GetInstance().init(); // This always first thing to do.
        VGDLRegistry.GetInstance().init();

        // First, we create the game to be played..
        toPlay = (GameSpace) (new VGDLParser().parseGame(game_file));

        //Now, we need to retrieve information from the game to expose parameters.
        HashMap<String, ParameterContent> gameParams = toPlay.getParameters();
        numValuesGenome = new int[gameParams.size()];
        parameterContents = new ParameterContent[gameParams.size()];
        parameterStrings = new String[gameParams.size()];
        int idx = 0;

        Set<Entry<String, ParameterContent>> entries = gameParams.entrySet();
        for(Map.Entry<String, ParameterContent> entry : entries)
        {
            numValuesGenome[idx] = entry.getValue().getnPoints();
            parameterContents[idx] = entry.getValue();
            parameterStrings[idx] = entry.getValue().toString();
            idx++;
        }
    }

    public double[] playGame(int[] parameters, String game_file, String level_file, int randomSeed)
    {
        String agentName = "tracks.singlePlayer.tools.human.Agent";
        boolean visuals = true;
        return runOneGame(parameters, game_file, level_file, visuals, agentName, null, randomSeed, 0);
    }


    public double[] playGame2P(int[] parameters, String game_file, String level_file, int randomSeed)
    {
        String humanController = "tracks.multiPlayer.tools.human.Agent";
        String controllers = humanController + " " + humanController;
        boolean visuals = true;
        return runOneGame(parameters, game_file, level_file, visuals, controllers, null, randomSeed, 0);
    }

    /**
     * Reads and launches a game for a bot to be played. Graphics can be on or
     * off.
     *
     * @param parameters parameters for the game space
     * @param level_file file with the level to be played.
     * @param visuals true to show the graphics, false otherwise.
     * @param agentNames names (inc. package) where the tracks are otherwise.  Names separated by space.
     * @param actionFile filename of the files where the actions of these players, for this game, should be recorded.
     * @param randomSeed sampleRandom seed for the sampleRandom generator.
     * @param playerID ID of the human player
     */
    public double[] runOneGame(int[] parameters, String game_file, String level_file, boolean visuals, String agentNames, String actionFile, int randomSeed, int playerID) {

        //Second, build the game with these parameters.
        for(int i = 0; i < parameters.length; ++i)
        {
            int value = parameters[i];
            ParameterContent pc = parameterContents[i];
            pc.setRunningValue(value);
        }

        //Parse the game with the new parameters.
        toPlay = (GameSpace) (new VGDLParser().parseGameWithParameters(game_file, toPlay.getParameters()));

        //Now it's time to build the level.
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

		if (no_players > 1) {
			// multi player games
			players = new AbstractMultiPlayer[no_players];
		} else {
			// single player games
			players = new AbstractPlayer[no_players];
		}

		for (int i = 0; i < no_players; i++) {

			humans[i] = ArcadeMachine.isHuman(names[i]);
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
                return toPlay.handleResult();
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
		//toPlay.printResult();

		return toPlay.getFullResult();
    }

    /**
     * Returns the number of dimensions of this game space.
     * @return number of dimensions.
     */
    public int getNumDimensions() {return numValuesGenome.length;}

    /**
     * Returns the number of points in a given dimensions
     * @param dim_idx Index of the dimension to check.
     * @return the number of points in the dimension dim_idx
     */
    public int getDimSize (int dim_idx) {return numValuesGenome[dim_idx];}


    /** DEBUG FUNCTIONS **/

    public void printValues(int[] parameters)
    {
        //First, we don't know if this game has been played before, so we'll reset just in case.
        toPlay.reset();

        //Second, build the game with these parameters.
        for(int i = 0; i < parameters.length; ++i)
        {
            int value = parameters[i];
            ParameterContent pc = parameterContents[i];
            pc.setRunningValue(value);
        }


        System.out.println(Arrays.toString(parameters));
        printDimensions(parameterContents);
    }

    public void printDimensions()
    {
        printDimensions(parameterContents);
    }

    private void printDimensions(ParameterContent[] params)
    {
        long spaceSize = 1;
        System.out.println("Individual length: " + getNumDimensions());
        System.out.printf("%-20.20s  %-15.15s %-20.20s %s \n", "Value", "Dim. Size", "Range", "Description");
        for(int i = 0; i < getNumDimensions(); ++i)
        {
            ParameterContent pc = parameterContents[i];
            String val = pc.getStValue();
            spaceSize *= getDimSize(i);
            System.out.printf("%-20.20s  %-15.15s %-20.20s %s \n", val, getDimSize(i), pc.values(), parameterStrings[i]);

        }
        DecimalFormat df = new DecimalFormat("0.000E0");
        System.out.println("Search Space Size: " + df.format(spaceSize));
    }
}

