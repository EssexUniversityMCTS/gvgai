package controllers.Return42.algorithms.GA.genomes;

import controllers.Return42.heuristics.CompareHeuristic;
import ontology.Types;

// idea: like DepthGenome but mutate more when score from other genome sucks
// avoids instant death in games like Frogs with Action.UP which gets adapted instead of discarded as first action
public class DepthSurvivalGenome extends DepthGenome {

    public DepthSurvivalGenome(int depth, Types.ACTIONS[] actions, CompareHeuristic heuristic) {
        super(depth, actions, heuristic);
    }

    @Override
    public void adapt(Genome other) {
        if(other.score == Double.NEGATIVE_INFINITY) {
            int d = Math.min(depth, other.getDepth());
            for(int i = 0; i < d; i++) {
                genome[i] = rnd.nextInt(N_ACTIONS);
            }
            score = Double.NaN;
        } else {
            super.adapt(other);
        }
    }
}