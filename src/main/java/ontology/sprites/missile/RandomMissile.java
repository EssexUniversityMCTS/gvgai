package ontology.sprites.missile;

import core.VGDLSprite;
import core.content.SpriteContent;
import core.game.Game;
import ontology.Types;
import tools.Utils;
import tools.Vector2d;

import java.awt.*;

/**
 * Created with IntelliJ IDEA. User: Diego Date: 21/10/13 Time: 18:18 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class RandomMissile extends Missile {
	public RandomMissile() {
	}

	public RandomMissile(Vector2d position, Dimension size, SpriteContent cnt) {
		// Init the sprite
		init(position, size);

		// Specific class default parameter values.
		loadDefaults();

		// Parse the arguments.
		parseParameters(cnt);
	}

	@Override
	protected void loadDefaults() {
		super.loadDefaults();
		orientation = Types.NIL;
	}

	@Override
	public void update(Game game) {
		if (orientation == Types.NIL) {
			orientation = (Vector2d) Utils.choice(Types.BASEDIRS,
					game.getRandomGenerator());
		}
		updatePassive();
	}

	@Override
	public VGDLSprite copy() {
		RandomMissile newSprite = new RandomMissile();
		copyTo(newSprite);
		return newSprite;
	}

	@Override
	public void copyTo(VGDLSprite target) {
		RandomMissile targetSprite = (RandomMissile) target;
		super.copyTo(targetSprite);
	}
}
