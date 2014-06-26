package ontology.sprites;

import core.VGDLSprite;
import core.content.SpriteContent;
import ontology.Types;
import tools.Vector2d;

import java.awt.*;

/**
 * Created with IntelliJ IDEA. User: Diego Date: 17/10/13 Time: 12:39 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Immovable extends VGDLSprite {
	public Immovable() {
	}

	public Immovable(Vector2d position, Dimension size, SpriteContent cnt) {
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
		color = Types.GRAY;
		is_static = true;
	}

	@Override
	public VGDLSprite copy() {
		Immovable newSprite = new Immovable();
		copyTo(newSprite);
		return newSprite;
	}

	@Override
	public void copyTo(VGDLSprite target) {
		Immovable targetSprite = (Immovable) target;
		super.copyTo(targetSprite);
	}
}
