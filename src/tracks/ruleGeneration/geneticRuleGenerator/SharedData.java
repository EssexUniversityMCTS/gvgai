package tracks.ruleGeneration.geneticRuleGenerator;

import java.util.ArrayList;
import java.util.Random;

import core.player.AbstractPlayer;
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
	 * the amount of time to evaluate a single level
	 */
	public static final long EVALUATION_TIME = 10000;
	
	/**
	 * number of the best chromosomes that are transfered from one generation to another
	 */
	public static final int ELITISM_NUMBER = 1;
	
	/**
	 * Crossover probability
	 */
	public static final double CROSSOVER_PROB = 0.9;
	
	/**
	 * Mutation probability
	 */
	public static final double MUTATION_PROB = 0.1;
	/**
	 * The percentage of the initial population that is randomly generated
	 */
	public static final double INIT_RANDOM_PERCENT = 0.4;
	/**
	 * The percentage of the initial population that is constructively generated
	 */
	public static final double INIT_CONSTRUCT_PERCENT = 0.2;
	/**
	 * The percentage of the initial population that is mutationally generated based
	 * off what already exists (random and constructive) in the population
	 */
	public static final double INIT_MUT_PERCENT = 0.4;
	
	/**
	 * the initial amount of mutations in the init pop
	 */
	public static final int INIT_MUTATION_AMOUNT = 2;
	/**
	 * random object to help in choosing random stuff
	 */
	public static Random random;
	
	/**
	 * The name of a the best agent with some human error
	 */
	public static final String BEST_AGENT_NAME = "tracks.singlePlayer.advanced.olets.Agent";
	/**
	 * The name of a naive agent
	 */
	public static final String NAIVE_AGENT_NAME = "tracks.singlePlayer.advanced.sampleMCTS.Agent";
	/**
	 * The name of the random agent
	 */
	public static final String RANDOM_AGENT_NAME = "tracks.singlePlayer.simple.simpleRandom.Agent";
	/**
	 * The name of the do nothing agent
	 */
	public static final String DO_NOTHING_AGENT_NAME = "tracks.singlePlayer.simple.doNothing.Agent";
	
	/**
	 * How many steps the best agent gets to evaluate a game
	 */
	public static final int EVALUATION_STEP_COUNT = 300;
	
	/**
	 * The amount of times used to run each agent
	 */
	public static final int REPETITION_AMOUNT = 3;
	/**
	 * The amount of time given for each time step
	 */
	public static final long EVALUATION_STEP_TIME = 40;
	/**
	 * The amount of times we perform a mutation on a set
	 */
	public static final int MUTATION_AMOUNT = 2;
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
	public static final double MODIFY_RULE_PROB = .34;
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
	 * The probability of a termination rule being a "win"
	 */
	public static final double WIN_PARAM_PROB = 0.5;
	/**
	 * The upper bound on a numerical value for a parameter insertion
	 */
	public static final int NUMERICAL_VALUE_PARAM = 2000;
	
	/**
	 * The upper bound on a numerical value for a termination limit parameter insertion
	 */
	public static final int TERMINATION_LIMIT_PARAM = 1000;
	
	/**
	 * Protects the fitness function from looping forever
	 */
	public static final int PROTECTION_COUNTER = 3;
	
	/**
	 * the best automated agent
	 */
	public static AbstractPlayer automatedAgent;
	/**
	 * the naive automated agent
	 */
	public static AbstractPlayer naiveAgent;
	/**
	 * the do nothing automated agent
	 */
	public static AbstractPlayer doNothingAgent;
	/**
	 * the random agent
	 */
	public static AbstractPlayer randomAgent;
	
	public static tracks.ruleGeneration.constructiveRuleGenerator.RuleGenerator constGen;
	
	/**
	 * Array contains all interactions we want to mutate over
	 */
	public static String[] interactions = new String[]{
			"killSprite", "killAll", "killIfHasMore", "killIfHasLess", "killIfFromAbove",
			"killIfOtherHasMore", "transformToSingleton", "spawnBehind",
			"spawnIfHasMore", "spawnIfHasLess", "cloneSprite", "transformTo", "transformIfCounts", 
			"transformToRandomChild", "updateSpawnType", "removeScore", 
			"addHealthPoints",  "addHealthPointsToMax", "subtractHealthPoints", "increaseSpeedToAll", 
			"decreaseSpeedToAll", "setSpeedForAll", "stepBack",  "undoAll", "flipDirection",  
			"reverseDirection", "attractGaze", "align", "turnAround", "wrapAround", "teleportToExit",
			"pullWithIt", "bounceForward", "collectResource", "changeResource"};
	/** 
	 * Array contains all terminations
	 */
	public static String[] terminations = new String[] {
		"SpriteCounter", "SpriteCounterMore", "MultiSpriteCounter",
		"StopCounter", "Timeout"};
	/**
	 * Array contains all possible interaction parameter types
	 */
	public static String[] interactionParams = new String[] {
		"scoreChange", "stype", "limit", "resource", "stype_other", "forceOrientation", "spawnPoint",
		"value", "geq", "leq"};
	public static String[] terminationParams = new String[] {
		"stype", "stype1", "stype2", "stype3"
	};

}
