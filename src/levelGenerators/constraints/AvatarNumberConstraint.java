package levelGenerators.constraints;

import java.util.HashMap;
import tools.GameAnalyzer;

public class AvatarNumberConstraint extends AbstractConstraint{

	/**
	 * hashmap contains the number of objects for each type
	 */
	public HashMap<String, Integer> numOfObjects;
	/**
	 * Object for game analyzer
	 */
	public GameAnalyzer gameAnalyzer;
	
	/**
	 * Check if there is only 1 avatar in the level
	 * @return	1 if constraint is staisfied and 0 otherwise
	 */
	@Override
	public double checkConstraint() {
		int totalAvatars = 0;
		for(String avatar:gameAnalyzer.getAvatarSprites()){
			if(numOfObjects.containsKey(avatar)){
				totalAvatars += numOfObjects.get(avatar);
			}
		}
		
		return totalAvatars == 1? 1:0;
	}

}
