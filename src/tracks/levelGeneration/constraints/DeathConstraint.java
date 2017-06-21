package tracks.levelGeneration.constraints;

import ontology.Types;
import ontology.Types.WINNER;

public class DeathConstraint extends AbstractConstraint{


	/**
	 * number of steps that do nothing agent did so far
	 */
	public double doNothingSteps;
	/**
	 * the minimum acceptable moves for the do nothing
	 */
	public double minDoNothingSteps;
	/**
	 * the end state of the do nothing algorithm
	 */
	public Types.WINNER doNothingState;
	
	/**
	 * Check if the do nothing player staisfy the minDoNothingSteps and didn't win
	 * @return	1 if the do nothing player didn't win and statisfy the minDoNothingSteps
	 * 			while percentage of statisfying otherwise
	 */
	@Override
	public double checkConstraint() {
		double result = 0.75 * doNothingSteps / minDoNothingSteps;
		if(result >= 0.75){
			result = 0.75;
		}
		if(doNothingState != WINNER.PLAYER_WINS){
			result += 0.25;
		}
		return result;
	}

}
