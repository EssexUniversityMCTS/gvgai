package levelGenerators.randomGeneticAlgorithm;

import java.util.Random;

import core.game.GameDescription;
import levelGenerators.randomGeneticAlgorithm.controller.Agent;

public class SharedData {
	public static Random random;
	public static GameDescription gameDescription;
	public static GameAnalyzer gameAnalyzer;
	public static Agent agent;
	
	public static double maxScorePercentage = 0.1;
	public static double drawFitness = 0.75;
	public static double minSize = 8;
	public static double maxSize = 20;
}
