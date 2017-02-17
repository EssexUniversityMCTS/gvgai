import java.util.Random;

import controllers.singlePlayer.playTraces.CommonData;
import core.ArcadeMachine;

public class RuleGenerationPlayTraces {
    private static int MAX_LIMIT = 5;
    
    public static void main(String[] args){
	String[] controllers = new String[]{"doNothing", "sampleRandom", "sampleonesteplookahead",
		"sampleMCTS", "sampleOLMCTS", "olets"};
	
	String[] games = new String[] { "aliens", "angelsdemons", "assemblyline", "avoidgeorge", "bait", // 0-4
		"beltmanager", "blacksmoke", "boloadventures", "bomber", "bomberman", // 5-9
		"boulderchase", "boulderdash", "brainman", "butterflies", "cakybaky", // 10-14
		"camelRace", "catapults", "chainreaction", "chase", "chipschallenge", // 15-19
		"clusters", "colourescape", "chopper", "cookmepasta", "cops", // 20-24
		"crossfire", "defem", "defender", "digdug", "dungeon", // 25-29
		"eighthpassenger", "eggomania", "enemycitadel", "escape", "factorymanager", // 30-34
		"firecaster", "fireman", "firestorms", "freeway", "frogs", // 35-39
		"garbagecollector", "gymkhana", "hungrybirds", "iceandfire", "ikaruga", // 40-44
		"infection", "intersection", "islands", "jaws", "killbillVol1", // 45-49
		"labyrinth", "labyrinthdual", "lasers", "lasers2", "lemmings", // 50-54
		"missilecommand", "modality", "overload", "pacman", "painter", // 55-59
		"pokemon", "plants", "plaqueattack", "portals", "racebet", // 60-64
		"raceBet2", "realportals", "realsokoban", "rivers", "roadfighter", // 65-69
		"roguelike", "run", "seaquest", "sheriff", "shipwreck", // 70-74
		"sokoban", "solarfox", "superman", "surround", "survivezombies", // 75-79
		"tercio", "thecitadel", "thesnowman", "waitforbreakfast", "watergame", // 80-84
		"waves", "whackamole", "wildgunman", "witnessprotection", "wrapsokoban", // 85-89
		"zelda", "zenpuzzle" }; // 90, 91
	
	String gamesPath = "examples/gridphysics/";
	String generateRulePath = "examples/generatedGames/";
	String outputPath = "examples/playtraces/";
	int limit = 0;
	
	CommonData.index = Integer.parseInt(args[0]);
	CommonData.outputPath = outputPath;
	for(int i=0; i<controllers.length; i++){
	    CommonData.agentName = controllers[i];
	    for(int j=0; j<games.length; j++){
		CommonData.gameName = games[j];
		for(int k=0; k<5; k++){
		    CommonData.iteration = k;
		    limit += 1;
		    try{
			CommonData.openFile();
			ArcadeMachine.runOneGame(generateRulePath + games[j] + "_" + k + ".txt", 
				gamesPath + games[j] + "_lvl0.txt", false, "controllers.singlePlayer.playTraces." + 
				controllers[i] + ".Agent", null, new Random().nextInt(), 0);
			limit = 0;
			CommonData.closeFile();
		    }
		    catch(Exception e){
			if(limit < MAX_LIMIT){
			    k--;
			}
			else{
			    CommonData.closeFile();
			}
		    }
		}
	    }
	}
    }
}
