package controllers.Return42.knowledgebase;

import java.util.Set;

import controllers.Return42.hashing.ObservationHasher;
import controllers.Return42.knowledgebase.observation.WalkableSpace;
import controllers.Return42.knowledgebase.observation.WalkableSpaceObserver;
import controllers.Return42.util.BiKey;
import controllers.Return42.util.StateObservationUtils;
import core.game.StateObservation;

/**
 * Uses the information that {@link WalkableSpaceObserver} provides to calculate the walkable space. 
 * <br>
 * Ignores movable objects
 * 
 * @author Frederik Buss-Joraschek
 *
 */
public class StaticWalkableSpaceGenerator extends WalkableSpaceGenerator {

	public StaticWalkableSpaceGenerator( GameInformation gameInformation, WalkableSpaceObserver walkableSpaceObserver) {
		super( gameInformation, walkableSpaceObserver);

	}

	@Override
	protected BiKey<Integer, Integer> getKey(StateObservation stateObs, int typeId) {
		final int prime = 47;
		int hash = stateObs.getAvatarResources().hashCode();
		hash = hash * prime + StateObservationUtils.getAvatarType(stateObs);
		hash = hash * prime + ObservationHasher.hash(stateObs.getImmovablePositions());
		hash = hash * prime + ObservationHasher.hash(stateObs.getPortalsPositions());
		hash = hash * prime + ObservationHasher.hash(stateObs.getResourcesPositions());
		
		return 	new BiKey<>(hash, typeId);
	}
	
	@Override
	protected Set<Integer> getNonWalkableTypes(StateObservation stateObs, int typeId){
		return walkableSpaceObserver.getOrCreateEntry(stateObs.getAvatarResources(), typeId);
	}

	@Override
	protected void checkLists(StateObservation stateObs, WalkableSpace[][] walkableSpace, Set<Integer> currentNonWalkableTypes) {
		checkList(stateObs, stateObs.getImmovablePositions(), walkableSpace, currentNonWalkableTypes);
		checkList(stateObs, stateObs.getPortalsPositions(), walkableSpace, currentNonWalkableTypes);
		checkList(stateObs, stateObs.getResourcesPositions(), walkableSpace, currentNonWalkableTypes);
	}

}
