package ontology.sprites;

import core.VGDLRegistry;
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
		this.init(position, size);

		// Specific class default parameter values.
		loadDefaults();

		// Parse the arguments.
		this.parseParameters(cnt);
	}

	public void postProcess() {
		super.postProcess();
	}

	protected void loadDefaults() {
		super.loadDefaults();
		color = Types.GRAY;
		is_static = true;
	}

	public VGDLSprite copy() {
		Immovable newSprite = new Immovable();
		this.copyTo(newSprite);
		return newSprite;
	}

	public void copyTo(VGDLSprite target) {
		Immovable targetSprite = (Immovable) target;
		super.copyTo(targetSprite);
	}
}
