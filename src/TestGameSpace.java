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

    GameSpace gameSpace = DesignMachine.createGame(game, level1, seed);
    int a  = 0;


	// 1. This starts a game, in a level, played by a human.
	//DesignMachine.playOneGame(game, level1, recordActionsFile, seed);

	// 2. This plays a game in a level by the controller.
//	DesignMachine.runOneGame(game, level1, visuals, sampleMCTSController, recordActionsFile, seed, 0);

	// 3. This replays a game from an action file previously recorded
//	 String readActionsFile = recordActionsFile;
//	 DesignMachine.replayGame(game, level1, visuals, readActionsFile);

	// 4. This plays a single game, in N levels, M times :
//	String level2 = gamesPath + games[gameIdx] + "_lvl" + 1 +".txt";
//	int M = 10;
//	for(int i=0; i<games.length; i++){
//		game = gamesPath + games[i] + ".txt";
//		level1 = gamesPath + games[i] + "_lvl" + levelIdx +".txt";
//		DesignMachine.runGames(game, new String[]{level1}, M, sampleMCTSController, null);
//	}

	// 5. This starts a game, in a generated level created by a specific level generator
//	 if(DesignMachine.generateOneLevel(game, randomLevelGenerator, recordLevelFile)){
//		 DesignMachine.playOneGeneratedLevel(game, recordActionsFile,
//		 recordLevelFile, seed);
//	 }

	//6. This plays N games, in the first L levels, M times each. Actions to file optional (set saveActions to true).
//	int N = 92, L = 5, M = 10;
//	boolean saveActions = false;
//	String[] levels = new String[L];
//	String[] actionFiles = new String[L*M];
//	for(int i = 0; i < N; ++i)
//	{
//		int actionIdx = 0;
//		game = gamesPath + games[i] + ".txt";
//		for(int j = 0; j < L; ++j){
//			levels[j] = gamesPath + games[i] + "_lvl" + j +".txt";
//			if(saveActions) for(int k = 0; k < M; ++k)
//			actionFiles[actionIdx++] = "actions_game_" + i + "_level_" + j + "_" + k + ".txt";
//		}
//		DesignMachine.runGames(game, levels, M, sampleMCTSController, saveActions? actionFiles:null);
//	}

	// 7. Generate rules (Interaction and Terminations) for a fixed level
	// DesignMachine.generateRules(game, level1, randomRuleGenerator, recordGameFile, seed);
	// DesignMachine.playOneGame(recordGameFile, level1, recordActionsFile, seed);
	// DesignMachine.runOneGame(recordGameFile, level1, visuals, sampleMCTSController, recordActionsFile, seed, 0);
	 
    }
}
