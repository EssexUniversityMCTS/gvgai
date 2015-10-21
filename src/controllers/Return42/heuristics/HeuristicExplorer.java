package controllers.Return42.heuristics;

import core.game.StateObservation;
import ontology.Types;

import java.util.List;

import controllers.Return42.GameStateCache;

public class HeuristicExplorer {

    private final CompareHeuristic heuristic;

    public HeuristicExplorer(CompareHeuristic heuristic) {
        this.heuristic = heuristic;
    }

    /**
     * Simulates a game with the given feature
     * @param stateObs current state
     * @param steps number of steps to simulate
     * @return return the gamescore after the cache
     */
    public double getScoreAfter(GameStateCache stateObs, int steps) {
        List<Types.ACTIONS> actions = stateObs.getAvailableActions();
        StateObservation state = stateObs.getState().copy();
        GameStateCache cache = new GameStateCache(state);
        for(int i=0; i<steps; i++) {
            StateObservation bestA = null;
            double bestScore = Double.NEGATIVE_INFINITY;
            for(Types.ACTIONS action : actions) {
                StateObservation s = state.copy();
                s.advance(action);
                double score = heuristic.evaluate(new GameStateCache(s), cache);
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
