package ontology.sprites.npc;

import core.VGDLSprite;
import core.content.SpriteContent;
import core.game.Game;
import ontology.Types;
import tools.Utils;
import tools.Vector2d;

import java.awt.*;

/**
 * Created with IntelliJ IDEA. User: Diego Date: 21/10/13 Time: 18:08 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class RandomNPC extends VGDLSprite {
	public RandomNPC() {
	}

	public RandomNPC(Vector2d position, Dimension size, SpriteContent cnt) {
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
		speed = 1;
		is_npc = true;
		is_stochastic = true;
	}

	@Override
	public void update(Game game) {
		updatePassive();
		Vector2d act = (Vector2d) Utils.choice(Types.BASEDIRS,
				game.getRandomGenerator());
		physics.activeMovement(this, act, speed);
	}

	@Override
	public VGDLSprite copy() {
		RandomNPC newSprite = new RandomNPC();
		copyTo(newSprite);
		return newSprite;
	}

	@Override
	public void copyTo(VGDLSprite target) {
		RandomNPC targetSprite = (RandomNPC) target;
		super.copyTo(targetSprite);
	}
}
