package ontology.effects.unary;

import core.VGDLRegistry;
import core.VGDLSprite;
import core.content.InteractionContent;
import core.game.Game;
import ontology.effects.Effect;

/**
 * Created by Diego on 18/02/14.
 */
public class SpawnIfHasMore extends Effect {

	public String resource;
	public int resourceId;
	public int limit;
	public String stype;
	public int itype;

	public SpawnIfHasMore(InteractionContent cnt) {
		resourceId = -1;
		parseParameters(cnt);
		resourceId = VGDLRegistry.GetInstance().getRegisteredSpriteValue(
				resource);
		itype = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stype);
	}

	@Override
	public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game) {
		if (sprite1.getAmountResource(resourceId) >= limit) {
			game.addSprite(itype, sprite1.getPosition());
		}
	}
}
