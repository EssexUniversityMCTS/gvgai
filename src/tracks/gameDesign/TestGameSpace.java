package tracks.gameDesign;

import tools.Utils;
import tracks.DesignMachine;

import java.util.Random;

/**
 * Created with IntelliJ IDEA. User: Diego Date: 04/10/13 Time: 16:29 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class TestGameSpace {

    public static void main(String[] args) {

        // Available controllers:
        String sampleRandomController = "tracks.singlePlayer.simple.sampleRandom.Agent";
        String doNothingController = "tracks.singlePlayer.simple.doNothing.Agent";
        String sampleOneStepController = "tracks.singlePlayer.simple.sampleonesteplookahead.Agent";
        String sampleMCTSController = "tracks.singlePlayer.deprecated.sampleMCTS.Agent";
        String sampleFlatMCTSController = "tracks.singlePlayer.simple.greedyTreeSearch.Agent";
        String sampleOLMCTSController = "tracks.singlePlayer.advanced.sampleMCTS.Agent";
        String sampleGAController = "tracks.singlePlayer.deprecated.sampleGA.Agent";
        String sampleOLETSController = "tracks.singlePlayer.advanced.olets.Agent";
        String repeatOLETS = "tracks.singlePlayer.tools.repeatOLETS.Agent";

        // Game settings
        boolean visuals = true;
        int seed = new Random().nextInt();

        //Load available games
        String spGamesCollection =  "examples/all_games_gd.csv";
        String[][] games = Utils.readGames(spGamesCollection);


        // Game and level to play
        int gameIdx = 0;
        int levelIdx = 0; // level names from 0 to 4 (game_lvlN.txt).

        String gameName = games[gameIdx][1];
        String game = games[gameIdx][0];
        String level1 = game.replace(gameName, gameName + "_lvl" + levelIdx);

        String recordActionsFile = null;// "actions_" + games[gameIdx] + "_lvl"
                        // + levelIdx + "_" + seed + ".txt";
                        // where to record the actions
                        // executed. null if not to save.



        /** Game Spaces stuff starts here **/

        //Reads VGDL and loads game with parameters.
        DesignMachine dm = new DesignMachine(game);

        //0: Assigns values to parameters to play the game. Two ways: random and explicit.
        //0.a: Creating an individual at random:
//        int[] individual = new int[dm.getNumDimensions()];
//        for (int i = 0; i < individual.length; ++i)
//            individual[i] = new Random().nextInt(dm.getDimSize(i));

        //0.b: Creating a new individual with an int[]:
        //    Each parameter will take a value = "lower_bound + i*increment" as it is printed by printValues(...)
        int[] individual = new int[]{2, 2, 0, 4, 8, 3, 9, 4};

        //We can print a report with the parameters and values:
        dm.printValues(individual);


        //1. Play as a human.
        dm.playGame(individual, game, level1, seed);

        //2. Play with a controller.
//        dm.runOneGame(individual, game, level1, visuals, sampleMCTSController, recordActionsFile, seed, 0);


        //3. Random Search test.
//        int NUM_TRIALS = 10;
//        individual = new int[dm.getNumDimensions()];
//        int[] best = new int[dm.getNumDimensions()];
//        double bestFit = -Integer.MAX_VALUE;
//        visuals = false;
//        for(int count = 0; count < NUM_TRIALS; ++count)
//        {
//            for(int i = 0; i < individual.length; ++i)
//                individual[i] = new Random().nextInt(dm.getDimSize(i));
//
//            dm.printValues(individual);
//
//            double[] result = dm.runOneGame(individual, game, level1, visuals, sampleMCTSController, recordActionsFile, seed, 0);
//            double fit = 1000.0 * result[0] + result[1]; //win + score
//
//            if(fit > bestFit)
//            {
//                bestFit = fit;
//                System.arraycopy(individual, 0, best, 0, dm.getNumDimensions());
//            }
//
//        }
//
//        visuals = true;
//        System.out.println("##########################");
//        System.out.println("Best individual with fitness " + bestFit);
//        System.out.println("##########################");
//        dm.runOneGame(individual, game, level1, visuals, sampleMCTSController, recordActionsFile, seed, 0);
//        dm.printDimensions();
    }
}
