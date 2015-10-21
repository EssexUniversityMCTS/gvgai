package controllers.Return42.algorithms.GA.genomes;

import controllers.Return42.GameStateCache;
import core.game.StateObservation;
import ontology.Types;

public class EmergencyGenome extends Genome {
    public EmergencyGenome(int depth, Types.ACTIONS[] actions) {
        super(depth, actions, null);
    }

    @Override
    public double getScore(GameStateCache newState, GameStateCache oldstate) {
        for(int i = 0; i < actions.length; i++) {
            StateObservation n = oldstate.getState().copy();
            n.advance(actions[i]);
            if(n.isGameOver()) {
                /*if(n.getGameWinner() != Types.WINNER.PLAYER_LOSES) {
                    genome[0] = i;
                    return 1;
                } */
            } else {
                genome[0] = i;
                return 1;
            }
        }
        return 0;
    }
}
