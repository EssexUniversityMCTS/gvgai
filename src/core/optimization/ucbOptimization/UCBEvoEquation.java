package core.optimization.ucbOptimization;

import controllers.singlePlayer.ucbOptimizerAgent.Helper;

public class UCBEvoEquation implements UCBEquation {
	public static double epsilon = 1e-6;
	
	@Override
	public int lengthParameters() {
		return 14;
	}

	@Override
	public double evaluate(double[] values, double[] parameters) {
		if(parameters.length < 14){
			double[] temp = new double[14];
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

}
