package ontology.sprites.producer;

import core.VGDLRegistry;
import core.VGDLSprite;
import core.content.SpriteContent;
import ontology.Types;
import tools.Vector2d;

import java.awt.*;

/**
 * Created with IntelliJ IDEA. User: Diego Date: 21/10/13 Time: 18:23 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Portal extends SpriteProducer {
	public String stype;
	public int itype;

	public Portal() {
	}

	public Portal(Vector2d position, Dimension size, SpriteContent cnt) {
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
		is_static = true;
		portal = true;
		color = Types.BLUE;
	}

	@Override
	public void postProcess() {
		super.postProcess();
		itype = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stype);
	}

	@Override
	public VGDLSprite copy() {
		Portal newSprite = new Portal();
		copyTo(newSprite);
		return newSprite;
	}

	@Override
	public void copyTo(VGDLSprite target) {
		Portal targetSprite = (Portal) target;
		targetSprite.stype = stype;
		targetSprite.itype = itype;
		super.copyTo(targetSprite);
	}
}
