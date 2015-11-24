import core.ArcadeMachine;

public class LevelGeneration {
	public static void main(String[] args){
		String levelGenerator = "levelGenerators.geneticLevelGenerator.LevelGenerator";
		int numberOfLevels = 2;
		
		String[] folderName = levelGenerator.split("\\.");
		String gamesPath = "examples/gridphysics/";
	    String generateLevelPath = "examples/generatedLevels/" + folderName[1] + "/";
		
		for(int j=0; j<args.length; j++){
			String game = gamesPath + args[j] + ".txt";
			for(int i=0; i<numberOfLevels; i++){
		        String recordLevelFile = generateLevelPath + args[j] + "_lvl" + i +".txt";
				
		        ArcadeMachine.generateOneLevel(game, levelGenerator, recordLevelFile);
			}
		}
    }
}
