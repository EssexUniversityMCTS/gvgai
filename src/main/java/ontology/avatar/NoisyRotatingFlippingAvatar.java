package ontology.avatar;

import core.VGDLSprite;
import core.content.SpriteContent;
import tools.Vector2d;

import java.awt.*;

/**
 * Created with IntelliJ IDEA. User: Diego Date: 22/10/13 Time: 18:07 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class NoisyRotatingFlippingAvatar extends RotatingFlippingAvatar {
	public NoisyRotatingFlippingAvatar() {
	}

	public NoisyRotatingFlippingAvatar(Vector2d position, Dimension size,
			SpriteContent cnt) {
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
		noiseLevel = 0.1;
	}

	@Override
	public VGDLSprite copy() {
		NoisyRotatingFlippingAvatar newSprite = new NoisyRotatingFlippingAvatar();
		copyTo(newSprite);
		return newSprite;
	}

	@Override
	public void copyTo(VGDLSprite target) {
		NoisyRotatingFlippingAvatar targetSprite = (NoisyRotatingFlippingAvatar) target;
		super.copyTo(targetSprite);
	}

}
