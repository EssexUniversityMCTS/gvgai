package controllers.Return42.knowledgebase;

import java.util.ArrayList;

import controllers.Return42.util.StateObservationUtils;
import tools.Vector2d;
import core.game.Observation;
import core.game.StateObservation;

/**
 * Provides easy access to the relationship between the typeId and the type of
 * the sprite.
 * 
 * In some situations its necessary to call {@link #update(SpriteType, ArrayList[])} again to
 * update the information about new types (think of spawned entities like
 * missiles or a changed avatar)
 * 
 * @author Frederik Buss-Joraschek
 *
 */
public class TypeInformation {

	private final static int ASSUMED_MAXIMUM_TYPE_COUNT = 50;

	private SpriteType[] types = new SpriteType[ASSUMED_MAXIMUM_TYPE_COUNT];

	private GameInformation gameInformation;

	public TypeInformation(GameInformation gameInformation) {
		this.gameInformation = gameInformation;
	}

	/**
	 * Updates all information about types based on the given
	 * {@link StateObservation}
	 * 
	 * @param stateObs
	 */
	public void updateAll(StateObservation stateObs) {
		types = new SpriteType[ASSUMED_MAXIMUM_TYPE_COUNT];

		update(SpriteType.TYPE_FROMAVATAR, stateObs.getFromAvatarSpritesPositions());
		update(SpriteType.TYPE_MOVABLE, stateObs.getMovablePositions());
		update(SpriteType.TYPE_NPC, stateObs.getNPCPositions());
		update(SpriteType.TYPE_PORTAL, stateObs.getPortalsPositions());
		update(SpriteType.TYPE_RESOURCE, stateObs.getResourcesPositions());
		update(SpriteType.TYPE_STATIC, stateObs.getImmovablePositions());

		updateAvatarTypeId(stateObs);

	}

	public void updateAvatarTypeId(StateObservation stateObs) {
		int avatarId = StateObservationUtils.getAvatarType(stateObs);
		if (avatarId != 0) {
			types[avatarId] = SpriteType.TYPE_AVATAR;
		}
	}
	
	public int getCurrentAvatarTypeId(StateObservation stateObs) {
		Vector2d transformedAvatarPosition = gameInformation.gamePositionToGridPosition(stateObs.getAvatarPosition());
		ArrayList<Observation> observations = stateObs.getObservationGrid()[(int) transformedAvatarPosition.x][(int) transformedAvatarPosition.y];
		// Avatar is one of these observations
		for (Observation obs : observations) {
			if (obs.position.equals(stateObs.getAvatarPosition())) {
				if (types[obs.itype] == null || types[obs.itype] == SpriteType.TYPE_AVATAR) {
					return obs.itype;
				}
			}
		}
		
		return 1; //Fallback
	}

	public void update(SpriteType type, ArrayList<Observation>[] positions) {
		if (positions != null) {
			for (ArrayList<Observation> elementsOfType : positions) {
				if (elementsOfType.size() > 0) {
					types[elementsOfType.get(0).itype] = type;
				}
			}
		}
	}

	/**
	 * Returns the type of the sprite with the given id. If no information is
	 * available it will return SpriteType.TYPE_UNKNOWN
	 * 
	 * @param typeId
	 * @return
	 */
	public SpriteType getType(int typeId) {
		SpriteType type = types[typeId];
		return type == null ? SpriteType.TYPE_UNKNOWN : type;
	}

	/**
	 * Returns the highest known type id. This can be used as an estimation for
	 * the total amount of sprite types.
	 * 
	 * @return
	 */
	public int getHighestTypeId() {
		int typeCount = ASSUMED_MAXIMUM_TYPE_COUNT - 1;
		while (types[typeCount] == null)
			typeCount--;

		return typeCount;
	}

}
