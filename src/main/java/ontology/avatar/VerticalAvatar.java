package ontology.avatar;

import core.VGDLSprite;
import core.content.SpriteContent;
import tools.Vector2d;

import java.awt.*;

/**
 * Created with IntelliJ IDEA. User: Diego Date: 22/10/13 Time: 18:09 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class VerticalAvatar extends MovingAvatar {

	public VerticalAvatar() {
	}

	public VerticalAvatar(Vector2d position, Dimension size, SpriteContent cnt) {
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
	}

	@Override
	public VGDLSprite copy() {
		VerticalAvatar newSprite = new VerticalAvatar();
		copyTo(newSprite);
		return newSprite;
	}

	@Override
	public void copyTo(VGDLSprite target) {
		VerticalAvatar targetSprite = (VerticalAvatar) target;
		super.copyTo(targetSprite);
	}
}
