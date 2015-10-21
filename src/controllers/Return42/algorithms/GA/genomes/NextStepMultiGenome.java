package controllers.Return42.algorithms.GA.genomes;

import controllers.Return42.GameStateCache;
import controllers.Return42.heuristics.CompareHeuristic;
import core.game.StateObservation;
import ontology.Types;

public class NextStepMultiGenome extends DepthSurvivalGenome {

    CompareHeuristic nextStepHeuristic;

    public NextStepMultiGenome(int depth, Types.ACTIONS[] actions, CompareHeuristic heuristic, CompareHeuristic nextStepHeuristic) {
        super(depth, actions, heuristic);
        this.nextStepHeuristic = nextStepHeuristic;
    }

    @Override
    public double getScore(GameStateCache newState, GameStateCache oldstate) {
        if(Double.isNaN(score)) {
            StateObservation stateObs = newState.getState().copy();
            score = 0;
            //System.out.println("--");
            for(int i = 0; i < depth; i++) {
                if(i == 1) {
                    score += nextStepHeuristic.evaluate(new GameStateCache(stateObs), oldstate);
                    //System.out.println(score);
                }
                if(i == depth / 3) {
                    score += heuristic.evaluate(new GameStateCache(stateObs), oldstate) * 0.6f;
                    //System.out.println(score);
                }
                stateObs.advance(actions[genome[i]]);
                if(stateObs.isGameOver()) {
                    score = 0;
                    break;
                }
            }
            score += heuristic.evaluate(new GameStateCache(stateObs), oldstate) * 0.4f;
            //System.out.println(score);
        }
        return score;
    }
    /*
    @Override
    public void adapt(Genome other) {
        if(other.score == Double.NEGATIVE_INFINITY) {
            int d = Math.min(depth, other.getDepth());
            for(int i = 1; i < d; i++) {
                genome[i] = rnd.nextInt(N_ACTIONS);
            }
            score = Double.NaN;
        } else {
            //double s = score;
            super.adapt(other);
            //if(s < 0) {
                genome[0] = rnd.nextInt(N_ACTIONS);
            //}
        }
    }*/
}