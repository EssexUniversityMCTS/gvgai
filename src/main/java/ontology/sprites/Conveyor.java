package ontology.sprites;

import core.VGDLSprite;
import core.content.SpriteContent;
import ontology.Types;
import tools.Vector2d;

import java.awt.*;

/**
 * Created with IntelliJ IDEA. User: Diego Date: 21/10/13 Time: 18:04 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Conveyor extends VGDLSprite {
	public Conveyor() {
	}

	public Conveyor(Vector2d position, Dimension size, SpriteContent cnt) {
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
		is_static = true;
		color = Types.BLUE;
		strength = 1;
		draw_arrow = true;
		is_oriented = true;
	}

	@Override
	public VGDLSprite copy() {
		Conveyor newSprite = new Conveyor();
		copyTo(newSprite);
		return newSprite;
	}

	@Override
	public void copyTo(VGDLSprite target) {
		Conveyor targetSprite = (Conveyor) target;
		super.copyTo(targetSprite);
	}
}
