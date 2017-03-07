import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CombiningFiles {
    
    private static void combineFiles(String inPath, String outPath, String gameVariant, String controller) throws IOException{
	BufferedWriter bw = new BufferedWriter(new FileWriter(outPath + controller + "/" + gameVariant + ".txt"));
	for(int i=0; i<100; i++){
	    bw.write("@@@@@@@@@" + i + "@@@@@@@@@\n");
	    BufferedReader br = new BufferedReader(new FileReader(inPath + controller + "_" + gameVariant + "_" + i + ".txt"));
	    while(true){
		String line = br.readLine();
		if(line == null){
		    break;
		}
		bw.write(line + "\n");
	    }
	    br.close();
	}
	bw.close();
    }
    
    public static void main(String[] args) throws IOException{
	String inPath = "examples/playtraces/";
	String outPath = "examples/combinedtraces/";
	new File(outPath).mkdir();
	
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
		"zelda", "zenpuzzle" };
	
	for(int i=0; i<controllers.length; i++){
	    for(int j=0; j<games.length; j++){
		for(int k=0; k<5; k++){
		    new File(outPath + controllers[i] + "/").mkdir();
		    combineFiles(inPath, outPath, games[j] + "_" + k, controllers[i]);
		}
	    }
	}
	
    }
}
