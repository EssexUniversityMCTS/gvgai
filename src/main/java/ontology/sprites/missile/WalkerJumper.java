package ontology.sprites.missile;

import core.VGDLSprite;
import core.content.SpriteContent;
import tools.Vector2d;

import java.awt.*;

/**
 * Created with IntelliJ IDEA. User: Diego Date: 21/10/13 Time: 18:17 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class WalkerJumper extends Walker {
	public double probability;

	public WalkerJumper() {
	}

	public WalkerJumper(Vector2d position, Dimension size, SpriteContent cnt) {
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
		probability = 0.1;
		strength = 10;
	}

	@Override
	public VGDLSprite copy() {
		WalkerJumper newSprite = new WalkerJumper();
		copyTo(newSprite);
		return newSprite;
	}

	@Override
	public void copyTo(VGDLSprite target) {
		WalkerJumper targetSprite = (WalkerJumper) target;
		targetSprite.probability = probability;
		super.copyTo(targetSprite);
	}

}
