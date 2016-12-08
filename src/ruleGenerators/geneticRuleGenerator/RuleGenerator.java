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
	private ArrayList<AbstractRuleGenerator> population;
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
		initPop();
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
	public void initPop() {
		population = new ArrayList<AbstractRuleGenerator>();
		for(int i = 0; i < POP_SIZE / 2; i++) {
			randomGen = new ruleGenerators.randomRuleGenerator.RuleGenerator(sl, time);
			population.add(randomGen);
			constructGen = new ruleGenerators.constructiveRuleGenerator.RuleGenerator(sl, time);
			population.add(constructGen);
		}
	}

	/**
	 * Mutate random interaction rules and termination conditions
	 * @param sl	contains information about sprites and current level
	 * @param time	amount of time allowed
	 */
	@Override
	public String[][] generateRules(SLDescription sl, ElapsedCpuTimer time) {

	return null;
	}
	
	/**
	 * The first fitness function. If rules are feasible 
	 * (and a do nothing agent can survive the first 40 moves) then this returns true
	 * @param ruleset
	 * @return
	 */
	public boolean feasibleTest(SLDescription sl, String[][] ruleset) {
		// test ruleset for robustness
		
		
		// set elapsed time to 1 second.  If doNothing loses within a second, this is infeasible
		ElapsedCpuTimer elapsedTimer = new ElapsedCpuTimer();
		elapsedTimer.setMaxTimeMillis(1000);
		StateObservation sO = sl.testRules(ruleset[0], ruleset[1]);
		controllers.singlePlayer.doNothing.Agent doNothing = new controllers.singlePlayer.doNothing.Agent(sO, elapsedTimer);
		if(sO.getGameWinner() == Types.WINNER.PLAYER_LOSES) {
			return false;
		}
		return true;
	}
}
