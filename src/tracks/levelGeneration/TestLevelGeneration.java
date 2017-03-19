package tracks.levelGeneration;

public class TestLevelGeneration {
    public static void main(String[] args) {

	String levelGenerator = "tracks.levelGeneration." + args[0] + ".LevelGenerator";
	int numberOfLevels = 5;
	tracks.levelGeneration.randomLevelGenerator.LevelGenerator.includeBorders = true;

	String[] folderName = levelGenerator.split("\\.");
	String gamesPath = "examples/gridphysics/";
	String generateLevelPath = "examples/generatedLevels/" + folderName[1] + "/";

	String game = gamesPath + args[1] + ".txt";
	for (int i = 0; i < numberOfLevels; i++) {
	    String recordLevelFile = generateLevelPath + args[1] + "_lvl" + i + ".txt";
	    LevelGenMachine.generateOneLevel(game, levelGenerator, recordLevelFile);
	}
    }
}
