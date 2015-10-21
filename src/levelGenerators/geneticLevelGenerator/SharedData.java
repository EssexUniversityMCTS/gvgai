package levelGenerators.geneticLevelGenerator;

import java.util.Random;

import core.game.GameDescription;
import levelGenerators.GameAnalyzer;

public class SharedData {
	public static final int POPULATION_SIZE = 50;
	public static final int GENERATIONS_SIZE = 50;
	public static final long EVALUATION_TIME = 1000;
	public static final double CROSSOVER_PROB = 0.7;
	public static final double MUTATION_PROB = 0.1;
	public static final int ELITISM_NUMBER = 1;
	
	public static final int RANDOM_INIT_AMOUNT = 50;
	public static final double INSERTION_PROB = 0.25;
	public static final double DELETION_PROB = 0.25;
	public static final int MUTATION_AMOUNT = 1;
	
	public static final double MAX_SCORE_PERCENTAGE = 0.1;
	public static final double DRAW_FITNESS  = 0;
	public static final double MIN_SIZE = 4;
	public static final double MAX_SIZE = 18;
	public static final double MIN_SOLUTION_LENGTH = 50;
	public static final double MIN_RULE_NUMBER = 30;
	public static final boolean CONSTRUCTIVE_INITIALIZATION = true;
	
	public static final String AGENT_NAME = "controllers.sampleUCT.Agent";
	
	public static GameDescription gameDescription;
	public static GameAnalyzer gameAnalyzer;
	public static Random random;
	public static levelGenerators.constructiveLevelGenerator.LevelGenerator constructiveGen;
	
}
