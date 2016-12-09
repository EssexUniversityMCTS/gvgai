package ruleGenerators.geneticRuleGenerator;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import core.game.SLDescription;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import levelGenerators.geneticLevelGenerator.SharedData;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.LevelMapping;

public class Chromosome {
	private ArrayList<String>[][] level;
	/**
	 * current chromosome fitness if its a feasible
	 */
	private double fitness;
	/**
	 * current chromosome fitness if its an infeasible
	 */
	private double constrainFitness;
	/**
	 * if the fitness is calculated before (no need to recalculate)
	 */
	private boolean calculated;
	/**
	 * the best automated agent
	 */
	private AbstractPlayer automatedAgent;
	/**
	 * the naive automated agent
	 */
	private AbstractPlayer naiveAgent;
	/**
	 * the do nothing automated agent
	 */
	private AbstractPlayer doNothingAgent;
	/**
	 * The current stateObservation of the level
	 */
	private StateObservation stateObs;
	/**
	 * the ruleset this chromosome contains
	 */
	private String[][] ruleset;
	/**
	 * the SL description of this chromosome
	 */
	private SLDescription sl;
	/**
	 * elapsed time
	 */
	private ElapsedCpuTimer time;
	/**
	 * amount of steps allowed for the naive agent to sit around
	 */
	private int FEASIBILITY_STEP_LIMIT = 40;
	/**
	 * amount of times to repeat the feasibility test
	 */
	private int REPITITON_AMOUNT = 100;
	
	
	/**
	 * Chromosome constructor.  Holds the ruleset and initializes agents within
	 * @param ruleset	the ruleset the chromosome contains
	 * @param sl		the game description
	 * @param time		elapsed time
	 */
	
	public Chromosome(String[][] ruleset, SLDescription sl, ElapsedCpuTimer time) {
		this.ruleset = ruleset;
		this.sl = sl;
		this.fitness = 0;
		this.calculated = false;
		this.stateObs = null;
		
		constructAgent();
	}
	
	/**
	 * mutates the ruleset
	 */
	public void mutate() {
		
	}
	
	/**
	 * tests to make sure the game is playable, meaning a DoNothing AI won't die in the first 40 frames.
	 */
	public boolean feasibilityTest() {
		int doNothingLength = Integer.MAX_VALUE;
		for(int i = 0; i < 100; i++) {
			int temp = this.getNaivePlayerResult(getStateObservation().copy(), FEASIBILITY_STEP_LIMIT, this.naiveAgent);
			if(temp < doNothingLength){
				doNothingLength = temp;
			}
		}
		if(doNothingLength < 39) {
			return false;
		}
		return true;
	}
	
	/**
	 * tests to make sure the game is playable, meaning that none of the rules will break the game.
	 */
	public void constraintsTest() {
		
	}
	
	/**
	 * calculates the fitness, by comparing the scores of a naiveAI and a smart AI
	 */
	public void fitnessCalculation() {
		
	}
	
	/**
	 * initialize the agents used during evaluating the chromosome
	 */
	private void constructAgent(){
		try{
			Class agentClass = Class.forName(SharedData.AGENT_NAME);
			Constructor agentConst = agentClass.getConstructor(new Class[]{StateObservation.class, ElapsedCpuTimer.class});
			automatedAgent = (AbstractPlayer)agentConst.newInstance(getStateObservation().copy(), null);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		try{
			Class agentClass = Class.forName(SharedData.NAIVE_AGENT_NAME);
			Constructor agentConst = agentClass.getConstructor(new Class[]{StateObservation.class, ElapsedCpuTimer.class});
			naiveAgent = (AbstractPlayer)agentConst.newInstance(getStateObservation().copy(), null);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		try{
			Class agentClass = Class.forName(SharedData.NAIVE_AGENT_NAME);
			Constructor agentConst = agentClass.getConstructor(new Class[]{StateObservation.class, ElapsedCpuTimer.class});
			doNothingAgent = (AbstractPlayer)agentConst.newInstance(getStateObservation().copy(), null);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * get game state observation for the current level
	 * @return	StateObservation for the current level
	 */
	private StateObservation getStateObservation(){
		if(stateObs != null){
			return stateObs;
		}
		stateObs = sl.testRules(ruleset[0], ruleset[1]);
		return stateObs;
	}
	
	/**
	 * clone the chromosome data
	 */
	public Chromosome clone(){
		
		// copy ruleset into nRuleset. Two for loops, in case 2d array is jagged
		String[][] nRuleset = new String[ruleset.length][ruleset.length];
		for(int i = 0; i < ruleset[0].length; i++) {
			nRuleset[0][i] = ruleset[0][i];
		}
		for(int i = 0; i < ruleset[1].length; i++) {
			nRuleset[1][i] = ruleset[1][i];
		}
		Chromosome c = new Chromosome(nRuleset, sl, time);
		return c;
	}
	
	/**
	 * Play the current level using the naive player
	 * @param stateObs	the current stateObservation object that represent the level
	 * @param steps		the maximum amount of steps that it shouldn't exceed it
	 * @param agent		current agent to play the level
	 * @return			the number of steps that the agent stops playing after (<= steps)
	 */
	private int getNaivePlayerResult(StateObservation stateObs, int steps, AbstractPlayer agent){
		int i =0;
		for(i=0;i<steps;i++){
			if(stateObs.isGameOver()){
				break;
			}
			Types.ACTIONS bestAction = agent.act(stateObs, null);
			stateObs.advance(bestAction);
		}
		
		return i;
	}
	
	public double getFitness() {
		return fitness;
	}
}
