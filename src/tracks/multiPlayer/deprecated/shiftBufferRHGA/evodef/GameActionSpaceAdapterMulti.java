package tracks.multiPlayer.deprecated.shiftBufferRHGA.evodef;

import core.game.StateObservationMulti;
import ontology.Types;
import tracks.multiPlayer.deprecated.shiftBufferRHGA.evodef.EvolutionLogger;
import tracks.multiPlayer.deprecated.shiftBufferRHGA.evodef.FitnessSpace;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by sml on 20/01/2017.
 */
public class GameActionSpaceAdapterMulti implements FitnessSpace {
    StateObservationMulti stateObservation;
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

    int playerID;
    int opponentID;
    static final double PENALITY = -1000000;

    /**
     * For now assume that the number of actions available at each game tick is always
     * the same and may be found with a call to stateObservation
     *
     * @param stateObservation
     * @param sequenceLength
     */
    public GameActionSpaceAdapterMulti(StateObservationMulti stateObservation, int sequenceLength, int playerID, int opponentID) {
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
        this.playerID = playerID;
        this.opponentID = opponentID;
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

        StateObservationMulti obs = stateObservation.copy();
        // note the score now - for normalisation reasons
        // we wish to track the change in score, not the absolute score
        double initScore = obs.getGameScore(playerID);
        double discount = 1.0;
        double denom = 0;
        double discountedTot = 0;

        for (int i=0; i<sequenceLength; i++) {

            // Note here that we need to look at the advance method which takes multiple players
            // hence an array of actions
            // the idea is that we'll pad out the
            int myAction = actions[i];
            int opAction = random.nextInt(obs.getAvailableActions(opponentID).size());
            Types.ACTIONS[] acts = new Types.ACTIONS[2];
            acts[playerID] = gvgaiActions[myAction];
            acts[opponentID] = gvgaiActions[opAction];

            obs.advance(acts);

            discountedTot += discount * (obs.getGameScore(playerID) - initScore);

//            if (useHeuristic && obs instanceof SpaceBattleLinkStateTwoPlayer) {
//                SpaceBattleLinkStateTwoPlayer state = (SpaceBattleLinkStateTwoPlayer) obs;
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

    /**
     * Evaluation given the opponent's action sequence
     * @param actions
     * @param oppActions
     * @return
     */
    public double pairEvaluate(int[] actions, int[] oppActions) {
        assert (actions.length == oppActions.length);

        // take a copy of the current game state and accumulate the score as we go along
        StateObservationMulti obs = stateObservation.copy();
        // note the score now - for normalisation reasons
        // we wish to track the change in score, not the absolute score
        double initScore = heuristic(obs); // obs.getGameScore(playerID);

        double discount = 0.9;
        double denom = 0;
        double discountedTot = 0;

        for (int i=0; i<sequenceLength; i++) {

            // Note here that we need to look at the advance method which takes multiple players
            // hence an array of actions
            // the idea is that we'll pad out the
            int myAction = actions[i];
            int opAction = oppActions[i];
            Types.ACTIONS[] acts = new Types.ACTIONS[2];
            acts[playerID] = gvgaiActions[myAction];
            acts[opponentID] = gvgaiActions[opAction];

            obs.advance(acts);

//            discountedTot += discount * (heuristic(obs)-initScore);
            discountedTot += discount * heuristic(obs);

//            discountedTot += discount * (obs.getGameScore(playerID) - initScore);

//            if (useHeuristic && obs instanceof SpaceBattleLinkStateTwoPlayer) {
//                SpaceBattleLinkStateTwoPlayer state = (SpaceBattleLinkStateTwoPlayer) obs;
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

    public double heuristic(StateObservationMulti obs) {
        double value = obs.getGameScore(playerID);
        boolean gameOver = obs.isGameOver();
        Types.WINNER win = obs.getMultiGameWinner()[playerID];
        if(gameOver && win == Types.WINNER.PLAYER_LOSES)
            value += PENALITY;
        if(gameOver && win == Types.WINNER.PLAYER_WINS)
            value -= PENALITY;
        return value;
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
