package levelGenerators.constraints;

import ontology.Types;
import ontology.Types.WINNER;

public class DeathConstraint extends AbstractConstraint{

	public double doNothingSteps;
	public double minDoNothingSteps;
	public Types.WINNER doNothingState;
	
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
