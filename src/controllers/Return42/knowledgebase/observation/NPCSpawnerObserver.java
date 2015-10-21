package controllers.Return42.knowledgebase.observation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Observable;
import java.util.Set;

import controllers.Return42.knowledgebase.GameInformation;
import controllers.Return42.util.StateObservationUtils;
import controllers.Return42.util.Util;
import tools.Vector2d;
import core.game.Observation;
import core.game.StateObservation;

/**
 * Observes the game and extracts information about spawners.
 * 
 * Currently only supports NPC spawners. 
 *  
 * @author Frederik Buss-Joraschek
 *
 */
public class NPCSpawnerObserver extends Observable implements GameObserver {

	private final static int HIGHEST_TYPE_ID_SAFETY_MARGIN = 10;
	
	// SpawnerTypeId => ProducedEntityTypeId
	private HashMap<Integer, Set<Integer>> spawner;
	private GameInformation gameInformation;
	
	private ArrayList<Observation>[] oldNPCs;

	public NPCSpawnerObserver( GameInformation gameInformation ) {
		spawner = new HashMap<>();
		this.gameInformation = gameInformation;
	}

	public void preStepObserve(StateObservation stateObs) {
		oldNPCs = stateObs.getNPCPositions();
	}

	public void postStepObserve(StateObservation stateObs) {	
		// cannot rely on information when game is over
		if (stateObs.isGameOver())
			return;
		
		ArrayList<Observation>[] currentNPCs = stateObs.getNPCPositions();
		if(currentNPCs == null){
			return;
		}
		
		// Sprite ids always increment strict monotonic: new NPC => higher sprite id
		int[] oldHighestIds = new int[gameInformation.getTypeInformation().getHighestTypeId() + HIGHEST_TYPE_ID_SAFETY_MARGIN];
		if (oldNPCs != null) {
			for (ArrayList<Observation> npcsOfSameType : oldNPCs) {
				for (Observation obs : npcsOfSameType) {
					oldHighestIds[obs.itype] = Math.max(oldHighestIds[obs.itype], obs.obsID);
				}
			}
		}
		
		for(ArrayList<Observation> obsOfSameType : currentNPCs){
			for(Observation obs : obsOfSameType){
				boolean npcIsNew = oldHighestIds[obs.itype] < obs.obsID;
				boolean isStillAlive = isObservationAlive( obs );
				if(npcIsNew && isStillAlive){
					int spawnerType = findSpawnerType(stateObs, obs);

					if (spawnerType != -1) {
						Set<Integer> entry = spawner.get(spawnerType);
						if (entry == null) {
							entry = new HashSet<Integer>();
							setChanged();
//							System.out.println("Found spawner: " + spawnerType + " produces " + obs.itype);
						} else if (!entry.contains(obs.itype)) {
							setChanged();
//							System.out.println("Found spawner: " + spawnerType + " produces " + obs.itype);
						}
						entry.add(obs.itype);
						spawner.put(spawnerType, entry);
					}
				}
			}
		}
		
		notifyObservers();
	}

	private boolean isObservationAlive(Observation obs) {
		return obs.position.x >= 0 && obs.position.y >= 0;
	}

	private int findSpawnerType(StateObservation stateObs, Observation spawnedObs) {
		Vector2d gridPosition = Util.gamePositionToGridPosition(stateObs, spawnedObs.position);
		ArrayList<Observation> obsAtPosition = stateObs.getObservationGrid()[(int) gridPosition.x][(int) gridPosition.y];
		
		int foundTypeId = -1;
		for(Observation obs : obsAtPosition){
			if(obs.position.equals(spawnedObs.position)){
				boolean obsIsNotSameTypeAsSpawnedEntity = obs.itype != spawnedObs.itype;
				if(obsIsNotSameTypeAsSpawnedEntity){
					boolean foundOtherCandidate = foundTypeId != -1 && obs.itype != foundTypeId;
					if(foundOtherCandidate){
						// Multiple candidates for the spawner. Aborting
						return -1;
					} else {
						if(obs.itype == 6){
							System.out.println("srust");
						}
						foundTypeId = obs.itype;
					}
				}
			}
		}
		
		return foundTypeId;
	}
	
	/**
	 * Returns the types that are spawned by a sprite with the given typeId. The
	 * Set is empty if no information is available
	 */
	public Set<Integer> getSpawnedTypes(int typeId){
		Set<Integer> entry = spawner.get(typeId);
		if(entry == null){
			return new HashSet<>();
		} else {
			return entry;
		}
	}


}


