package core.optimization.ucbOptimization;

import tracks.singlePlayer.tools.ucbOptimizerAgent.Helper;

/**
 * implementation of the evolved ucb equations from
 * Evolving game-specific UCB alternatives for General Video Game Playing
 * @author AhmedKhalifa
 */
public class UCBEvoEquation implements UCBEquation {
	/**
	 * small value to avoid division by zero
	 */
	public static double epsilon = 1e-6;
	
	/**
	 * get the number of parameters used in this equation
	 * @return number of ucb parameters (14)
	 */
	@Override
	public int lengthParameters() {
		return 14;
	}
	/**
	 * evaluate the ucb equation using the current parameters at the current state
	 * @param values		state values
	 * @param parameters	ucb parameters
	 * @return				ucb equation result
	 */
	@Override
	public double evaluate(double[] values, double[] parameters) {
		if(parameters.length < this.lengthParameters()){
			double[] temp = new double[this.lengthParameters()];
			for(int i=0; i<parameters.length; i++){
				temp[i] = parameters[i];
			}
			parameters = temp;
		}
		
        double uctValue = 
        		parameters[0] * (values[Helper.TREE_CHILD_VALUE] / (values[Helper.TREE_CHILD_VISITS] + this.epsilon)) + 
        		parameters[1] * values[Helper.TREE_CHILD_MAX_VALUE] + 
        		parameters[2] * Math.pow(Math.log(values[Helper.TREE_PARENT_VISITS])/values[Helper.TREE_CHILD_VISITS] + this.epsilon, parameters[3]) +
        		parameters[4] * Math.pow(values[Helper.SPACE_EXPLORATION_VALUE] + this.epsilon, parameters[5]) + 
        		parameters[6] * Math.pow(values[Helper.DISTANCE_MIN_NPC] + this.epsilon, parameters[7]) +
        		parameters[8] * Math.pow(values[Helper.DISTANCE_MIN_PORTAL] + this.epsilon, parameters[9]) +
        		parameters[10] * Math.pow(values[Helper.DISTANCE_MIN_MOVABLE] + this.epsilon, parameters[11]) +
        		parameters[12] * Math.pow(values[Helper.DISTANCE_MIN_RESOURCE] + this.epsilon, parameters[13]);
		
		return uctValue;
	}
	/**
	 * print the ucb equation using the current parameters
	 * @param parameters	ucb parameters
	 * @return				the ucb equation
	 */
	@Override
	public String toString(double[] parameters) {
		if(parameters.length < this.lengthParameters()){
			double[] temp = new double[this.lengthParameters()];
			for(int i=0; i<parameters.length; i++){
				temp[i] = parameters[i];
			}
			parameters = temp;
		}
		double[] temp = new double[parameters.length];
		boolean[] term = new boolean[parameters.length];
		for(int i=0; i<parameters.length; i++){
			temp[i] = (int)parameters[i] + ((int)((parameters[i] - (int)parameters[i]) * 1000)) / 1000.0;
			term[i] = !(Math.abs(temp[i]) <= this.epsilon);
		}
		
		String result = "UCB = " +
				(term[0]?(temp[0] + " * average(X(j)) + "):"") + 
				(term[1]?(temp[1] + " * max(X(j)) + "):"") + 
				(term[2]?(temp[2] + " * (ln(n)/n(j))^" + temp[3] + " + "):"") +
				(term[4]?(temp[4] + " * E(x,y)^" + temp[5] + " + "):"") + 
				(term[6]?(temp[6] + " * min(D(npc))^" +  temp[7] + " + "):"") +
				(term[8]?(temp[8] + " * min(D(portal))^" + temp[9] + " + "):"") +
				(term[10]?(temp[10] + " * min(D(movable))^" + temp[11] + " + "):"") +
				(term[12]?(temp[12] + " * min(D(resource))^" + temp[13] + " + "):"");
		result = result.substring(0, result.length() - 3);
		return result;
	}

}
