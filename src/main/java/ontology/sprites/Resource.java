package ontology.sprites;

import core.VGDLRegistry;
import core.VGDLSprite;
import core.content.SpriteContent;
import tools.Vector2d;

import java.awt.*;

/**
 * Created with IntelliJ IDEA. User: Diego Date: 21/10/13 Time: 18:28 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Resource extends Passive {
	public int value;
	public int limit;
	public int resource_type;
	public String resource_name;

	public Resource() {
	}

	public Resource(Vector2d position, Dimension size, SpriteContent cnt) {
		// Init the sprite
		init(position, size);

		// Specific class default parameter values.
		loadDefaults();

		// Resources are a bit special, we need the resource name
		resource_name = cnt.identifier;

		// Parse the arguments.
		parseParameters(cnt);

	}

	@Override
	public void postProcess() {
		super.postProcess();
		resource_type = VGDLRegistry.GetInstance().getRegisteredSpriteValue(
				resource_name);
	}

	@Override
	protected void loadDefaults() {
		super.loadDefaults();
		limit = 2;
		value = 1;
		color = Color.YELLOW;
		resource_type = -1;
		is_resource = true;
	}

	@Override
	public VGDLSprite copy() {
		Resource newSprite = new Resource();
		copyTo(newSprite);
		return newSprite;
	}

	@Override
	public void copyTo(VGDLSprite target) {
		Resource targetSprite = (Resource) target;
		targetSprite.limit = limit;
		targetSprite.value = value;
		targetSprite.resource_type = resource_type;
		targetSprite.resource_name = resource_name;
		super.copyTo(targetSprite);
	}

}
