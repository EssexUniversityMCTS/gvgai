package ontology.sprites.producer;

import core.VGDLRegistry;
import core.VGDLSprite;
import core.content.SpriteContent;
import core.game.Game;
import ontology.Types;
import tools.Vector2d;

import java.awt.*;

/**
 * Created with IntelliJ IDEA. User: Diego Date: 21/10/13 Time: 18:24 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class SpawnPoint extends SpriteProducer {
	public double prob;
	public int total;
	public int counter;
	public String stype;
	public int itype;

	public SpawnPoint() {
	}

	public SpawnPoint(Vector2d position, Dimension size, SpriteContent cnt) {
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
		prob = 1.0;
		total = 0;
		color = Types.BLACK;
		cooldown = 1;
		is_static = true;
	}

	@Override
	public void postProcess() {
		super.postProcess();
		is_stochastic = 0 < prob && 1 > prob;
		counter = 0;
		itype = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stype);
	}

	@Override
	public void update(Game game) {
		if (0 == game.getGameTick() % cooldown
				&& game.getRandomGenerator().nextFloat() < prob) {
			game.addSprite(itype, getPosition());
			counter++;
		}

		super.update(game);

		if (0 < total && counter >= total) {
			game.killSprite(this);
		}
	}

	@Override
	public VGDLSprite copy() {
		SpawnPoint newSprite = new SpawnPoint();
		copyTo(newSprite);
		return newSprite;
	}

	@Override
	public void copyTo(VGDLSprite target) {
		SpawnPoint targetSprite = (SpawnPoint) target;
		targetSprite.prob = prob;
		targetSprite.total = total;
		targetSprite.counter = counter;
		targetSprite.stype = stype;
		targetSprite.itype = itype;
		super.copyTo(targetSprite);
	}

}
