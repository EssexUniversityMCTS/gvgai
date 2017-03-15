package tracks.gameDesign;

import core.DesignMachine;

import java.util.Random;

/**
 * Created with IntelliJ IDEA. User: Diego Date: 04/10/13 Time: 16:29 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class TestMultiGameSpace {

    public static void main(String[] args) {
        // Available tracks:
        String doNothingController = "tracks.multiPlayer.simple.doNothing.Agent";
        String randomController = "tracks.multiPlayer.simple.sampleRandom.Agent";
        String oneStepController = "tracks.multiPlayer.simple.sampleOneStepLookAhead.Agent";
        String sampleMCTSController = "tracks.multiPlayer.deprecated.sampleMCTS.Agent";
        String sampleOLMCTSController = "tracks.multiPlayer.deprecated.sampleMCTS.Agent";
        String sampleGAController = "tracks.multiPlayer.deprecated.sampleGA.Agent";
        String humanController = "tracks.multiPlayer.tools.human.Agent";

        String sampleRHEA = "tracks.multiPlayer.advanced.sampleRHEA.Agent";
        String sampleRS = "tracks.multiPlayer.advanced.sampleRS.Agent";


        // Available games:
        String gamesPath = "examples/gameDesign/";
        String games[] = new String[] {};
        String gameRules[] = new String[] {};


        // All public games
        games = new String[] { "ghostbusters", "fatty" }; 				// 0
        gameRules = new String[] {  };     // 0


        // Other settings
        boolean visuals = true;
        int seed = new Random().nextInt();

        // Game and level to play
        int gameIdx = 1;
        int levelIdx = 3; // level names from 0 to 4 (game_lvlN.txt).
        String controllers = sampleRHEA + " " + sampleRHEA;

        String game = gamesPath + games[gameIdx] + ".txt";
//        String game = gamesPath + gameRules[gameIdx] + ".txt";
//        String game = "examples/gridphysics/" + games[gameIdx] + ".txt";

        String level1 = gamesPath + games[gameIdx] + "_lvl" + levelIdx + ".txt";

        String recordActionsFile = null;// "actions_" + games[gameIdx] + "_lvl"
                        // + levelIdx + "_" + seed + ".txt";
                        // where to record the actions
                        // executed. null if not to save.



        /** New stuff starts here **/

        DesignMachine dm = new DesignMachine(game);

        int[] individual = new int[dm.getNumDimensions()];
        for(int i = 0; i < individual.length; ++i)
            individual[i] = new Random().nextInt(dm.getDimSize(i));

        dm.printValues(individual);
//        int[] individual = new int[]{0,1,2,14,1,4,9,1,5,5,2,4};

        //1. Play as a human.
//        dm.playGame(individual, game, level1, seed);

        //2. Play with a controller.
        dm.runOneGame(individual, game, level1, visuals, controllers, recordActionsFile, seed, 0);


//        double wins = 0.0, scores = 0.0;
//        double validationReps = 20;
//
//        visuals = false;
//        for(int i =0; i < validationReps; ++i)
//        {
//            tracks.singlePlayer.RHEA.Agent.SIMULATION_DEPTH = 6;
//
//            double[] results = ArcadeMachine.runOneGame(game, level1, visuals, rhea, recordActionsFile, seed, 0);
//
////            double[] results = dm.runOneGame(individual, game, level1, visuals, rhea, recordActionsFile, seed, 0);
//            wins += results[0];
//            scores += results[1];
//        }
//
//        System.out.printf("%.2f%%, average score: %.2f", 100*wins/validationReps, scores/validationReps);





//        dm.printDimensions();

        //Random Search tracks.singlePlayer.Test.
//        int NUM_TRIALS = 10;
//        int[] individual = new int[dm.getNumDimensions()];
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
