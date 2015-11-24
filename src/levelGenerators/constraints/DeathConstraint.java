package levelGenerators.constraints;

import ontology.Types;
import ontology.Types.WINNER;

public class DeathConstraint extends AbstractConstraint{

	public Types.WINNER bestPlayer;
	public double doNothingSteps;
	public double minDoNothingSteps;
	
	@Override
	public double checkConstraint() {
		double result = 0;
		if(bestPlayer == WINNER.NO_WINNER){
			result += 0.5;
		}
		if(doNothingSteps >= minDoNothingSteps){
			result += 0.5;
		}
		return result;
	}

}
