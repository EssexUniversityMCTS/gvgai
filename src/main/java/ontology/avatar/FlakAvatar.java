package ontology.avatar;

import core.VGDLRegistry;
import core.VGDLSprite;
import core.content.SpriteContent;
import core.game.Game;
import ontology.Types;
import tools.Utils;
import tools.Vector2d;

import java.awt.*;

/**
 * Created with IntelliJ IDEA. User: Diego Date: 22/10/13 Time: 18:08 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class FlakAvatar extends HorizontalAvatar {
	public String stype;

	public int itype;

	// This is the resource I need, to be able to shoot.
	public String ammo; // If ammo is null, no resource needed to shoot.
	public int ammoId;
	public int minAmmo; // -1 if not used. minimum amount of ammo needed for
						// shooting.
	public int ammoCost; // 1 if not used. amount of ammo to subtract after
							// shooting once.

	public FlakAvatar() {
	}

	public FlakAvatar(Vector2d position, Dimension size, SpriteContent cnt) {
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
		minAmmo = -1;
		ammoCost = 1;
		color = Types.GREEN;
	}

	@Override
	public void postProcess() {
		// Define actions here first.
		if (actions.isEmpty()) {
			actions.add(Types.ACTIONS.ACTION_USE);
			actions.add(Types.ACTIONS.ACTION_LEFT);
			actions.add(Types.ACTIONS.ACTION_RIGHT);
		}

		super.postProcess();

		itype = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stype);
		if (null != ammo)
			ammoId = VGDLRegistry.GetInstance().getRegisteredSpriteValue(ammo);
	}

	@Override
	public void update(Game game) {
		super.update(game);

		if (!hasMoved && Utils.processUseKey(Game.ki.getMask()) && hasAmmo()) {
			VGDLSprite added = game.addSprite(itype, new Vector2d(rect.x,
					rect.y));
			if (null != added) { // singleton sprites could not add anything
									// here.
				reduceAmmo();
				added.setFromAvatar(true);
			}
		}
	}

	private boolean hasAmmo() {
		if (null == ammo)
			return true; // no ammo defined, I can shoot.

		// If I have ammo, I must have enough resource of ammo type to be able
		// to shoot.
		if (resources.containsKey(ammoId))
			return -1 < minAmmo
					? resources.get(ammoId) > minAmmo
					: 0 < resources.get(ammoId);

		return false;
	}

	private void reduceAmmo() {
		if (null != ammo && resources.containsKey(ammoId)) {
			resources.put(ammoId, resources.get(ammoId) - ammoCost);
		}
	}
	@Override
	public VGDLSprite copy() {
		FlakAvatar newSprite = new FlakAvatar();
		copyTo(newSprite);
		return newSprite;
	}

	@Override
	public void copyTo(VGDLSprite target) {
		FlakAvatar targetSprite = (FlakAvatar) target;
		targetSprite.stype = stype;
		targetSprite.itype = itype;
		targetSprite.ammo = ammo;
		targetSprite.ammoId = ammoId;
		targetSprite.ammoCost = ammoCost;
		targetSprite.minAmmo = minAmmo;
		super.copyTo(targetSprite);
	}

}
