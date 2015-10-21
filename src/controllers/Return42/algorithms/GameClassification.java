package controllers.Return42.algorithms;

import core.game.Observation;
import core.game.StateObservation;
import ontology.Types;

import java.util.List;

import controllers.Return42.hashing.IGameStateHasher;
import controllers.Return42.hashing.MovableHasher;

/**
 * Created by Oliver on 06.05.2015.
 */
public class GameClassification {

	private boolean isDeterministic;
	private boolean hasSelfMovingObjects;
	private boolean hasMovables;
	
	public void init(StateObservation state) {
		isDeterministic = checkIfGameIsDeterministic( state, 20, 2 );
		hasSelfMovingObjects = checkIfGameHasObjectsMovingByThemselves( state, 5, 2 );
		hasMovables = checkIfGameHasMovables( state );
	}
	
    /**
     * simulate n steps in m simulations and check if the states are all equal
     * @param stateObs game state
     * @param steps Number of cache steps
     * @param runs Number of cache runs
     * @return true if the game is deterministic after n steps
     */
    private boolean checkIfGameIsDeterministic(StateObservation stateObs, int steps, int runs) {
        StateObservation[] obs = new StateObservation[runs];
        
        for(int i = 0; i < runs; i++) {
            obs[i] = stateObs.copy();
            for(int j = 0; j < steps; j++) {
                obs[i].advance(Types.ACTIONS.ACTION_NIL);
            }

            if(obs[i].getNPCPositions() != null) {
                return false;
            }

            if(i > 0 && !obs[i-1].equiv(obs[i])) {
                return false;
            }
        }

        return true;
    }

    /**
     * Check if movable objects move on its own. E.g. Frogs
     * @param stateObs game state
     * @param n Number of cache steps
     * @param m Number of cache runs
     * @return true if movables movable on its own
     */
    private boolean checkIfGameHasObjectsMovingByThemselves(StateObservation stateObs, int n, int m) {
        IGameStateHasher hasher = new MovableHasher();

        for(int i = 0; i < m; i++) {
            StateObservation state = stateObs.copy();
            List<Observation>[] movables = stateObs.getMovablePositions();

            if(movables == null) {
                return false;
            } else {
                int hashBeforeSimulation = hasher.hash( state );

                for(int j = 0; j < n; j++) {
                    state.advance(Types.ACTIONS.ACTION_NIL);
                }

                int hashAfterSimulation = hasher.hash( state );

                if (hashBeforeSimulation != hashAfterSimulation) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean checkIfGameHasMovables( StateObservation state ) {
        return state.getMovablePositions() != null;
    }

	public boolean isDeterministic() {
		return isDeterministic;
	}

	public boolean hasSelfMovingObjects() {
		return hasSelfMovingObjects;
	}

	public boolean hasMovables() {
		return hasMovables;
	}
}
