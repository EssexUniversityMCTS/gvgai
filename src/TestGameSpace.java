import core.DesignMachine;
import core.game.GameSpace;

import java.util.Random;

/**
 * Created with IntelliJ IDEA. User: Diego Date: 04/10/13 Time: 16:29 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class TestGameSpace {

    public static void main(String[] args) {
        // Available controllers:
        String sampleRandomController = "controllers.singlePlayer.sampleRandom.Agent";
        String doNothingController = "controllers.singlePlayer.doNothing.Agent";
        String sampleOneStepController = "controllers.singlePlayer.sampleonesteplookahead.Agent";
        String sampleMCTSController = "controllers.singlePlayer.sampleMCTS.Agent";
        String sampleFlatMCTSController = "controllers.singlePlayer.sampleFlatMCTS.Agent";
        String sampleOLMCTSController = "controllers.singlePlayer.sampleOLMCTS.Agent";
        String sampleGAController = "controllers.singlePlayer.sampleGA.Agent";
        String sampleOLETSController = "controllers.singlePlayer.olets.Agent";
        String repeatOLETS = "controllers.singlePlayer.repeatOLETS.Agent";


        // Available games:
        String gamesPath = "examples/gameDesign/";
        String games[] = new String[] {};

        // All public games
        games = new String[] { "aliens" }; 				// 0

        // Other settings
        boolean visuals = true;
        int seed = new Random().nextInt();

        // Game and level to play
        int gameIdx = 0;
        int levelIdx = 0; // level names from 0 to 4 (game_lvlN.txt).
        String game = gamesPath + games[gameIdx] + ".txt";
        String level1 = gamesPath + games[gameIdx] + "_lvl" + levelIdx + ".txt";

        String recordActionsFile = null;// "actions_" + games[gameIdx] + "_lvl"
                        // + levelIdx + "_" + seed + ".txt";
                        // where to record the actions
                        // executed. null if not to save.



        /** New stuff starts here **/

        DesignMachine dm = new DesignMachine(game);

        int[] individual = new int[dm.getNumDimensions()];
        for(int i = 0; i < individual.length; ++i)
        {
            individual[i] = new Random().nextInt(dm.getDimSize(i));
        }

        dm.printDimensions();

        //1. Play as a human.
        //dm.playGame(individual, level1, seed);

        //2. Play with a controller.
        dm.runOneGame(individual, level1, visuals, sampleMCTSController, recordActionsFile, seed, 0);
    }
}
