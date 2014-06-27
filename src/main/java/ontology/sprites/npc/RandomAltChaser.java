package ontology.sprites.npc;

import core.VGDLSprite;
import core.content.SpriteContent;
import core.game.Game;
import ontology.Types;
import tools.Utils;
import tools.Vector2d;

import java.awt.*;

/**
 * Created by Diego on 24/02/14.
 */
public class RandomAltChaser extends AlternateChaser {

	public double epsilon;

	public RandomAltChaser() {
	}

	public RandomAltChaser(Vector2d position, Dimension size, SpriteContent cnt) {
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
		epsilon = 0.0;
	}

	@Override
	public void update(Game game) {
		double roll = game.getRandomGenerator().nextDouble();
		if (roll < epsilon) {
			// do a sampleRandom move.
			updatePassive();
			Vector2d act = (Vector2d) Utils.choice(Types.BASEDIRS,
					game.getRandomGenerator());
			physics.activeMovement(this, act, speed);
		} else {
			super.update(game);
		}
	}

	@Override
	public VGDLSprite copy() {
		RandomAltChaser newSprite = new RandomAltChaser();
		copyTo(newSprite);
		return newSprite;
	}

	@Override
	public void copyTo(VGDLSprite target) {
		RandomAltChaser targetSprite = (RandomAltChaser) target;
		targetSprite.epsilon = epsilon;
		super.copyTo(targetSprite);
	}

}
