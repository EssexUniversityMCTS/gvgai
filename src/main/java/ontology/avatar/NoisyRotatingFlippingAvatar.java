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
		this.init(position, size);

		// Specific class default parameter values.
		loadDefaults();

		// Parse the arguments.
		this.parseParameters(cnt);
	}

	protected void loadDefaults() {
		super.loadDefaults();
		noiseLevel = 0.1;
	}

	public VGDLSprite copy() {
		NoisyRotatingFlippingAvatar newSprite = new NoisyRotatingFlippingAvatar();
		this.copyTo(newSprite);
		return newSprite;
	}

	public void copyTo(VGDLSprite target) {
		NoisyRotatingFlippingAvatar targetSprite = (NoisyRotatingFlippingAvatar) target;
		super.copyTo(targetSprite);
	}

}
