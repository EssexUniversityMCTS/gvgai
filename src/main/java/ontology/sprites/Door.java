package ontology.sprites;

import core.VGDLSprite;
import core.content.SpriteContent;
import tools.Vector2d;

import java.awt.*;

/**
 * Created by diego on 17/02/14.
 */
public class Door extends Immovable {
	public Door() {
	}

	public Door(Vector2d position, Dimension size, SpriteContent cnt) {
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
		portal = true;
	}

	@Override
	public VGDLSprite copy() {
		Door newSprite = new Door();
		copyTo(newSprite);
		return newSprite;
	}

	@Override
	public void copyTo(VGDLSprite target) {
		Door targetSprite = (Door) target;
		super.copyTo(targetSprite);
	}

}
