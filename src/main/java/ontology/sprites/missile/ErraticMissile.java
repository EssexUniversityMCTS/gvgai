package ontology.sprites.missile;

import core.VGDLSprite;
import core.content.SpriteContent;
import tools.Vector2d;

import java.awt.*;

/**
 * Created with IntelliJ IDEA. User: Diego Date: 21/10/13 Time: 18:19 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class ErraticMissile extends Missile {
	public ErraticMissile() {
	}

	public ErraticMissile(Vector2d position, Dimension size, SpriteContent cnt) {
		// Init the sprite
		init(position, size);

		// Specific class default parameter values.
		loadDefaults();

		// Parse the arguments.
		parseParameters(cnt);

		System.out
				.println("WARNING: ErraticMissile.java, this class must set prob value, "
						+ "and is_stochastic must be adjusted according to the value in the parameters is_stochastic=(>0 && <1)");
	}

	@Override
	protected void loadDefaults() {
		super.loadDefaults();
	}

	@Override
	public VGDLSprite copy() {
		ErraticMissile newSprite = new ErraticMissile();
		copyTo(newSprite);
		return newSprite;
	}

	@Override
	public void copyTo(VGDLSprite target) {
		ErraticMissile targetSprite = (ErraticMissile) target;
		super.copyTo(targetSprite);
	}
}
