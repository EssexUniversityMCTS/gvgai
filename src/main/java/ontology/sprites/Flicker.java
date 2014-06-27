package ontology.sprites;

import core.VGDLSprite;
import core.content.SpriteContent;
import core.game.Game;
import ontology.Types;
import tools.Vector2d;

import java.awt.*;

/**
 * Created with IntelliJ IDEA. User: Diego Date: 17/10/13 Time: 12:45 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Flicker extends VGDLSprite {
	public int limit;

	public int age;

	public Flicker() {
	}

	public Flicker(Vector2d position, Dimension size, SpriteContent cnt) {
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
		limit = 1;
		age = 0;
		color = Types.RED;
	}

	@Override
	public void update(Game game) {
		super.update(game);

		if (age > limit)
			game.killSprite(this);
		age++;

	}

	@Override
	public VGDLSprite copy() {
		Flicker newSprite = new Flicker();
		copyTo(newSprite);
		return newSprite;
	}

	@Override
	public void copyTo(VGDLSprite target) {
		Flicker targetSprite = (Flicker) target;
		targetSprite.limit = limit;
		targetSprite.age = age;
		super.copyTo(targetSprite);
	}
}
