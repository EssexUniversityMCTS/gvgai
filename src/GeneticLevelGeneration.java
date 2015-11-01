import java.util.Random;

import core.ArcadeMachine;

public class GeneticLevelGeneration {
	public static void main(String[] args){
		String geneticGenerator = "levelGenerators.geneticLevelGenerator.LevelGenerator";
		
		String gamesPath = "examples/gridphysics/";
	    String generateLevelPath = "examples/generatedLevels/";
		String game = gamesPath + args[0] + ".txt";
		
		for(int i=0; i<5; i++){
	        String recordLevelFile = generateLevelPath + args[0] + "_lvl" + i +".txt";
			
	        ArcadeMachine.generateOneLevel(game, geneticGenerator, recordLevelFile);
		}
    }
}
