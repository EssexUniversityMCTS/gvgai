package ontology.avatar.oriented;

import core.VGDLRegistry;
import core.VGDLSprite;
import core.content.SpriteContent;
import core.game.Game;
import ontology.Types;
import tools.Utils;
import tools.Vector2d;

import java.awt.*;

/**
 * Created with IntelliJ IDEA. User: Diego Date: 22/10/13 Time: 18:10 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class ShootAvatar extends OrientedAvatar {

	// This is the resource I need, to be able to shoot.
	public String ammo; // If ammo is null, no resource needed to shoot.
	public int ammoId;

	// This is the sprite I shoot
	public String stype;
	public int itype;

	public ShootAvatar() {
	}

	public ShootAvatar(Vector2d position, Dimension size, SpriteContent cnt) {
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
		ammo = null;
		ammoId = -1;
		stype = null;
		itype = -1;
	}

	@Override
	public void update(Game game) {
		super.update(game);

		if (!hasMoved && Utils.processUseKey(Game.ki.getMask()) && hasAmmo()) {
			shoot(game);
		}

	}

	private void shoot(Game game) {
		// TODO: Theoretically, we should be able to shoot many things here...
		// to be done.
		Vector2d dir = orientation.copy();
		dir.normalise();

		VGDLSprite newOne = game.addSprite(itype, new Vector2d(rect.x + dir.x
				* lastrect.width, rect.y + dir.y * lastrect.height));

		if (null != newOne) {
			if (newOne.is_oriented)
				newOne.orientation = dir;
			reduceAmmo();
			newOne.setFromAvatar(true);
		}
	}

	private boolean hasAmmo() {
		if (null == ammo)
			return true; // no ammo defined, I can shoot.

		// If I have ammo, I must have enough resource of ammo type to be able
		// to shoot.
		return resources.containsKey(ammoId) && 0 < resources.get(ammoId);

	}

	private void reduceAmmo() {
		if (null != ammo && resources.containsKey(ammoId)) {
			resources.put(ammoId, resources.get(ammoId) - 1);
		}
	}

	@Override
	public void postProcess() {
		// Define actions here first.
		if (actions.isEmpty()) {
			actions.add(Types.ACTIONS.ACTION_USE);
			actions.add(Types.ACTIONS.ACTION_LEFT);
			actions.add(Types.ACTIONS.ACTION_RIGHT);
			actions.add(Types.ACTIONS.ACTION_DOWN);
			actions.add(Types.ACTIONS.ACTION_UP);
		}

		super.postProcess();

		itype = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stype);
		if (null != ammo)
			ammoId = VGDLRegistry.GetInstance().getRegisteredSpriteValue(ammo);
	}

	@Override
	public VGDLSprite copy() {
		ShootAvatar newSprite = new ShootAvatar();
		copyTo(newSprite);
		return newSprite;
	}

	@Override
	public void copyTo(VGDLSprite target) {
		ShootAvatar targetSprite = (ShootAvatar) target;
		targetSprite.stype = stype;
		targetSprite.itype = itype;
		targetSprite.ammo = ammo;
		targetSprite.ammoId = ammoId;

		super.copyTo(targetSprite);
	}
}
