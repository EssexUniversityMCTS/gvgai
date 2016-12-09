package ruleGenerators.geneticRuleGenerator;

import java.util.ArrayList;
import java.util.Random;

import core.ArcadeMachine;
import core.game.SLDescription;
import core.game.StateObservation;
import core.generator.AbstractRuleGenerator;
import ontology.Types;
import tools.ElapsedCpuTimer;

public class RuleGenerator extends AbstractRuleGenerator {
	/**
	 * Defines how large the population will be
	 */
	private final int POP_SIZE = 100;
	/**
	 * ArrayList that contains the population
	 */
	private ArrayList<Chromosome> population;
	/**
	 * Array contains all the simple interactions
	 */
	private String[] interactions = new String[]{
			"killSprite", "killAll", "killIfHasMore", "killIfHasLess", "killIfFromAbove",
			"killIfOtherHasMore", "transformToSingleton", "spawnBehind", "stepBack", 
			"spawnIfHasMore", "spawnIfHasLess", "cloneSprite", "transformTo", "undoAll", 
			"flipDirection", "transformIfCounts", "transformToRandomChild", "updateSpawnType", 
			"removeScore", "addHealthPoints", "addHealthPointsToMax", "reverseDirection", 
			"subtractHealthPoints", "increaseSpeedToAll", "decreaseSpeedToAll", "attractGaze", 
			"align", "turnAround", "wrapAround", "pullWithIt", "bounceForward", "teleportToExit",
			"collectResource", "setSpeedForAll", "undoAll", "reverseDirection", "changeResource"};
	/**
	 * A list of all the useful sprites in the game
	 */
	private ArrayList<String> usefulSprites;
	/**
	 * Random object to help in generation
	 */
	private Random random;
	/**
	 * A constructor to help speed up the creation of random gens
	 */
	private static ruleGenerators.randomRuleGenerator.RuleGenerator randomGen;

	/**
	 * A constructor to help speed up the creation of constructive gens
	 */
	private static ruleGenerators.constructiveRuleGenerator.RuleGenerator constructGen;
	/**
	 * contains info about sprites and current level
	 */
	private SLDescription sl;
	/**
	 * amount of time allowed
	 */
	private ElapsedCpuTimer time;
	/**
	 * chance of mutation
	 */
	private double MUTATION_CHANCE = 0.1;
	/**
	 * elitism coefficient
	 */
	private double ELITISM = 0.1;
	/**
	 * chance of crossover
	 */
	private double CROSSOVER_CHANCE = 0.1;
	
	/**
	 * This is an evolutionary rule generator
	 * @param sl	contains information about sprites and current level
	 * @param time	amount of time allowed
	 */
	public RuleGenerator(SLDescription sl, ElapsedCpuTimer time) {
		this.sl = sl;
		this.time = time;
		this.usefulSprites = new ArrayList<String>();
		this.random = new Random();

		String[][] currentLevel = sl.getCurrentLevel();
		//Just get the useful sprites from the current level
		for (int y = 0; y < currentLevel.length; y++) {
			for (int x = 0; x < currentLevel[y].length; x++) {
				String[] parts = currentLevel[y][x].split(",");
				for (int i = 0; i < parts.length; i++) {
					if (parts[i].trim().length() > 0) {
						//Add the sprite if it doesn't exisit
						if (!usefulSprites.contains(parts[i].trim())) {
							usefulSprites.add(parts[i].trim());
						}
					}
				}
			}
		}
		//Initialize the population
		initPop(sl, time);
		
		// iterate through population and mutate
	}

	/**
	 * convert the arraylist of string to a normal array of string
	 * @param list	input arraylist
	 * @return		string array
	 */
	 private String[] getArray(ArrayList<String> list){
		 String[] array = new String[list.size()];
		 for(int i=0; i<list.size(); i++){
			 array[i] = list.get(i);
		 }
		 return array;
	 }
	/**
	 * Initializes the population with 100 RandomGenerators
	 */
	public void initPop(SLDescription sl, ElapsedCpuTimer time) {
		population = new ArrayList<Chromosome>();
		randomGen = new ruleGenerators.randomRuleGenerator.RuleGenerator(sl, time);
		constructGen = new ruleGenerators.constructiveRuleGenerator.RuleGenerator(sl, time);
		Chromosome c;
		for(int i = 0; i < POP_SIZE / 2; i++) {
			c = new Chromosome(randomGen.generateRules(sl, time), sl, time);
			population.add(c);
			c = new Chromosome(constructGen.generateRules(sl, time), sl, time);
			population.add(c);
		}
	}
	
	/**
	 * The first fitness function. This will eliminate games that are not feasible to play
	 */
	public void feasibilityTest() {
		// test ruleset for robustness
		for(Chromosome chrome : population) {
			if(!chrome.feasibilityTest()){
				population.remove(chrome);
			}
		}
	}
	
	public ArrayList<Chromosome> saveElites() {
		ArrayList<Chromosome> popCopy = (ArrayList<Chromosome>) population.clone();
		ArrayList<Chromosome> elites = new ArrayList<Chromosome>();
		int indexMax = 0;
		double fitnessMax = 0;
		for(int i = 0; i < ELITISM * 100; i++) {
			for(int j = 0; i < popCopy.size(); i++) {
				if(popCopy.get(i).getFitness() > fitnessMax) {
					fitnessMax = popCopy.get(i).getFitness();
					indexMax = i;
				}
			}
			// add the biggest to elites, remove that from the copy
			elites.add(popCopy.get(i));
			popCopy.remove(popCopy.get(i));
		}
		return elites;
	}

	@Override
	public String[][] generateRules(SLDescription sl, ElapsedCpuTimer time) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * 
	 */
	
	
}
