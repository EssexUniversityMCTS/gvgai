package levelGenerators.constraints;

import java.util.HashMap;

import core.game.GameDescription;
import core.game.GameDescription.SpriteData;
import tools.GameAnalyzer;

public class AvatarNumberConstraint extends AbstractConstraint{

	public HashMap<String, Integer> numOfObjects;
	public GameAnalyzer gameAnalyzer;
	
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
