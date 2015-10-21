package controllers.Return42.algorithms.GA.genomes;

import core.game.StateObservation;
import ontology.Types;

import java.util.Random;

import controllers.Return42.GameStateCache;
import controllers.Return42.heuristics.CompareHeuristic;

public class Genome implements IGenome {

    protected final double RECPROB = 0.3;
    protected final double MUT = 0.6;

    protected int[] genome;
    protected int depth;
    protected final int N_ACTIONS;
    protected final Types.ACTIONS[] actions;
    protected final Random rnd = new Random();
    protected final CompareHeuristic heuristic;

    protected double score = Double.NaN;

    public Genome(int depth, Types.ACTIONS[] actions, CompareHeuristic heuristic) {
        this.depth = depth;
        this.heuristic = heuristic;
        this.actions = actions;
        this.N_ACTIONS = actions.length;
        genome = new int[depth];
        for(int i = 0; i < depth; i++) {
            genome[i] = rnd.nextInt(N_ACTIONS);
        }
    }

    public Types.ACTIONS getNextAction() {
        return actions[genome[0]];
    }

    public int getDepth() {
        return depth;
    }

    public void advance() {
        System.arraycopy(genome, 1, genome, 0, depth - 1);
        genome[depth - 1] = rnd.nextInt(N_ACTIONS);
        score = Double.NaN;
    }

    public int getAction(int index) {
        return genome[index];
    }

    public void adapt(Genome other) {
        int d = Math.min(depth, other.getDepth());
        for(int i = 0; i < d; i++) {
            if(rnd.nextDouble() < RECPROB) {
                // adapt
                genome[i] = other.getAction(i);
            } else if(rnd.nextDouble() < MUT) {
                // mutate
                genome[i] = rnd.nextInt(N_ACTIONS);
            }
        }
        score = Double.NaN;
    }

    public double getScore(GameStateCache stateObs, GameStateCache oldstate) {
        if(Double.isNaN(score)) {
            StateObservation so = stateObs.getState().copy();
            for(int i = 0; i < depth; i++) {
                so.advance(actions[genome[i]]);
                if(stateObs.isGameOver()) {
                    break;
                }
            }
            score = heuristic.evaluate(new GameStateCache(so), oldstate);
        }
        return score;
    }

    public void addDepth(int n) {
        depth += n;
        int[] s = genome;
        genome = new int[depth];
        for(int i = 0; i < depth; i++) {
            if(s.length + 1 < i) {
                genome[i] = s[i];
            } else {
                genome[i] = rnd.nextInt(N_ACTIONS);
            }
        }
    }
}
