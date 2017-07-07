package testing;

import java.util.Random;

import core.logging.Logger;
import tracks.ArcadeMachine;

/**
 * Created with IntelliJ IDEA. User: Raluca Date: 12/04/16 This is a Java port
 * from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class ErrorTestingMultiplayer {

    public static void main(String[] args) {
	String sampleMCTSController = "tracks.multiPlayer.advanced.sampleMCTS.Agent";

	// Available games:
	String gamesPath = "examples/2player/";
	String games[] = new String[] {};

	// All public games
	games = new String[] { "accelerator", "akkaarrh", "asteroids", "beekeeper", "bombergirl", // 0-4
		"breedingdragons", "captureflag", "competesokoban", "copsNrobbers", "donkeykong", // 5-9
		"dragonattack", "drowning", "egghunt", "fatty", "firetruck", // 10-14
		"football", "ghostbusters", "gotcha", "isawsanta", "klax", // 15-19
		"mimic", "minesweeper", "minions", "oopsbrokeit", "reflection", // 20-24
		"rivalry", "romeoNjuliet", "samaritan", "sokoban", "steeplechase", // 25-29
		"teamescape", "thebridge", "trainride", "treasuremap", "tron", // 30-34
		"upgrade-x", "uphigh", "warzone", "watchout", "wheelme" }; // 35-39

	// Other settings
	for (String g : games) {
	    for (int i = 0; i < 5; i++) {
		String game = gamesPath + g + ".txt";
		String level1 = gamesPath + g + "_lvl" + i + ".txt";
		Logger.getInstance().flushMessages();
		try {
		    ArcadeMachine.runOneGame(game, level1, false, sampleMCTSController, null, new Random().nextInt(),
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
