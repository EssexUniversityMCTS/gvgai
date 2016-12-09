package ruleGenerators.geneticRuleGenerator;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;

import core.game.SLDescription;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import levelGenerators.constraints.CombinedConstraints;
import levelGenerators.geneticLevelGenerator.SharedData;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.StepController;

public class Chromosome implements Comparable<Chromosome>{
	private ArrayList<String>[][] level;
	/**
	 * current chromosome fitness if its a feasible
	 */
	private ArrayList<Double> fitness;
	/**
	 * Parameters to initialize constraints
	 */
	HashMap<String, Object> parameters;
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
	
	private int doNothingLength;
	StateObservation doNothingState;
	StateObservation bestState;
	ArrayList<Types.ACTIONS> bestSol;
	
	/**
	 * Chromosome constructor.  Holds the ruleset and initializes agents within
	 * @param ruleset	the ruleset the chromosome contains
	 * @param sl		the game description
	 * @param time		elapsed time
	 */
	
	public Chromosome(String[][] ruleset, SLDescription sl, ElapsedCpuTimer time) {
		this.ruleset = ruleset;
		this.sl = sl;
		this.fitness = new ArrayList<Double>();
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
		doNothingLength = Integer.MAX_VALUE;
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
		//calculate the constrain fitness by applying all different constraints
		//Updating parameters
		parameters.put("solutionLength", bestSol.size());
		parameters.put("minSolutionLength", SharedData.MIN_SOLUTION_LENGTH);
		parameters.put("doNothingSteps", doNothingLength);
		parameters.put("doNothingState", doNothingState.getGameWinner());
		parameters.put("bestPlayer", bestState.getGameWinner());
		parameters.put("minDoNothingSteps", SharedData.MIN_DOTHING_STEPS);
		//parameters.put("coverPercentage", coverPercentage);
		//parameters.put("minCoverPercentage", SharedData.MIN_COVER_PERCENTAGE);
		//parameters.put("maxCoverPercentage", SharedData.MAX_COVER_PERCENTAGE);
		//parameters.put("numOfObjects", calculateNumberOfObjects());
		parameters.put("gameAnalyzer", SharedData.gameAnalyzer);
		parameters.put("gameDescription", SharedData.gameDescription);
		
		CombinedConstraints constraint = new CombinedConstraints();
		constraint.addConstraints(new String[]{"SolutionLengthConstraint", "DeathConstraint", 
											   //"CoverPercentageConstraint", "SpriteNumberConstraint",
											   "GoalConstraint", "AvatarNumberConstraint", "WinConstraint"});
		constraint.setParameters(parameters);
		constrainFitness = constraint.checkConstraint();
		
		//System.out.println("SolutionLength:" + bestSol.size() + " doNothingSteps:" + doNothingLength + " coverPercentage:" + coverPercentage + " bestPlayer:" + bestState.getGameWinner());

	}
	
	/**
	 * calculates the fitness, by comparing the scores of a naiveAI and a smart AI
	 * @param time	how much time to evaluate the chromosome
	 */
	public void calculateFitness(long time) {
		//Play the game using the best agent
		StepController stepAgent = new StepController(automatedAgent, SharedData.EVALUATION_STEP_TIME);
		ElapsedCpuTimer elapsedTimer = new ElapsedCpuTimer();
		elapsedTimer.setMaxTimeMillis(time);
		stepAgent.playGame(stateObs.copy(), elapsedTimer);
		
		bestState = stepAgent.getFinalState();
		bestSol = stepAgent.getSolution();
		
		doNothingState = null;
		int doNothingLength = Integer.MAX_VALUE;
		//playing the game using the donothing agent and naive agent
		for(int i=0; i<SharedData.REPETITION_AMOUNT; i++){
			StateObservation tempState = stateObs.copy();
			int temp = getNaivePlayerResult(tempState, bestSol.size(), doNothingAgent);
			if(temp < doNothingLength){
				doNothingLength = temp;
				doNothingState = tempState;
			}
		}
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
	
	public ArrayList<Double> getFitness() {
		return fitness;
	}
	/**
	 * Get constraint fitness for infeasible chromosome
	 * @return	1 if its feasible and less than 1 if not
	 */
	public double getConstrainFitness(){
		return constrainFitness;
	}
	/**
	 * Compare two chromosome with each other based on their 
	 * constrained fitness and normal fitness
	 */
	@Override
	public int compareTo(Chromosome o) {
		if(this.constrainFitness < 1 || o.constrainFitness < 1){
			if(this.constrainFitness < o.constrainFitness){
				return 1;
			}
			if(this.constrainFitness > o.constrainFitness){
				return -1;
			}
			return 0;
		}
		
		double firstFitness = 0;
		double secondFitness = 0;
		for(int i=0; i<this.fitness.size(); i++){
			firstFitness += this.fitness.get(i);
			secondFitness += o.fitness.get(i);
		}
		
		if(firstFitness > secondFitness){
			return -1;
		}
		
		if(firstFitness < secondFitness){
			return 1;
		}
		
		return 0;
	}
	
	/**
	 * crossover the current chromosome with the input chromosome
	 * @param c	the other chromosome to crossover with
	 * @return	the current children from the crossover process
	 */
	public ArrayList<Chromosome> crossOverIteraction(Chromosome c){
		ArrayList<Chromosome> children = new ArrayList<Chromosome>();
		children.add(new Chromosome(ruleset.clone(), sl, time));
		children.add(new Chromosome(ruleset.clone(), sl, time));

		//crossover point
		int pointY = SharedData.random.nextInt(ruleset.length);
		int pointX = SharedData.random.nextInt(ruleset[0].length);
		
		//swap the two chromosomes around this point
		for(int y = 0; y < ruleset[0].length; y++){
			for(int x = 0; x < level[y].length; x++){
				if(y < pointY){
					children.get(0).level[y][x].addAll(this.level[y][x]);
					children.get(1).level[y][x].addAll(c.level[y][x]);
				}
				else if(y == pointY){
					if(x <= pointX){
						children.get(0).level[y][x].addAll(this.level[y][x]);
						children.get(1).level[y][x].addAll(c.level[y][x]);
					}
					else{
						children.get(0).level[y][x].addAll(c.level[y][x]);
						children.get(1).level[y][x].addAll(this.level[y][x]);
					}
				}
				else{
					children.get(0).level[y][x].addAll(c.level[y][x]);
					children.get(1).level[y][x].addAll(this.level[y][x]);
				}
			}
		}
		return children;
	}
	
	/**
	 * Get the average value of the fitness
	 * @return	average value of the fitness array
	 */
	public double getCombinedFitness(){
		double result = 0;
		for(double v: this.fitness){
			result += v;
		}
		return result / this.fitness.size();
	}
}
