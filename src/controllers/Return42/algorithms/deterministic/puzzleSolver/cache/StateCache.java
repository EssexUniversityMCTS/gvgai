package controllers.Return42.algorithms.deterministic.puzzleSolver.cache;

import controllers.Return42.algorithms.deterministic.puzzleSolver.AStarNode;
import core.game.StateObservation;

/**
 * Created by Oliver on 03.05.2015.
 */
public interface StateCache {

    void storeState( AStarNode node, StateObservation state );
    boolean containsStateForNode( AStarNode node );
    StateObservation getState(AStarNode node);

}