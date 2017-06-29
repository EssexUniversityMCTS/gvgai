package tracks.singlePlayer;

import java.util.Random;

import core.logging.Logger;
import tracks.ArcadeMachine;

/**
 * Created with IntelliJ IDEA. User: Diego Date: 04/10/13 Time: 16:29 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Test {

    public static void main(String[] args) {

		// Available tracks:
		String sampleRandomController = "tracks.singlePlayer.simple.sampleRandom.Agent";
		String doNothingController = "tracks.singlePlayer.simple.doNothing.Agent";
		String sampleOneStepController = "tracks.singlePlayer.simple.sampleonesteplookahead.Agent";
		String sampleFlatMCTSController = "tracks.singlePlayer.simple.greedyTreeSearch.Agent";

		String sampleMCTSController = "tracks.singlePlayer.advanced.sampleMCTS.Agent";
        String sampleRSController = "tracks.singlePlayer.advanced.sampleRS.Agent";
        String sampleRHEAController = "tracks.singlePlayer.advanced.sampleRHEA.Agent";
		String sampleOLETSController = "tracks.singlePlayer.advanced.olets.Agent";

		// Available games:
		String gridGamesPath = "examples/gridphysics/";
        String contGamesPath = "examples/contphysics/";
        String gamesPath;
		String games[];
        boolean GRID_PHYSICS = true;

        // All public games (gridphysics)
		if(GRID_PHYSICS) {
		    gamesPath = gridGamesPath;
            games = new String[]{"aliens", "angelsdemons", "assemblyline", "avoidgeorge", "bait", // 0-4
                    "beltmanager", "blacksmoke", "boloadventures", "bomber", "bomberman", // 5-9
                    "boulderchase", "boulderdash", "brainman", "butterflies", "cakybaky", // 10-14
                    "camelRace", "catapults", "chainreaction", "chase", "chipschallenge", // 15-19
                    "clusters", "colourescape", "chopper", "cookmepasta", "cops", // 20-24
                    "crossfire", "defem", "defender", "digdug", "dungeon", // 25-29
                    "eighthpassenger", "eggomania", "enemycitadel", "escape", "factorymanager", // 30-34
                    "firecaster", "fireman", "firestorms", "freeway", "frogs", // 35-39
                    "garbagecollector", "gymkhana", "hungrybirds", "iceandfire", "ikaruga", // 40-44
                    "infection", "intersection", "islands", "jaws", "killBillVol1", // 45-49
                    "labyrinth", "labyrinthdual", "lasers", "lasers2", "lemmings", // 50-54
                    "missilecommand", "modality", "overload", "pacman", "painter", // 55-59
                    "pokemon", "plants", "plaqueattack", "portals", "raceBet", // 60-64
                    "raceBet2", "realportals", "realsokoban", "rivers", "roadfighter", // 65-69
                    "roguelike", "run", "seaquest", "sheriff", "shipwreck", // 70-74
                    "sokoban", "solarfox", "superman", "surround", "survivezombies", // 75-79
                    "tercio", "thecitadel", "thesnowman", "waitforbreakfast", "watergame", // 80-84
                    "waves", "whackamole", "wildgunman", "witnessprotection", "wrapsokoban", // 85-89
                    "zelda", "zenpuzzle"}; // 90, 91

        }else{
            gamesPath = contGamesPath;
            games = new String[]{"artillery", "asteroids", "bird", "bubble", "candy",   //0 - 4
                    "lander", "mario", "pong", "ptsp", "racing"};                       //5 - 9
        }


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

		// 1. This starts a game, in a level, played by a human.
		ArcadeMachine.playOneGame(game, level1, recordActionsFile, seed);

		// 2. This plays a game in a level by the controller.
//		ArcadeMachine.runOneGame(game, level1, visuals, sampleRHEAController, recordActionsFile, seed, 0);


		// 3. This replays a game from an action file previously recorded
	//	 String readActionsFile = recordActionsFile;
	//	 ArcadeMachine.replayGame(game, level1, visuals, readActionsFile);

		// 4. This plays a single game, in N levels, M times :
	//	String level2 = gamesPath + games[gameIdx] + "_lvl" + 1 +".txt";
	//	int M = 10;
	//	for(int i=0; i<games.length; i++){
	//		game = gamesPath + games[i] + ".txt";
	//		level1 = gamesPath + games[i] + "_lvl" + levelIdx +".txt";
	//		ArcadeMachine.runGames(game, new String[]{level1}, M, sampleMCTSController, null);
	//	}

		//5. This plays N games, in the first L levels, M times each. Actions to file optional (set saveActions to true).
//		int N = games.length, L = 1, M = 1;
//		boolean saveActions = false;
//		String[] levels = new String[L];
//		String[] actionFiles = new String[L*M];
//		for(int i = 0; i < N; ++i)
//		{
//			int actionIdx = 0;
//			game = gamesPath + games[i] + ".txt";
//			for(int j = 0; j < L; ++j){
//				levels[j] = gamesPath + games[i] + "_lvl" + j +".txt";
//				if(saveActions) for(int k = 0; k < M; ++k)
//				actionFiles[actionIdx++] = "actions_game_" + i + "_level_" + j + "_" + k + ".txt";
//			}
//			ArcadeMachine.runGames(game, levels, M, sampleRHEAController, saveActions? actionFiles:null);
//		}


    }
}
