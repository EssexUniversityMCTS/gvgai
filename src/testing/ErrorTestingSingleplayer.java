package testing;

import java.util.Random;

import core.logging.Logger;
import tracks.ArcadeMachine;

/**
 * Created with IntelliJ IDEA. User: Diego Date: 04/10/13 Time: 16:29 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class ErrorTestingSingleplayer {

    public static void main(String[] args) {

	// Available tracks:
	String sampleOLETSController = "tracks.singlePlayer.advanced.olets.Agent";

	// Available games:
	String gridGamesPath = "examples/gridphysics/";
	String contGamesPath = "examples/contphysics/";
	String gamesPath;
	String games[];
	boolean GRID_PHYSICS = true;

	// All public games (gridphysics)
	if (GRID_PHYSICS) {
	    gamesPath = gridGamesPath;
	    games = new String[] { "aliens", "angelsdemons", "assemblyline", "avoidgeorge", "bait", // 0-4
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
		    "zelda", "zenpuzzle" }; // 90, 91

	} else {
	    gamesPath = contGamesPath;
	    games = new String[] { "artillery", "asteroids", "bird", "bubble", "candy", // 0
											// -
											// 4
		    "lander", "mario", "pong", "ptsp", "racing" }; // 5 - 9
	}

	// Other settings
	for (String g : games) {
	    for (int i = 0; i < 5; i++) {
		String game = gamesPath + g + ".txt";
		String level1 = gamesPath + g + "_lvl" + i + ".txt";
		Logger.getInstance().flushMessages();
		try {
		    ArcadeMachine.runOneGame(game, level1, false, sampleOLETSController, null, new Random().nextInt(),
			    0);
		} catch (Exception e) {
		    System.out.println("*************** " + g + " **************");
		    System.out.println("Level " + i);
		    e.printStackTrace();
		    System.out.println("****************************************");
		}
		if (Logger.getInstance().getMessageCount() > 0) {
		    System.out.println("*************** " + g + " **************");
		    System.out.println("Level " + i);
		    Logger.getInstance().printMessages();
		    System.out.println("****************************************");
		}
	    }
	}

    }
}
