package core.optimization.ucbOptimization;

import java.util.Random;

import core.ArcadeMachine;
import core.optimization.OptimizationObjective;

public class UCBOptimization implements OptimizationObjective {
	private String[] gamePaths;
	private String[] levelPaths;
	private int repetition;
	
	public UCBOptimization(String[] gamePaths, String[] levelPaths, int repetition, UCBEquation ucb){
		this.gamePaths = gamePaths;
		this.levelPaths = levelPaths;
		this.repetition = repetition;
		controllers.singlePlayer.ucbOptimizerAgent.Agent.ucb = ucb;
	}
	
	@Override
	public int getNumberOfParameters() {
		return controllers.singlePlayer.ucbOptimizerAgent.Agent.ucb.lengthParameters();
	}

	@Override
	public int getNumberOfObjectives() {
		return this.gamePaths.length;
	}

	@Override
	public double[] evaluate(double[] parameters) {
		controllers.singlePlayer.ucbOptimizerAgent.Agent.parameters = parameters;
		
		double[] results = new double[this.getNumberOfObjectives()];
		for(int i=0; i<this.gamePaths.length; i++){
			double total = 0;
			for(int j=0; j<this.repetition; j++){
				double[] gameResults = null;
				do{
					gameResults = ArcadeMachine.runOneGame(this.gamePaths[i], this.levelPaths[i], false, 
							"controllers.singlePlayer.ucbOptimizerAgent.Agent", null, new Random().nextInt(), 0);
				}while(gameResults[0] < -10);
				
				total += Math.max(gameResults[0], 0) * 1000 + gameResults[1];
			}
			results[i] = total / this.repetition;
		}
		
		return results;
	}

}
