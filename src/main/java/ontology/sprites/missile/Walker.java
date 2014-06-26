package ontology.sprites.missile;

import core.VGDLSprite;
import core.content.SpriteContent;
import tools.Vector2d;

import java.awt.*;

/**
 * Created with IntelliJ IDEA. User: Diego Date: 21/10/13 Time: 18:16 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Walker extends Missile {
	public boolean airsteering;

	public Walker() {
	}

	public Walker(Vector2d position, Dimension size, SpriteContent cnt) {
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
		airsteering = false;
		is_stochastic = true;
	}

	@Override
	public VGDLSprite copy() {
		Walker newSprite = new Walker();
		copyTo(newSprite);
		return newSprite;
	}

	@Override
	public void copyTo(VGDLSprite target) {
		Walker targetSprite = (Walker) target;
		targetSprite.airsteering = airsteering;
		super.copyTo(targetSprite);
	}
}
