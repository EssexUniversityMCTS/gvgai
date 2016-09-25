import core.ArcadeMachine;

public class TestOptimization {
	public static void main(String[] args)
    {
		String gamesPath = "examples/gridphysics/";
        String games[] = new String[]{};
        
		games = new String[]{"aliens", "angelsdemons", "assemblyline", "avoidgeorge", "bait", //0-4
                "blacksmoke", "boloadventures", "bomber", "boulderchase", "boulderdash",      //5-9
                "brainman", "butterflies", "cakybaky", "camelRace", "catapults",              //10-14
                "chainreaction", "chase", "chipschallenge", "clusters", "colourescape",       //15-19
                "chopper", "cookmepasta", "cops", "crossfire", "defem",                       //20-24
                "defender", "digdug", "dungeon", "eggomania", "enemycitadel",                 //25-29
                "escape", "factorymanager", "firecaster",  "fireman", "firestorms",           //30-34
                "freeway", "frogs", "gymkhana", "hungrybirds", "iceandfire",                  //35-39
                "infection", "intersection", "islands", "jaws", "labyrinth",                  //40-44
                "labyrinthdual", "lasers", "lasers2", "lemmings", "missilecommand",           //45-49
                "modality", "overload", "pacman", "painter", "plants",                        //50-54
                "plaqueattack", "portals", "racebet", "raceBet2", "realportals",              //55-59
                "realsokoban", "rivers", "roguelike", "run", "seaquest",                      //60-64
                "sheriff", "shipwreck", "sokoban", "solarfox" ,"superman",                    //65-69
                "surround", "survivezombies", "tercio", "thecitadel", "thesnowman",           //70-74
                "waitforbreakfast", "watergame", "waves", "whackamole", "witnessprotection",  //75-79
                "zelda", "zenpuzzle" };                                                       //80, 81
		
		int[] selectedGames = new int[]{0, 9, 11};
		int selectedLevel = 0;
		
		String ucbEvoEquationName = "core.optimization.ucbOptimization.UCBEvoEquation";
		
		String randomOptimizerName = "optimizers.random.Optimizer";
		String hillClimibingOptimizerName = "optimizers.hillClimbing.Optimizer";
		
		String[] tempGames = new String[selectedGames.length];
		String[] tempLevels = new String[selectedGames.length];
		for(int i=0; i<selectedGames.length; i++){
			tempGames[i] = gamesPath + games[selectedGames[i]] + ".txt";
			tempLevels[i] = gamesPath + games[selectedGames[i]] + "_lvl" + selectedLevel + ".txt";
		}
		
		double[] parameters = ArcadeMachine.optimizeUCBAgent(hillClimibingOptimizerName, ucbEvoEquationName, tempGames, tempLevels);
		for(int i=0; i<parameters.length; i++){
			System.out.print(parameters[i] + ", ");
		}
    }
}
