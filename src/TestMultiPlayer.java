import java.util.Random;

import core.ArcadeMachine;

/**
 * Created with IntelliJ IDEA.
 * User: Raluca
 * Date: 12/04/16
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class TestMultiPlayer
{

    public static void main(String[] args)
    {
        //Available controllers:
        String doNothingController = "controllers.multiPlayer.doNothing.Agent";
        String randomController = "controllers.multiPlayer.sampleRandom.Agent";
        String oneStepController = "controllers.multiPlayer.sampleOneStepLookAhead.Agent";
        String sampleMCTSController = "controllers.multiPlayer.sampleMCTS.Agent";
        String sampleOLMCTSController = "controllers.multiPlayer.sampleOLMCTS.Agent";
        String sampleGAController = "controllers.multiPlayer.sampleGA.Agent";
        String humanController = "controllers.multiPlayer.human.Agent";

        //Set here the controllers used in the games (need 2 separated by space).
        String controllers = humanController + " " + humanController;

        //Available games:
        String gamesPath = "examples/2player/";
        String games[] = new String[]{};

        //All public games
        games = new String[]{"akkaarrh", "asteroids", "captureflag", "copsNrobbers", "gotcha", //0-4
                "klax", "samaritan", "sokoban", "steeplechase", "tron"};                          //5-9

        //Other settings
        boolean visuals = true;
        int seed = new Random().nextInt();

        //Game and level to play
        int gameIdx = 0;
        int levelIdx = 0; //level names from 0 to 4 (game_lvlN.txt).
        String game = gamesPath + games[gameIdx] + ".txt";
        String level1 = gamesPath + games[gameIdx] + "_lvl" + levelIdx +".txt";

        String recordActionsFile = null;//"actions_" + games[gameIdx] + "_lvl" + levelIdx + "_" + seed + ".txt"; //where to record the actions executed. null if not to save.

        // 1. This starts a game, in a level, played by two humans.
        //ArcadeMachine.playOneGameMulti(game, level1, recordActionsFile, seed);

        // 2. This plays a game in a level by the controllers. If one of the players is human, change the playerID passed
        // to the runOneGame method to be that of the human player (0 or 1).
        ArcadeMachine.runOneGame(game, level1, visuals, controllers, recordActionsFile, seed, 0);

        // 3. This replays a game from an action file previously recorded
        //String readActionsFile = recordActionsFile;
        //ArcadeMachine.replayGame(game, level1, visuals, readActionsFile);

        // 4. This plays a single game, in N levels, M times :
//        String level2 = gamesPath + games[gameIdx] + "_lvl" + 1 +".txt";
//        int M = 3;
//        for(int i=0; i<games.length; i++){
//        	game = gamesPath + games[i] + ".txt";
//        	level1 = gamesPath + games[i] + "_lvl" + levelIdx +".txt";
//        	ArcadeMachine.runGames(game, new String[]{level1}, M, controllers, null);
//        }

        //5. This plays N games, in the first L levels, M times each. Actions to file optional (set saveActions to true).
//        int N = 10, L = 5, M = 5;
//        boolean saveActions = false;
//        String[] levels = new String[L];
//        String[] actionFiles = new String[L*M];
//        for(int i = 0; i < N; ++i)
//        {
//            int actionIdx = 0;
//            game = gamesPath + games[i] + ".txt";
//            for(int j = 0; j < L; ++j){
//                levels[j] = gamesPath + games[i] + "_lvl" + j +".txt";
//                if(saveActions) for(int k = 0; k < M; ++k)
//                    actionFiles[actionIdx++] = "actions_game_" + i + "_level_" + j + "_" + k + ".txt";
//            }
//            ArcadeMachine.runGames(game, levels, M, controllers, saveActions? actionFiles:null);
//        }
    }
}
