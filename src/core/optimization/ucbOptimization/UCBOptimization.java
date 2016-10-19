package core.optimization.ucbOptimization;

import java.util.Random;

import core.ArcadeMachine;
import core.optimization.OptimizationObjective;

/**
 * an implementation of optimization objective for any UCBEquation
 * @author AhmedKhalifa
 */
public class UCBOptimization implements OptimizationObjective {
	/**
	 * all games paths require to test against
	 */
	private String[] gamePaths;
	/**
	 * all level paths associated with the games
	 */
	private String[] levelPaths;
	/**
	 * number of repetition to play each game
	 */
	private int repetition;
	/**
	 * the maximum allowed number of evaluation calls
	 */
	private int numberOfEvaluation;
	
	/**
	 * Constructor for the current ucb optimization objective
	 * @param gamePaths		all game paths require to test against
	 * @param levelPaths	all level paths associated with the games
	 * @param repetition	number of repetition to play each game
	 * @param evaluation	the maximum number of calls allowed for the evaluation function
	 * @param ucb			the current ucb equation to optimize
	 */
	public UCBOptimization(String[] gamePaths, String[] levelPaths, int repetition, int evaluation, UCBEquation ucb){
		this.gamePaths = gamePaths;
		this.levelPaths = levelPaths;
		this.repetition = repetition;
		this.numberOfEvaluation = evaluation;
		controllers.singlePlayer.ucbOptimizerAgent.Agent.ucb = ucb;
	}
	
	/**
	 * small value to avoid division by zero
	 */
	@Override
	public int getNumberOfParameters() {
		return controllers.singlePlayer.ucbOptimizerAgent.Agent.ucb.lengthParameters();
	}

	/**
	 * get the number of parameters used in this equation
	 * @return number of ucb parameters (14)
	 */
	@Override
	public int getNumberOfObjectives() {
		return this.gamePaths.length;
	}

	/**
	 * evaluate the current parameters against the target objectives
	 * @param parameters	the current set of parameters to test
	 * @return				array of fitness against all objectives (the higher the better), 
	 * 						null if you exceed the number of allowed evaluations
	 */
	@Override
	public double[] evaluate(double[] parameters) {
		if(this.numberOfEvaluation <= 0){
			return null;
		}
		this.numberOfEvaluation -= 1;
		
		controllers.singlePlayer.ucbOptimizerAgent.Agent.parameters = parameters;
		
		double[] results = new double[this.getNumberOfObjectives()];
		for(int i=0; i<this.gamePaths.length; i++){
			double totalWins = 0;
			double totalScore = 0;
			for(int j=0; j<this.repetition; j++){
				double[] gameResults = null;
				do{
					gameResults = ArcadeMachine.runOneGame(this.gamePaths[i], this.levelPaths[i], false, 
							"controllers.singlePlayer.ucbOptimizerAgent.Agent", null, new Random().nextInt(), 0);
				}while(gameResults[0] < -10);
				
				totalWins += Math.max(gameResults[0], 0);
				totalScore += gameResults[1];
			}
			results[i] = 1000 * totalWins / this.repetition + totalScore / this.repetition;
		}
		
		return results;
	}

}
