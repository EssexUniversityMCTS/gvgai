package ontology.effects.unary;

import core.VGDLSprite;
import core.content.InteractionContent;
import core.game.Game;
import ontology.effects.Effect;

/**
 * Created with IntelliJ IDEA. User: Diego Date: 23/10/13 Time: 15:21 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class WrapAround extends Effect {

	public double offset;

	public WrapAround(InteractionContent cnt) {
		parseParameters(cnt);
	}

	@Override
	public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game) {

		if (0 < sprite1.orientation.x) {
			sprite1.rect.x = (int) (offset * sprite1.rect.width);
		} else if (0 > sprite1.orientation.x) {
			sprite1.rect.x = (int) (game.getScreenSize().width - sprite1.rect.width
					* (1 + offset));
		} else if (0 < sprite1.orientation.y) {
			sprite1.rect.y = (int) (offset * sprite1.rect.height);
		} else if (0 > sprite1.orientation.y) {
			sprite1.rect.y = (int) (game.getScreenSize().height - sprite1.rect.height
					* (1 + offset));
		}

		sprite1.lastmove = 0;
	}
}
