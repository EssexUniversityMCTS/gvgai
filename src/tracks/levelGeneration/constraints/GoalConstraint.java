package tracks.levelGeneration.constraints;

import java.util.HashMap;

import core.game.GameDescription;
import core.game.GameDescription.TerminationData;

public class GoalConstraint extends AbstractConstraint{


	/**
	 * hashmap for number of objects in the level
	 */
	public HashMap<String, Integer> numOfObjects;
	/**
	 * Game description object send by the system
	 */
	public GameDescription gameDescription;
	
	/**
	 * Check if all terminate conditions are unsatisfiable
	 * @return	return 1 if all conditions are unsatisfiable and percentage
	 * 			of unsatisfied conditions otherwise
	 */
	@Override
	public double checkConstraint() {
		double result = 0;
		int acheived = 0;
		for(TerminationData t:gameDescription.getTerminationConditions()){
			for(String s:t.sprites){
				acheived += numOfObjects.get(s);
			}
			
			if(t.type.equals("SpriteCounter")){
				if(acheived > t.limit){
					result += 1;
				}
			}
			else if(t.type.equals("MultiSpriteCounter")){
				if(acheived != t.limit){
					result += 1;
				}
			}
		}
		
		return result / gameDescription.getTerminationConditions().size();
	}
	
}
