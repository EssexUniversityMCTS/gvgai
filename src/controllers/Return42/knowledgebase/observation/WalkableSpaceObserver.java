package controllers.Return42.knowledgebase.observation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Observable;
import java.util.Set;

import controllers.Return42.knowledgebase.GameInformation;
import controllers.Return42.knowledgebase.SpriteType;
import controllers.Return42.util.MapUtil;
import controllers.Return42.util.ResourcesAndTypeKey;
import ontology.Types;
import tools.Vector2d;
import core.game.Event;
import core.game.Observation;
import core.game.StateObservation;

/**
 * Observed the game and extracts information about walkable sprites and
 * calculates the according walkable space. It takes the inventory of the avatar
 * into consideration.
 * 
 * @author Frederik Buss-Joraschek
 *
 */
public class WalkableSpaceObserver extends Observable implements GameObserver {

	private GameInformation gameInformation;

	private Vector2d lastPosition = new Vector2d(-1, -1);
	private HashMap<Integer, Integer> lastResources;
	
	private Map<ResourcesAndTypeKey, Set<Integer>> nonWalkableTypesMap = new HashMap<>();


	public WalkableSpaceObserver( GameInformation gameInformation ) {
		this.gameInformation = gameInformation;
	}

	public void preStepObserve(StateObservation stateObs) {
		lastPosition = stateObs.getAvatarPosition();
		lastResources = stateObs.getAvatarResources();
	}

	public void postStepObserve(StateObservation stateObs) {
		Event lastEvent = new Event(stateObs.getGameTick() - 1, true, -1, -1, -1, -1, null);
		NavigableSet<Event> eventsInLastTurn = stateObs.getEventsHistory().tailSet(lastEvent, false);
		for (Event event : eventsInLastTurn) {
								
			SpriteType receiverType = gameInformation.getTypeInformation().getType(event.passiveTypeId);
			SpriteType triggerType = gameInformation.getTypeInformation().getType(event.activeTypeId);
			if (receiverType.equals(SpriteType.TYPE_STATIC) && triggerType.equals(SpriteType.TYPE_AVATAR)) {
				
				if(stateObs.getGameWinner() == Types.WINNER.PLAYER_LOSES){
					if (isReceiverObjectStillThere(stateObs, event.passiveTypeId, event.passiveSpriteId)) {
						Set<Integer> typesSet = getOrCreateEntry(lastResources, event.activeTypeId);

						if (!typesSet.contains(event.passiveTypeId)) {
							typesSet.add(event.passiveTypeId);
							setChanged();
						}
					}
				} else if (stateObs.getGameWinner() == Types.WINNER.NO_WINNER) {
					if (lastPosition.equals(stateObs.getAvatarPosition())) {
						if (isReceiverObjectStillThere(stateObs, event.passiveTypeId, event.passiveSpriteId)) {
							Set<Integer> typesSet = getOrCreateEntry(lastResources, event.activeTypeId);

							if (!typesSet.contains(event.passiveTypeId)) {
								typesSet.add(event.passiveTypeId);
								setChanged();
							}
						} else {
							removeTypeFromNonWalkableSet(event);
						}
					} else {
						removeTypeFromNonWalkableSet(event);
					}
				}
			}
		}
		

		

		notifyObservers();
	}

	private void removeTypeFromNonWalkableSet(Event event) {
		Set<Integer> typesSet = getOrCreateEntry(lastResources, event.activeTypeId);

		if (typesSet.contains(event.passiveTypeId)) {
			typesSet.remove(event.passiveTypeId);
			setChanged();
		}
	}

	private boolean isReceiverObjectStillThere(StateObservation stateObs, int passiveTypeId, int passiveSpriteId) {
		for (ArrayList<Observation> elementsOfType : stateObs.getImmovablePositions()) {
			if (elementsOfType.size() > 0 && elementsOfType.get(0).itype == passiveTypeId) {
				for (Observation element : elementsOfType) {
					if (element.obsID == passiveSpriteId) {
						return true;
					}
				}
			}
		}

		return false;
	}

	public Set<Integer> getOrCreateEntry(Map<Integer, Integer> avatarResources, int activeTypeId) {
		ResourcesAndTypeKey key = new ResourcesAndTypeKey(avatarResources, activeTypeId);
		return MapUtil.getOrCreate(nonWalkableTypesMap, key, new HashSet<Integer>());
	}
}