package controllers.Return42.algorithms.GA.genomes;

import controllers.Return42.heuristics.CompareHeuristic;
import ontology.Types;

public class MutantGenome extends Genome {

    public MutantGenome(int depth, Types.ACTIONS[] actions, CompareHeuristic heuristic) {
        super(depth, actions, heuristic);
    }

    @Override
    public void adapt(Genome other) {
        int d = Math.min(depth, other.getDepth());
        for(int i = 0; i < d; i++) {
            genome[i] = rnd.nextInt(N_ACTIONS);
        }
        score = Double.NaN;
    }
}
