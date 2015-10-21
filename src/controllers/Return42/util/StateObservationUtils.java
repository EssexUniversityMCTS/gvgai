package controllers.Return42.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import controllers.Return42.knowledgebase.KnowledgeBase;
import controllers.Return42.knowledgebase.observation.ScoreObserver;
import ontology.Types;
import tools.Vector2d;
import core.game.Observation;
import core.game.StateObservation;

/**
 * Created by Oliver on 04.05.2015.
 */
public class StateObservationUtils {

    public static List<Observation> flatten( List<Observation>[] observations ) {
        List<Observation> result = new LinkedList<>();

        if (observations == null)
            return result;

        for( List<Observation> observationsOfSameType: observations ) {
            result.addAll( observationsOfSameType );
        }

        return result;
    }

    public static List<Observation> getMovablesOfType( StateObservation state, int itypeOfMovable) {
        List<Observation>[] allMovables = state.getMovablePositions();
        if (allMovables == null)
            return new LinkedList<>();

        for( List<Observation> observationsOfSameType: allMovables ) {
            if (!observationsOfSameType.isEmpty()) {
                if (observationsOfSameType.get(0).itype == itypeOfMovable) {
                    return observationsOfSameType;
                }
            }
        }

        return new LinkedList<>();
    }

    public static List<Observation> getImmovablesOfType(StateObservation state, int itypeOfSink) {
        List<Observation>[] allImmovables = state.getImmovablePositions();

        for( List<Observation> observationsOfSameType: allImmovables ) {
            if (!observationsOfSameType.isEmpty()) {
                if (observationsOfSameType.get(0).itype == itypeOfSink) {
                    return observationsOfSameType;
                }
            }
        }

        return new LinkedList<>();
    }

    public static Observation selectClosest(List<Observation> observations, Vector2d position) {
        double closestDist = Double.MAX_VALUE;
        Observation closest = null;

        for( Observation anObservation: observations ) {
            double dist = DistanceUtils.manhattanDistance(position, anObservation.position);

            if (dist < closestDist) {
                closestDist = dist;
                closest = anObservation;
            }
        }

        return closest;
    }

	public static int count(List<Observation>[] observations) {
		if (observations == null)
			return 0;
		
		int sum = 0;
		for( List<Observation> observationType: observations ) {
			sum += observationType.size();
		}
		
		return sum;
	}
	
	public static int count(HashMap<Integer, Integer> resources ) {
		if (resources == null)
			return 0;
		
		int sum = 0;
		
		for( Integer value: resources.values() ) {
			sum += value;
		}
		
		return sum;
	}


	public static Map<Integer, Integer> countSpritesPerCategories( List<Observation>[] observations) {
		if (observations == null)
			return new HashMap<Integer, Integer>();

		Map<Integer, Integer> counts = new HashMap<Integer, Integer>();

		for( List<Observation> observationsOfSameCategory: observations ) {
			if (!observationsOfSameCategory.isEmpty() ) {
				int type = observationsOfSameCategory.get(0).itype;
				counts.put( type , observationsOfSameCategory.size() );
			}
		}
		
		return counts;
	}

	public static int getAvatarType(StateObservation state) {
		int x = (int) state.getAvatarPosition().x / state.getBlockSize();
		int y = (int) state.getAvatarPosition().y / state.getBlockSize();
	
		List<Observation>[][] grid = state.getObservationGrid();
		if ( x < 0 || y < 0 || x >= grid.length || y >= grid[0].length ) {
			System.out.println( "Warning: Did not find avatar type. Returning 0 instead.");
			return 0;
		}

		List<Observation> obsAtAvatarPos = grid[x][y];
		for( Observation obs: obsAtAvatarPos ) {
			if (obs.category == Types.TYPE_AVATAR) {
				return obs.itype;
			}
		}
		
		System.out.println( "Warning: Did not find avatar type. Returning 0 instead.");
		return 0;
	}

	public static Map<Integer,Integer> countTypes(ArrayList<Observation>[] observations) {
		Map<Integer,Integer> counts = new HashMap<>();
		
		if (observations == null)
			return counts;
		
		for( List<Observation> sameType: observations ) {
			if (!sameType.isEmpty()) {
				counts.put( sameType.get(0).itype, sameType.size() );
			}
		}
		
		return counts;
	}

	public static List<Vector2d> getNpcPositions(StateObservation state) {
		List<Observation> npcs = flatten(state.getNPCPositions());
		List<Vector2d> npcPositions = new LinkedList<Vector2d>();

		for( Observation npc: npcs ) {
			npcPositions.add( npc.position.copy() );
		}
		
		return npcPositions;
	}

	public static boolean isPlayerCloseToNpc(StateObservation stateObs, KnowledgeBase knowledge, double maxDistInGrid ) {
		List<Observation> npcs = flatten( stateObs.getNPCPositions());
	
		if (npcs.isEmpty())
			return false;

		double[][] distances = knowledge.getDistanceMapGenerator().generateDistanceMap( stateObs );
		ScoreObserver observer = knowledge.getScoreObserver();
		int avatarId = getAvatarType(stateObs);
				
		// Do not trust the order given by getNpcPosition( avatarPosition ). It's wrong!
		for( Observation obs: npcs ) {
			if (!observer.isDeadly(stateObs.getAvatarResources(), avatarId, obs.itype)) {
				continue;
			}
			
			Vector2d posInGrid = knowledge.getGameInformation().gamePositionToGridPosition( obs.position );
			double distInGrid = distances[(int)posInGrid.x][(int)posInGrid.y]; 
					
			if (distInGrid <= maxDistInGrid) {
				return true;
			}
		}
		
		return false;
	}
}
