package ontology.effects.unary;

import core.VGDLSprite;
import core.content.InteractionContent;
import core.game.Game;
import ontology.effects.Effect;

import java.util.Iterator;

/**
 * Created with IntelliJ IDEA. User: Diego Date: 23/10/13 Time: 15:23 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class UndoAll extends Effect {
	public UndoAll(InteractionContent cnt) {
		parseParameters(cnt);
	}

	@Override
	public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game) {
		int[] gameSpriteOrder = game.getSpriteOrder();
		int spriteOrderCount = gameSpriteOrder.length;
		for (int spriteTypeInt : gameSpriteOrder) {
			Iterator<VGDLSprite> spriteIt = game.getSpriteGroup(spriteTypeInt);
			if (null != spriteIt)
				while (spriteIt.hasNext()) {
					VGDLSprite sp = spriteIt.next();
					sp.setRect(sp.lastrect);
				}
		}
	}
}
