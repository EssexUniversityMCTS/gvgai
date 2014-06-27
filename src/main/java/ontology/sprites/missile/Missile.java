package ontology.sprites.missile;

import core.VGDLSprite;
import core.content.SpriteContent;
import tools.Vector2d;

import java.awt.*;

/**
 * Created with IntelliJ IDEA. User: Diego Date: 21/10/13 Time: 17:35 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Missile extends VGDLSprite {
	public Missile() {
	}

	public Missile(Vector2d position, Dimension size, SpriteContent cnt) {
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
		speed = 1;
		is_oriented = true;
	}

	@Override
	public VGDLSprite copy() {
		Missile newSprite = new Missile();
		copyTo(newSprite);
		return newSprite;
	}

	@Override
	public void copyTo(VGDLSprite target) {
		Missile targetSprite = (Missile) target;
		super.copyTo(targetSprite);
	}
}
