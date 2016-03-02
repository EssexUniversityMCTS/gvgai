import core.ArcadeMachine;

public class LevelGeneration {
	public static void main(String[] args){

		String levelGenerator = "levelGenerators." + args[0] + ".LevelGenerator";
		int numberOfLevels = 5;
		levelGenerators.randomLevelGenerator.LevelGenerator.includeBorders = true;

		String[] folderName = levelGenerator.split("\\.");
		String gamesPath = "examples/gridphysics/";
	    String generateLevelPath = "examples/generatedLevels/" + folderName[1] + "/";
		

		String game = gamesPath + args[1] + ".txt";
		for(int i=0; i<numberOfLevels; i++){
	        String recordLevelFile = generateLevelPath + args[1] + "_lvl" + i +".txt";
	        ArcadeMachine.generateOneLevel(game, levelGenerator, recordLevelFile);
		}
    }
}
