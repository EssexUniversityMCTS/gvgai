package ontology.sprites;

import core.VGDLRegistry;
import core.VGDLSprite;
import core.content.SpriteContent;
import core.game.Game;
import ontology.Types;
import tools.Vector2d;

import java.awt.*;

/**
 * Created with IntelliJ IDEA. User: Diego Date: 21/10/13 Time: 18:31 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Spreader extends Flicker {
	public double spreadprob;

	public String stype;

	public int itype;

	public Spreader() {
	}

	public Spreader(Vector2d position, Dimension size, SpriteContent cnt) {
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
		spreadprob = 1.0;
	}

	@Override
	public void postProcess() {
		super.postProcess();
		itype = -1;
		if (null != stype)
			itype = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stype);
	}

	@Override
	public void update(Game game) {
		super.update(game);
		if (2 == age) {
			for (Vector2d u : Types.BASEDIRS) {
				if (game.getRandomGenerator().nextDouble() < spreadprob) {
					int newType = -1 == itype ? getType() : itype;
					game.addSprite(newType, new Vector2d(lastrect.x + u.x
							* lastrect.width, lastrect.y + u.y
							* lastrect.height));
				}
			}
		}

	}
	@Override
	public VGDLSprite copy() {
		Spreader newSprite = new Spreader();
		copyTo(newSprite);
		return newSprite;
	}

	@Override
	public void copyTo(VGDLSprite target) {
		Spreader targetSprite = (Spreader) target;
		targetSprite.spreadprob = spreadprob;
		super.copyTo(targetSprite);
	}
}
