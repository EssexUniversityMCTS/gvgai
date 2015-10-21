package controllers.YOLOBOT.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class GameLevelCounter {

	private static int LEVELS_PER_GAME = 5;

	static ClassLoader loader = GameLevelCounter.class.getClassLoader();
	private static String ClassPath = loader.getResource("YoloBot/Util/GameLevelCounter.class").getPath();
	
	private static String DESTINATION = ClassPath.substring(0, ClassPath.length()-22) + "level.txt";

	private static int actLevel = -1;

	public static int getLevelCount() {
		try {
			File f = new File(DESTINATION);
			if (f.exists()) {
				FileReader reader = new FileReader(f);
				BufferedReader br = new BufferedReader(reader);
				actLevel =  Integer.parseInt( br.readLine());
				br.close();
				reader.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return actLevel;

	}

	public static void updateLevelCount() {
		try {
			File f = new File(DESTINATION);
			if(f.exists()) f.delete();
			FileWriter writer = new FileWriter(DESTINATION);
			int newLevel = (actLevel + 1) % LEVELS_PER_GAME;
			writer.write(Integer.toString(newLevel));
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
