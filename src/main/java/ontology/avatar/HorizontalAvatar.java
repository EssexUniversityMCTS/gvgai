package ontology.avatar;

import core.VGDLSprite;
import core.content.SpriteContent;
import core.game.Game;
import ontology.Types;
import tools.Vector2d;

import java.awt.*;

/**
 * Created with IntelliJ IDEA. User: Diego Date: 22/10/13 Time: 18:07 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class HorizontalAvatar extends MovingAvatar {
	public HorizontalAvatar() {
	}

	public HorizontalAvatar(Vector2d position, Dimension size, SpriteContent cnt) {
		// Init the sprite
		init(position, size);

		// Specific class default parameter values.
		loadDefaults();

		// Parse the arguments.
		parseParameters(cnt);
	}

	@Override
	public void postProcess() {
		// Define actions here first.
		if (actions.isEmpty()) {
			actions.add(Types.ACTIONS.ACTION_LEFT);
			actions.add(Types.ACTIONS.ACTION_RIGHT);
		}

		super.postProcess();
	}

	@Override
	protected void loadDefaults() {
		super.loadDefaults();
	}

	@Override
	public void update(Game game) {
		super.update(game);
	}

	@Override
	public VGDLSprite copy() {
		HorizontalAvatar newSprite = new HorizontalAvatar();
		copyTo(newSprite);
		return newSprite;
	}

	@Override
	public void copyTo(VGDLSprite target) {
		HorizontalAvatar targetSprite = (HorizontalAvatar) target;
		super.copyTo(targetSprite);
	}
}
