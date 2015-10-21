package controllers.Return42.algorithms.GA.genomes;


import controllers.Return42.GameStateCache;
import controllers.Return42.heuristics.CompareHeuristic;
import core.game.StateObservation;
import ontology.Types;

public class MultiStepGenome extends DepthSurvivalGenome {

    public MultiStepGenome(int depth, Types.ACTIONS[] actions, CompareHeuristic heuristic) {
        super(depth, actions, heuristic);
    }

    @Override
    public double getScore(GameStateCache newState, GameStateCache oldstate) {
        if(Double.isNaN(score)) {
            StateObservation stateObs = newState.getState().copy();
            score = 0;
            for(int i = 0; i < depth; i++) {
                if(i == depth / 3) {
                    score += heuristic.evaluate(new GameStateCache(stateObs), oldstate) * 0.7f;
                }
                stateObs.advance(actions[genome[i]]);
                if(stateObs.isGameOver()) {
                    score = 0;
                    break;
                }
            }
            score += heuristic.evaluate(new GameStateCache(stateObs), oldstate) * 0.3f;
            //System.out.println(score);
        }
        return score;
    }
}