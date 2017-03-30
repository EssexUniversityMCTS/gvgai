package shiftBufferRHCA.evodef;


import core.game.StateObservation;
import ontology.Types;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by sml on 20/01/2017.
 */
public class GameActionSpaceAdapter implements FitnessSpace {
    StateObservation stateObservation;
    int sequenceLength;
    EvolutionLogger logger;
    int nEvals;
    public static boolean useDiscountFactor = true;
    public static boolean useHeuristic = true;
    static Random random = new Random();
    static double noiseLevel = 0;

    public int numActions;
    public Types.ACTIONS[] gvgaiActions;


    // this is used to value future rewards less
    // than immediate ones via an exponential decay
    double discountFactor = 0.99;


    /**
     * For now assume that the number of actions available at each game tick is always
     * the same and may be found with a call to stateObservation
     *
     * @param stateObservation
     * @param sequenceLength
     */
    public GameActionSpaceAdapter(StateObservation stateObservation, int sequenceLength) {
        this.stateObservation = stateObservation;
        this.sequenceLength = sequenceLength;

        ArrayList<Types.ACTIONS> act = stateObservation.getAvailableActions();
        gvgaiActions = new Types.ACTIONS[act.size()];
        for(int i = 0; i < gvgaiActions.length; ++i)
        {
            gvgaiActions[i] = act.get(i);
        }
        numActions = gvgaiActions.length;
        logger = new EvolutionLogger();
        nEvals = 0;
    }


    @Override
    public int nDims() {
        return sequenceLength;
    }

    @Override
    public int nValues(int i) {
        // we assume that the nummber of actions available (and hence number of possible
        // values at each point in the search space
        // is always the same
        return numActions;
    }

    @Override
    public void reset() {
        // no action is needed apart from resetting the count;
        // the state is defined by the stateObservation that is passed to this
        logger.reset();
        nEvals = 0;
    }

    @Override
    public double evaluate(int[] actions) {
        // take a copy of the current game state and accumulate the score as we go along

        StateObservation obs = stateObservation.copy();
        // note the score now - for normalisation reasons
        // we wish to track the change in score, not the absolute score
        double initScore = obs.getGameScore();
        double discount = 1.0;
        double denom = 0;
        double discountedTot = 0;

        for (int i=0; i<sequenceLength; i++) {
            obs.advance(gvgaiActions[actions[i]]);
            discountedTot += discount * (obs.getGameScore() - initScore);
//            if (useHeuristic && obs instanceof SpaceBattleLinkState) {
//                SpaceBattleLinkState state = (SpaceBattleLinkState) obs;
//                discountedTot += state.getHeuristicScore();
//            }
            denom += discount;
            discount *= discountFactor;
        }

        nEvals++;
        double delta;
        if (useDiscountFactor) {
            delta = discountedTot / denom;
        } else {
            delta = obs.getGameScore() - initScore;
        }
        delta += noiseLevel * random.nextGaussian();
        return delta;
    }

    @Override
    public double pairEvaluate(int[] bits, int[] oppBits) {
        return 0;
    }

    @Override
    public boolean optimalFound() {
        return false;
    }

    @Override
    public SearchSpace searchSpace() {
        return this;
    }

    @Override
    public int nEvals() {
        return nEvals;
    }

    @Override
    public EvolutionLogger logger() {
        return logger;
    }

    @Override
    public Double optimalIfKnown() {
        return null;
    }
}
