package levelGenerators.constraints;

import java.util.HashMap;

import core.game.GameDescription;
import core.game.GameDescription.SpriteData;

public class AvatarNumberConstraint extends AbstractConstraint{

	public HashMap<String, Integer> numOfObjects;
	public GameDescription gameDescription;
	
	@Override
	public double checkConstraint() {
		int totalAvatars = 0;
		for(SpriteData avatar:gameDescription.getAvatar()){
			if(numOfObjects.containsKey(avatar.name)){
				totalAvatars += numOfObjects.get(avatar.name);
			}
		}
		
		return totalAvatars == 1? 1:0;
	}

}
