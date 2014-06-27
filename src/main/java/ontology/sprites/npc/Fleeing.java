package ontology.sprites.npc;

import core.VGDLSprite;
import core.content.SpriteContent;
import tools.Vector2d;

import java.awt.*;

/**
 * Created with IntelliJ IDEA. User: Diego Date: 21/10/13 Time: 18:33 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Fleeing extends Chaser {
	public Fleeing() {
	}

	public Fleeing(Vector2d position, Dimension size, SpriteContent cnt) {
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
		fleeing = true;
	}

	@Override
	public VGDLSprite copy() {
		Fleeing newSprite = new Fleeing();
		copyTo(newSprite);
		return newSprite;
	}

	@Override
	public void copyTo(VGDLSprite target) {
		Fleeing targetSprite = (Fleeing) target;
		super.copyTo(targetSprite);
	}
}
