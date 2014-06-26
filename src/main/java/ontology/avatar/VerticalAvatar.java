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
		this.init(position, size);

		// Specific class default parameter values.
		loadDefaults();

		// Parse the arguments.
		this.parseParameters(cnt);
	}

	protected void loadDefaults() {
		super.loadDefaults();
	}

	public VGDLSprite copy() {
		VerticalAvatar newSprite = new VerticalAvatar();
		this.copyTo(newSprite);
		return newSprite;
	}

	public void copyTo(VGDLSprite target) {
		VerticalAvatar targetSprite = (VerticalAvatar) target;
		super.copyTo(targetSprite);
	}
}
