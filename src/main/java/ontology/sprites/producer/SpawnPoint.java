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
		this.init(position, size);

		// Specific class default parameter values.
		loadDefaults();

		// Parse the arguments.
		this.parseParameters(cnt);
	}

	protected void loadDefaults() {
		super.loadDefaults();
		prob = 1.0;
		total = 0;
		color = Types.BLACK;
		cooldown = 1;
		is_static = true;
	}

	public void postProcess() {
		super.postProcess();
		is_stochastic = (prob > 0 && prob < 1);
		counter = 0;
		itype = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stype);
	}

	public void update(Game game) {
		if ((game.getGameTick() % cooldown == 0)
				&& game.getRandomGenerator().nextFloat() < prob) {
			game.addSprite(itype, this.getPosition());
			counter++;
		}

		super.update(game);

		if (total > 0 && counter >= total) {
			game.killSprite(this);
		}
	}

	public VGDLSprite copy() {
		SpawnPoint newSprite = new SpawnPoint();
		this.copyTo(newSprite);
		return newSprite;
	}

	public void copyTo(VGDLSprite target) {
		SpawnPoint targetSprite = (SpawnPoint) target;
		targetSprite.prob = this.prob;
		targetSprite.total = this.total;
		targetSprite.counter = this.counter;
		targetSprite.stype = this.stype;
		targetSprite.itype = this.itype;
		super.copyTo(targetSprite);
	}

}
