package controllers.YOLOBOT.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import controllers.YOLOBOT.YoloState;
import tools.Vector2d;
import core.game.Observation;

/**
 * In dieser Klasse wird pro IType die Observation mit der kuerzesten Distanz
 * gespeichert, so wie der Spielzustand (YoloState), von dem aus man
 * einen IType am kuerzesten erreicht.
 * 
 * @author Elvir, Gerrit
 *
 */
public class ITypeQueue {
	/**
	 * Die Eigenschaften der tripel sind wie folgt definiert:
	 * 1. Observation mit der kuerzesten sqDist zu dem IType
	 * 2. Spielzustand, von dem aus man die kuerzeste Distanz zu dem IType hat
	 * 3. Prioritaet des ITypes (hoechste Prioritaet ist 1)
	 */
	public final Map<Integer, Triplet<Observation, YoloState, Integer>> closestITypes;
	public final List<ObservationType> observationTypes;
	public YoloState currentStateObs;

	public ITypeQueue(YoloState initStateObs,
			ObservationType observationType) {
		this(initStateObs, Arrays.asList(observationType));
	}

	public ITypeQueue(YoloState initStateObs,
			List<ObservationType> observationTypes) {
		currentStateObs = initStateObs;
		this.observationTypes = observationTypes;
		closestITypes = new HashMap<Integer, Triplet<Observation, YoloState, Integer>>();

		UpdateClosestITypes();
	}

	public Triplet<Observation, YoloState, Integer> GetMostPrioritizedAndClosestIType() {
		//Change: Java 7 -> kein list.sort, sondern Collections.sort(list)! 
		Comparator<Triplet<Observation, YoloState, Integer>> c = new Comparator<Triplet<Observation, YoloState, Integer>>() {
			@Override
			public int compare(
					Triplet<Observation, YoloState, Integer> triplet1,
					Triplet<Observation, YoloState, Integer> triplet2) {
				// Compare by priority
				if (triplet1.Item3 < triplet2.Item3) {
					return -1;
				}
				if (triplet1.Item3 > triplet2.Item3) {
					return 1;
				}
				// Then compare by sqDist
				if (triplet1.Item1.sqDist < triplet2.Item1.sqDist) {
					return -1;
				}
				if (triplet1.Item1.sqDist > triplet2.Item1.sqDist) {
					return 1;
				}
				return 0;
			}
		};
		ArrayList<Triplet<Observation, YoloState, Integer>> list = new ArrayList<Triplet<Observation, YoloState, Integer>>(
				closestITypes.values());
		Collections.sort(list, c);

		return list.get(0);
	}
	
	public void RaisePriority(int itype, int raiseValue) {
		if (raiseValue <= 0) {
			throw new IllegalArgumentException("raiseValue must be positive.");
		}
		
		Triplet<Observation, YoloState, Integer> triplet = closestITypes.get(itype);
		closestITypes.put(
				itype, 
				new Triplet<Observation, YoloState, Integer>(
						triplet.Item1,
						triplet.Item2,
						Math.max(1, triplet.Item3 - raiseValue))
						);
		
	}
	
	public void LowerPriority(int itype, int lowerValue) {
		if (lowerValue <= 0) {
			throw new IllegalArgumentException("lowerValue must be positive.");
		}
		
		Triplet<Observation, YoloState, Integer> triplet = closestITypes.get(itype);
		closestITypes.put(
				itype, 
				new Triplet<Observation, YoloState, Integer>(
						triplet.Item1,
						triplet.Item2,
						triplet.Item3 + lowerValue)
						);
	}

	public int Count() {
		return closestITypes.size();
	}

	public void UpdateClosestITypes() {
		Vector2d avatarPosition = currentStateObs.getAvatarPosition();

		for (ObservationType observationType : observationTypes) {
			List<Observation>[] allObservations;
			switch (observationType) {
			case Immovable:
				allObservations = currentStateObs
						.getImmovablePositions(avatarPosition);
				break;
			case Movable:
				allObservations = currentStateObs
						.getMovablePositions(avatarPosition);
				break;
			case NPC:
				allObservations = currentStateObs
						.getNpcPositions(avatarPosition);
				break;
			case Portal:
				allObservations = currentStateObs
						.getPortalsPositions(avatarPosition);
				break;
			case Resource:
				allObservations = currentStateObs
						.getResourcesPositions(avatarPosition);
				break;
			case FromAvatarSprite:
				allObservations = currentStateObs
						.getFromAvatarSpritesPositions(avatarPosition);
				break;
			default:
				allObservations = null;
				break;
			}

			int itype;
			// Put in all closest observations
			if(allObservations != null){
				for (List<Observation> observations : allObservations) {
					itype = observations.get(0).itype;
					
					// If itype is unknown add a new triplet with priority 1
					if (!closestITypes.keySet().contains(itype)) {
						closestITypes.put(observations.get(0).itype,
								new Triplet<Observation, YoloState, Integer>(
										observations.get(0), 
										currentStateObs, 
										1)
									);
					} 
					// otherwise if a new YoloState with shorter distance has been found 
					// then update the triplet
					else if (observations.get(0).sqDist < closestITypes.get(itype).Item1.sqDist) {
						closestITypes.put(observations.get(0).itype,
								new Triplet<Observation, YoloState, Integer>(
										observations.get(0), 
										currentStateObs, 
										closestITypes.get(itype).Item3)
									);
					}
				}
			}
		}
	}

}
