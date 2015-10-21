package controllers.Return42.algorithms.GA.genomes;

import controllers.Return42.heuristics.CompareHeuristic;
import ontology.Types;

// idea: more important to alter later actions than the earlier ones
// the earlier ones are already tested and altered multiple times
public class DepthGenome extends Genome {

    private final double adaptFactor = 0.7; // goes down from here
    private final double mutateFactor = 1.0; // goes up to here

    public DepthGenome(int depth, Types.ACTIONS[] actions, CompareHeuristic heuristic) {
        super(depth, actions, heuristic);
    }

    @Override
    public void adapt(Genome other) {
        int d = Math.min(depth, other.getDepth());
        for(int i = 0; i < d; i++) {
            if(rnd.nextDouble() < adaptFactor - ((double) i / depth) * adaptFactor) {
                // adapt
                genome[i] = other.getAction(i);
            } else if(rnd.nextDouble() < ((i + 1.0) / depth) * mutateFactor) {
                // mutate
                genome[i] = rnd.nextInt(N_ACTIONS);
            }
        }
        score = Double.NaN;
    }
}
