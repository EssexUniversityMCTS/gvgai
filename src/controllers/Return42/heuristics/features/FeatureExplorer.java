package controllers.Return42.heuristics.features;

import core.game.StateObservation;
import ontology.Types;

import java.util.List;

import controllers.Return42.GameStateCache;

public class FeatureExplorer {

    private final CompareFeature feature;

    public FeatureExplorer(CompareFeature feature) {
        this.feature = feature;
    }

    /**
     * Simulates a game with the given feature
     *
     * @param stateObs current state
     * @param steps    number of steps to simulate
     * @return return the gamescore after the cache
     */
    public double getScoreAfter(StateObservation stateObs, int steps) {
        List<Types.ACTIONS> actions = stateObs.getAvailableActions();
        StateObservation state = stateObs.copy();
        GameStateCache cache = new GameStateCache(state);
        for(int i = 0; i < steps; i++) {
            StateObservation bestA = null;
            double bestScore = Double.NEGATIVE_INFINITY;
            for(Types.ACTIONS action : actions) {
                StateObservation s = state.copy();
                s.advance(action);
                GameStateCache c = new GameStateCache(s);
                double score = feature.evaluate(c, cache);
                if(score >= bestScore) {
                    bestScore = score;
                    bestA = s;
                }
            }
            state = bestA;
        }
        return state.getGameScore();
    }
}
