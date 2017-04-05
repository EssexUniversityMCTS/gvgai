package core.player;

import core.game.StateObservation;
import ontology.Types;
import tools.ElapsedCpuTimer;

/**
 * Created by Daniel on 05.04.2017.
 */
public abstract class AbstractLearnerMultiPlayer extends Player{

    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        return Types.ACTIONS.ACTION_NIL;
    }

    @Override
    public Types.ACTIONS act(String stateObsStr, ElapsedCpuTimer elapsedTimer) {
        return null;
    }
}
