package controllers.Return42.knowledgebase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import controllers.Return42.hashing.ObservationHasher;
import controllers.Return42.knowledgebase.observation.WalkableSpace;
import controllers.Return42.knowledgebase.observation.WalkableSpaceObserver;
import controllers.Return42.util.BiKey;
import controllers.Return42.util.StateObservationUtils;
import controllers.Return42.util.Util;
import tools.Vector2d;
import core.game.Observation;
import core.game.StateObservation;

/**
 * Uses the information that {@link WalkableSpaceObserver} provides to calculate the walkable space. 
 * 
 * @author Frederik Buss-Joraschek
 *
 */
public class WalkableSpaceGenerator implements Observer {

	protected GameInformation gameInformation;
	protected WalkableSpaceObserver walkableSpaceObserver;
	
	protected Map<BiKey<Integer, Integer>, WalkableSpace[][]> cachedWalkableSpaces;


	public WalkableSpaceGenerator( GameInformation gameInformation, WalkableSpaceObserver walkableSpaceObserver) {
		this.gameInformation = gameInformation;
		this.walkableSpaceObserver = walkableSpaceObserver;
		
		walkableSpaceObserver.addObserver(this);
		cachedWalkableSpaces = new HashMap<>();
	}

	protected Set<Integer> getNonWalkableTypes(StateObservation stateObs, int typeId){
		return walkableSpaceObserver.getOrCreateEntry(stateObs.getAvatarResources(), typeId);
	}	
	
	public WalkableSpace[][] getWalkableSpace(StateObservation stateObs, int typeId) {
		BiKey<Integer, Integer> key = getKey(stateObs, typeId);
		WalkableSpace[][] walkableSpace = cachedWalkableSpaces.get(key);
		if(walkableSpace != null){
			return walkableSpace;
		}
		
		ArrayList<Observation>[][] spriteGrid = stateObs.getObservationGrid();
		walkableSpace = new WalkableSpace[spriteGrid.length][spriteGrid[0].length];
		initalizeWalkableSpace(walkableSpace);
		Set<Integer> currentNonWalkableTypes = getNonWalkableTypes(stateObs, typeId);
		
		checkLists(stateObs, walkableSpace, currentNonWalkableTypes);

		cachedWalkableSpaces.put(key, walkableSpace);
		return walkableSpace;
	}

	private void initalizeWalkableSpace(WalkableSpace[][] walkableSpace) {
		for(int i = 0; i < walkableSpace.length; i++){
			for(int j = 0; j < walkableSpace[0].length; j++){
				walkableSpace[i][j] = WalkableSpace.WALKABLE;
			}
		}	
	}

	protected void checkLists(StateObservation stateObs, WalkableSpace[][] walkableSpace,
			Set<Integer> currentNonWalkableTypes) {
		markMovables(stateObs, walkableSpace);
		checkList(stateObs, stateObs.getImmovablePositions(), walkableSpace, currentNonWalkableTypes);
		checkList(stateObs, stateObs.getPortalsPositions(), walkableSpace, currentNonWalkableTypes);
		checkList(stateObs, stateObs.getResourcesPositions(), walkableSpace, currentNonWalkableTypes);
	}
	
	protected void checkList(StateObservation stateObs, ArrayList<Observation>[] list, WalkableSpace[][] walkableSpace,
			Set<Integer> nonWalkableTypes) {
		if(list == null){
			return;
		}
		
		for (ArrayList<Observation> observationsForType : list) {
			if (observationsForType != null && observationsForType.size() > 0) {
				if (nonWalkableTypes.contains(observationsForType.get(0).itype)) {
					for (Observation obs : observationsForType) {
						Vector2d gridPosition = Util.gamePositionToGridPosition(stateObs, obs.position);
						walkableSpace[(int) gridPosition.x][(int) gridPosition.y] = WalkableSpace.BLOCKED;
					}
				}
			}
		}
	}

	protected void markMovables(StateObservation stateObs, WalkableSpace[][] walkableSpace) {
		ArrayList<Observation>[] list = stateObs.getMovablePositions();
		if (list == null) {
			return;
		}

		for (ArrayList<Observation> observationsForType : list) {
			if (observationsForType != null) {
				for (Observation obs : observationsForType) {
					Vector2d gridPosition = Util.gamePositionToGridPosition(stateObs, obs.position);
					if (Util.isGridPositionValid(stateObs, gridPosition)) {
						walkableSpace[(int) gridPosition.x][(int) gridPosition.y] = WalkableSpace.MOVABLE;
					}
				}
			}
		}
	}
	

	protected BiKey<Integer, Integer> getKey(StateObservation stateObs, int typeId) {
		final int prime = 47;
		int hash = stateObs.getAvatarResources().hashCode();
		hash = hash * prime + StateObservationUtils.getAvatarType(stateObs);
		hash = hash * prime + ObservationHasher.hash(stateObs.getMovablePositions());
		hash = hash * prime + ObservationHasher.hash(stateObs.getImmovablePositions());
		hash = hash * prime + ObservationHasher.hash(stateObs.getPortalsPositions());
		hash = hash * prime + ObservationHasher.hash(stateObs.getResourcesPositions());
		
		return 	new BiKey<>(hash, typeId);
	}
	
	public WalkableSpace checkWalkable(StateObservation stateObs, int typeId, int x, int y) {
		return checkWalkable(stateObs, getNonWalkableTypes(stateObs, typeId), x, y);
	}

	protected WalkableSpace checkWalkable(StateObservation stateObs, Set<Integer> nonWalkableTypes, int x, int y) {
		ArrayList<Observation>[][] spriteGrid = stateObs.getObservationGrid();

		for (Observation observation : spriteGrid[x][y]) {
			if (nonWalkableTypes.contains(observation.itype)) {
				return WalkableSpace.BLOCKED;
			}
			boolean isMovable = gameInformation.getTypeInformation().getType(observation.itype)
					.isEqualOrUnknown(SpriteType.TYPE_MOVABLE);
			if (isMovable) {
				return WalkableSpace.MOVABLE;
			}
		}
		return WalkableSpace.WALKABLE;
	}

	@Override
	public void update(Observable o, Object arg) {
		cachedWalkableSpaces.clear();
	}
}
