package controllers.Return42.algorithms.deterministic.puzzleSolver;

import java.util.List;

import controllers.Return42.util.TimeoutException;
import ontology.Types;
import tools.ElapsedCpuTimer;
import core.game.StateObservation;

/**
 * Created by Oliver on 06.05.2015.
 */
public class StateBuilder {

    private final StateObservation initialState;

    public StateBuilder(StateObservation initialState) {
        this.initialState = initialState;
    }

    public StateObservation buildState( AStarNode node, ElapsedCpuTimer timer ) throws TimeoutException {
        List<Types.ACTIONS> plan = AStarUtils.extractActions( node );

        StateObservation newState = initialState.copy();
        for(Types.ACTIONS action: plan) {
            if (timer.exceededMaxTime()) {
                throw new TimeoutException();
            }

            newState.advance(action);
        }

        return newState;
    }
}
