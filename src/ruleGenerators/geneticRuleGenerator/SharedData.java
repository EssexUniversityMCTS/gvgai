package ruleGenerators.geneticRuleGenerator;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

import core.game.GameDescription;
import tools.GameAnalyzer;
import tools.LevelAnalyzer;

public class SharedData {
	/**
	 * The size of the Genetic Algorithm Population
	 */
	public static final int POPULATION_SIZE = 10;
	/**
	 * The amount of times used to check the one step look ahead and do nothing algorithm
	 */
	public static final int REPETITION_AMOUNT = 5;
	/**
	 * the amount of time to evaluate a single level
	 */
	public static final long EVALUATION_TIME = 30000;
	/**
	 * The amount of time given for each time step
	 */
	public static final long EVALUATION_STEP_TIME = 40;
	/**
	 * Crossover probability
	 */
	public static final double CROSSOVER_PROB = 0.5;
	/**
	 * Mutation probability
	 */
	public static final double MUTATION_PROB = 0.1;
	/**
	 * number of the best chromosomes that are transfered from one generation to another
	 */
	public static final int ELITISM_NUMBER = 1;
	/**
	 * very small value
	 */
	public static final double EIPSLON = 1e-6;
	
	/**
	 * the amount of mutations done on a chromosome to start as random
	 */
	public static final int RANDOM_INIT_AMOUNT = 50;
	/**
	 * the probability of inserting a new rule
	 */
	public static final double INSERTION_PROB = 0.25;
	/**
	 * the probability of deleting an exisiting rule
	 */
	public static final double DELETION_PROB = 0.25;
	/**
	 * the probability of changing an existing rule
	 */
	public static final double CHANGE_PROB = 0.5;
	
	/**
	 * the amount of times the mutation has to be done on a single chromosome
	 */
	public static final int MUTATION_AMOUNT = 1;
	/**
	 * the initial amount of mutations in the init pop
	 */
	public static final int INIT_MUTATION_AMOUNT = 7;
	/**
	 * used for calculating the minimum required score for the generated level
	 */
	public static final double MAX_SCORE_PERCENTAGE = 0.1;
	/**
	 * a fitness value given if the player ends in draw (not winning neither losing)
	 */
	public static final double DRAW_FITNESS  = 0;
	/**
	 * minimum level size
	 */
	public static final double MIN_SIZE = 4;
	/**
	 * maximum level size
	 */
	public static final double MAX_SIZE = 18;
	/**
	 * minimum acceptable solution
	 */
	public static final double MIN_SOLUTION_LENGTH = 200;
	/**
	 * minimum acceptable do nothing steps before dying
	 */
	public static final double MIN_DOTHING_STEPS = 40;
	/**
	 * minimum acceptable cover percentage of sprites
	 */
	public static final double MIN_COVER_PERCENTAGE = 0.05;
	/**
	 * maximum acceptable cover percentage of sprites
	 */
	public static final double MAX_COVER_PERCENTAGE = 0.3;
	/**
	 * minimum amount of unique rules that should be applied
	 */
	public static final double MIN_UNIQUE_RULE_NUMBER = 3;
	/**
	 * starting the GA with seeds from the constructive algorithm
	 */
	public static final boolean CONSTRUCTIVE_INITIALIZATION = true;
	
	/**
	 * The name of a the best agent with some human error
	 */
	public static final String AGENT_NAME = "controllers.singlePlayer.repeatOLETS.Agent";
	/**
	 * The name of a naive agent
	 */
	public static final String NAIVE_AGENT_NAME = "controllers.singlePlayer.sampleonesteplookahead.Agent";
	/**
	 * The name of the random agent
	 */
	public static final String RANDOM_AGENT_NAME = "controllers.singlePlayer.sampleRandom.Agent";
	/**
	 * The name of the do nothing agent
	 */
	public static final String DO_NOTHING_AGENT_NAME = "controllers.singlePlayer.doNothing.Agent";
	
	/**
	 * Protects the fitness function from looping forever
	 */
	public static final int PROTECTION_COUNTER = 5;
	/**
	 * The game description object
	 */
	public static GameDescription gameDescription;
	/**
	 * A game analyzer object to help in constructing the level
	 */
	public static GameAnalyzer gameAnalyzer;
	/**
	 * random object to help in choosing random stuff
	 */
	public static Random random;
	/**
	 * static file to write to
	 */
	public static String filename = "debugLog";
	/**
	 * Writer
	 */
	public static PrintWriter out;
	/**
	 * useful sprites in the game
	 */
	public static ArrayList<String> usefulSprites;
	/**
	 * the analyzer of this level
	 */
	public static LevelAnalyzer la;
}
