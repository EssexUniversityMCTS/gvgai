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
	public static final int POPULATION_SIZE = 50;
	/**
	 * useful sprites in the game
	 */
	public static ArrayList<String> usefulSprites;
	/**
	 * the analyzer of this level
	 */
	public static LevelAnalyzer la;
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
	public static PrintWriter output;
	
	/**
	 * The name of a the best agent with some human error
	 */
	public static final String BEST_AGENT_NAME = "controllers.singlePlayer.repeatOLETS.Agent";
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
	 * The amount of times used to check the one step look ahead and do nothing algorithm
	 */
	public static final int REPETITION_AMOUNT = 5;
	/**
	 * The amount of time given for each time step
	 */
	public static final long EVALUATION_STEP_TIME = 40;
	/**
	 * The amount of times we perform a mutation on a set
	 */
	public static final int MUTATION_AMOUNT = 1;
	/**
	 * the probability of inserting a new rule
	 */
	public static final double INSERTION_PROB = 0.33;
	/**
	 * the probability of performing a deletion an exisiting rule
	 */
	public static final double DELETION_PROB = 0.33;
	/**
	 * the probability of performing a modify on an existing rule
	 */
	public static final double MODIFY_RULE_PROB = 0.34;
	/**
	 * the probablity of changing a parameter of an existing rule
	 */
	public static final double MODIFY_PARAM_PROB = 0.5;
	/**
	 * The probability to insert a new parameter into an existing rule
	 */
	public static final double INSERT_PARAM_PROB = 0.5;
	/**
	 * The probability to delete a parameter from an exisiting rule
	 */
	public static final double DELETE_PARAM_PROB = 0.5;
	/**
	 * The probability of putting a number or a sprite as the value of a parameter insertion
	 */
	public static final double PARAM_NUM_OR_SPRITE_PROB = 0.5;
	/**
	 * The upper bound on a numerical value for a parameter insertion
	 */
	public static final int NUMBERICAL_VALUE_PARAM = 100;
	
}
