package ontology.sprites.producer;

import core.VGDLSprite;
import core.content.SpriteContent;
import ontology.Types;
import tools.Vector2d;

import java.awt.*;

/**
 * Created with IntelliJ IDEA. User: Diego Date: 21/10/13 Time: 18:26 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Bomber extends SpawnPoint {
	public Bomber() {
	}

	public Bomber(Vector2d position, Dimension size, SpriteContent cnt) {
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
		color = Types.ORANGE;
		is_static = false;
		is_oriented = true;
		orientation = Types.RIGHT.copy();
		is_npc = true;
	}

	@Override
	public VGDLSprite copy() {
		Bomber newSprite = new Bomber();
		copyTo(newSprite);
		return newSprite;
	}

	@Override
	public void copyTo(VGDLSprite target) {
		Bomber targetSprite = (Bomber) target;
		super.copyTo(targetSprite);
	}
}
