package tracks.multiPlayer;

import java.util.Random;

import core.logging.Logger;
import tools.Utils;
import tracks.ArcadeMachine;

/**
 * Created with IntelliJ IDEA. User: Raluca Date: 12/04/16 This is a Java port
 * from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class TestMultiPlayer {

    public static void main(String[] args) {

		// Available controllers:
		String doNothingController = "tracks.multiPlayer.simple.doNothing.Agent";
		String randomController = "tracks.multiPlayer.simple.sampleRandom.Agent";
		String oneStepController = "tracks.multiPlayer.simple.sampleOneStepLookAhead.Agent";

		String sampleMCTSController = "tracks.multiPlayer.advanced.sampleMCTS.Agent";
		String sampleRSController = "tracks.multiPlayer.advanced.sampleRS.Agent";
		String sampleRHEAController = "tracks.multiPlayer.advanced.sampleRHEA.Agent";
		String humanController = "tracks.multiPlayer.tools.human.Agent";

		// Set here the controllers used in the games (need 2 separated by space).
		String controllers = sampleMCTSController + " " + sampleMCTSController;

		//Load available games
		String spGamesCollection =  "examples/all_games_2p.csv";
		String[][] games = Utils.readGames(spGamesCollection);

		// Other settings
		boolean visuals = true;
		int seed = new Random().nextInt();

		// Game and level to play
		int gameIdx = 0;
		int levelIdx = 0; // level names from 0 to 4 (game_lvlN.txt).
		String gameName = games[gameIdx][1];
		String game = games[gameIdx][0];
		String level1 = game.replace(gameName, gameName + "_lvl" + levelIdx);

		String recordActionsFile = null;// "actions_" + games[gameIdx] + "_lvl"
						// + levelIdx + "_" + seed + ".txt";
						// //where to record the actions
						// executed. null if not to save.

		// 1. This starts a game, in a level, played by two humans.
		ArcadeMachine.playOneGameMulti(game, level1, recordActionsFile, seed);


		// 2. This plays a game in a level by the tracks. If one of the
		// players is human, change the playerID passed
		// to the runOneGame method to be that of the human player (0 or 1).
//		ArcadeMachine.runOneGame(game, level1, visuals, controllers, recordActionsFile, seed, 0);

		// 3. This replays a game from an action file previously recorded
//		 String readActionsFile = recordActionsFile;
//		 ArcadeMachine.replayGame(game, level1, visuals, readActionsFile);

		// 4. This plays a single game, in N levels, M times :
//		String level2 = new String(game).replace(gameName, gameName + "_lvl" + 1);
//		int M = 1;
//		for(int i=0; i<games.length; i++){
//			game = games[i][0];
//			gameName = games[i][1];
//			level1 = game.replace(gameName, gameName + "_lvl" + levelIdx);
//			ArcadeMachine.runGames(game, new String[]{level1}, M, controllers, null);
//		}

		 // 5. This plays N games, in the first L levels, M times each. Actions to file optional (set saveActions to true).
//		 int N = games.length, L = 2, M = 1;
//		 boolean saveActions = false;
//		 String[] levels = new String[L];
//		 String[] actionFiles = new String[L*M];
//		 for(int i = 0; i < N; ++i)
//		 {
//	         int actionIdx = 0;
//			 game = games[i][0];
//			 gameName = games[i][1];
//	         for(int j = 0; j < L; ++j)
//	         {
//	             levels[j] = game.replace(gameName, gameName + "_lvl" + j);
//	             if(saveActions) for(int k = 0; k < M; ++k)
//	                actionFiles[actionIdx++] = "actions_game_" + i + "_level_" + j + "_"  + k + ".txt";
//	         }
//		    ArcadeMachine.runGames(game, levels, M, controllers, saveActions? actionFiles:null);
//		 }

		 // 6. This plays a round robin style tournament between multiple tracks, in N games, first L levels, M times each.
		 // Controllers are swapped for each match as well. Actions to file optional (set saveActions to true).
//		 int N = games.length, L = 5, M = 2;
//		 boolean saveActions = false;
//		 String[] levels = new String[L];
//		 String[] actionFiles = new String[L*M];
//		 int actionIdx = 0;
//
//	     //add all controllers that should play in this array
//		 String[] cont = new String[]{doNothingController, randomController, oneStepController, sampleRHEAController, sampleMCTSController, sampleMCTSController};
//	     for(int i = 0; i < N; ++i)
//	     {
//	     	game = games[i][0];
//	     	gameName = games[i][1];
//	        for (int k = 0; k < cont.length - 1; k++) {
//	            for (int t = k + 1; t < cont.length; t++) {
//	                // set action files for the first controller order
//	                for(int j = 0; j < L; ++j){
//	                    levels[j] = game.replace(gameName, gameName + "_lvl" + j);
//	                    if(saveActions){
//	                        actionIdx = 0;
//	                        for(int p = 0; p < M; ++p) {
//	                          actionFiles[actionIdx++] = "actions_" + cont[k] + "_" + cont[t] + "_game_" + i + "_level_" + j + "_" + p + ".txt";
//	                        }
//	                    }
//	                }
//
//	                controllers = cont[k] + " " + cont[t];
//
//	                System.out.println(controllers);
//	                ArcadeMachine.runGames(game, levels, M, controllers, saveActions ? actionFiles : null);
//
//	                // reset action files for the swapped tracks
//	                if (saveActions) {
//	                    actionIdx = 0;
//	                    for (int j = 0; j < L; ++j) {
//	                        for (int p = 0; p < M; ++p) {
//	                            actionFiles[actionIdx++] = "actions_" + cont[t] + "_" + cont[k] + "_game_" + i + "_level_" + j + "_" + p + ".txt";
//	                        }
//	                    }
//	                }
//	                controllers = cont[t] + " " + cont[k];
//	                System.out.println(controllers);
//	                ArcadeMachine.runGames(game, levels, M, controllers, saveActions ? actionFiles : null);
//	            }
//	        }
//	     }



    }
}
