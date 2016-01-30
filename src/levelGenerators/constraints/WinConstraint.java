package levelGenerators.constraints;

import levelGenerators.geneticLevelGenerator.SharedData;
import ontology.Types;
import ontology.Types.WINNER;

public class WinConstraint extends AbstractConstraint {

	public Types.WINNER bestPlayer;
	
	@Override
	public double checkConstraint() {
		double result = 0;
		if(bestPlayer == WINNER.PLAYER_WINS){
			result += 1;
		}
		if(bestPlayer == WINNER.NO_WINNER){
			result += SharedData.DRAW_FITNESS;
		}
		return result;
	}
}
