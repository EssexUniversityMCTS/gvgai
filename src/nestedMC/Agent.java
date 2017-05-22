package nestedMC;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;

import java.util.ArrayList;
import java.util.Random;

/**
 * Tristan Cazenave's nestedMC
 *
 *
 */
public class Agent extends AbstractPlayer {
    static Random random = new Random();

    public Types.ACTIONS[] actions; // available actions at current game tick
    public int num_actions; // length of actions

    public static int maxNestingDepth = 5;
    public static int maxRolloutLength = 20;

    int[] lengthBestRollout;
    double[] scoreBestRollout;
    Types.ACTIONS[][] bestRollout;
    Types.ACTIONS[] moveSeqCopy;

    double bestScoreNested = 100000;

    public int minRemainingTime = 7;
    public double epsilon = 1e-6;
    Types.ACTIONS[] bestPrediction;
    public double bestScoreSoFar;
    
    /**
     * Public constructor with state observation and time due.
     *
     * @param so           state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer) {
        //Get the actions in a static array.
        ArrayList<Types.ACTIONS> act = so.getAvailableActions();
        actions = new Types.ACTIONS[act.size()];
        for (int i = 0; i < actions.length; ++i) {
            actions[i] = act.get(i);
        }
        num_actions = actions.length;
        bestScoreSoFar = -bestScoreNested;
        bestPrediction = new Types.ACTIONS[maxRolloutLength];
        for (int i=0;i<maxRolloutLength;i++) {
            bestPrediction[i] = Types.ACTIONS.ACTION_NIL;
        }
        moveSeqCopy = new Types.ACTIONS[maxRolloutLength];
        for (int i=0;i<moveSeqCopy.length;i++) {
            moveSeqCopy[i] = Types.ACTIONS.ACTION_NIL;
        }

        bestRollout = new Types.ACTIONS[maxNestingDepth][maxRolloutLength];
        for (int i=0; i<maxNestingDepth; i++) {
            for (int j=0; j<maxRolloutLength; j++) {
                bestRollout[i][j] = Types.ACTIONS.ACTION_NIL;
            }
        }
        lengthBestRollout = new int[maxNestingDepth];
        scoreBestRollout = new double[maxNestingDepth];
    }

    public void shiftBestPlayout() {
        int i=0;
        for (;i<maxRolloutLength-1;i++){
            bestPrediction[i] = bestPrediction[i+1];
        }
        bestPrediction[i] = bestPrediction[i-1];
    }

    public void resetMoveSeqCopy() {
        for (int i=0;i<moveSeqCopy.length;i++){
            moveSeqCopy[i] = Types.ACTIONS.ACTION_NIL;
        }
    }
    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     *
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        shiftBestPlayout();

        //Set the state observation object as the new root of the tree.
        // we'll set up a game adapter and run the algorithm independently each
        // time at least to being with
        StateObservation obs = stateObs.copy();

        Types.ACTIONS bestAction = actions[0];
        int nestedIdx=0;
        boolean finished = false;

        double avgTimeTaken = 0;
        double acumTimeTaken = 0;
        long remaining = elapsedTimer.remainingTimeMillis();
        int numIters = 0;
        while(remaining > 2*avgTimeTaken && remaining > minRemainingTime && nestedIdx<maxNestingDepth) {
            ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();
//            && nbModelCalls < maxModelCalls) {
            Types.ACTIONS tmpBestAction = actions[0];
            double bestScore = -bestScoreNested;
            for (int i=0; i<num_actions; i++) {
                StateObservation state = obs.copy();
//                resetMoveSeqCopy();
                int nActionsPlayed = 0;
                state.advance(actions[i]);
                moveSeqCopy[nActionsPlayed] = actions[i];
                nActionsPlayed++;
                if (nestedIdx > 0) {
                    finished = nested(state, nestedIdx, moveSeqCopy, nActionsPlayed, elapsedTimer);
                } else {
                    finished = playout(state, moveSeqCopy, 0, elapsedTimer);
                }
                if (finished) {
                    double score = score(state);
                    if (score > bestScore){
                        bestScore = score;
                        tmpBestAction = actions[i];
//                        if (bestScore>bestScoreSoFar) {
//                            bestPrediction[nActionsPlayed] = tmpBestAction;
//                        }
                        if (score>bestScoreSoFar){
//                        if (bestScore>bestScoreSoFar ) {
                            for (int j = 0; j < maxRolloutLength; j++) {
                                bestPrediction[j] = moveSeqCopy[j];
                            }
                            String str = "score=" +bestScore+ ", actions are ";
                            for (int j = 0; j < 10; j++) {
                                str += bestPrediction[j] + ",";
                            }
                            bestScoreSoFar = bestScore;
                        }
                    }
                }
            }
            if (finished || nestedIdx==0) {
                bestAction = tmpBestAction;
            }
            nestedIdx++;
            numIters++;
            acumTimeTaken += (elapsedTimerIteration.elapsedMillis()) ;
            avgTimeTaken  = acumTimeTaken/numIters;
            remaining = elapsedTimer.remainingTimeMillis();
        }
        return bestPrediction[0];
    }


    boolean playout(StateObservation stateObservation, Types.ACTIONS[] moveSeq, int nActionsPlayed,
        ElapsedCpuTimer elapsedTimer) {
        while (!stateObservation.isGameOver() && nActionsPlayed < maxRolloutLength) {
            if (elapsedTimer.remainingTimeMillis() <= minRemainingTime) {
                return false;
            }
            ArrayList<Types.ACTIONS> availableActions = stateObservation.getAvailableActions();
            int move = random.nextInt(availableActions.size());
            moveSeq[nActionsPlayed] = availableActions.get(move);
            stateObservation.advance(moveSeq[nActionsPlayed]);
            nActionsPlayed++;
        }
        return true;
    }

    boolean nested(StateObservation stateObservation, int nestingLevel, Types.ACTIONS[] moveSeq,
                int nActionsPlayed, ElapsedCpuTimer elapsedTimer) {
        ArrayList<Types.ACTIONS> availableActions = stateObservation.getAvailableActions();
        int nbMoves = availableActions.size();

        lengthBestRollout[nestingLevel] = -1;
        scoreBestRollout[nestingLevel] = -bestScoreNested;
        while(true) {
            if (stateObservation.isGameOver())
                return true;
            if (nActionsPlayed >= maxRolloutLength)
                return true;
            if (elapsedTimer.remainingTimeMillis() < minRemainingTime) {
                return false;
            }
            double avgTimeTaken = 0;
            double acumTimeTaken = 0;
            long remaining = elapsedTimer.remainingTimeMillis();
            int numIters = 0;
            int i=0;
            while(remaining > 2*avgTimeTaken && remaining > minRemainingTime && i<nbMoves) {
//            for (int i = 0; i < nbMoves; i++) {
                ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();
                StateObservation state = stateObservation.copy();
//                Types.ACTIONS[] moveSeqCopy = new Types.ACTIONS[maxRolloutLength];
//                resetMoveSeqCopy();
                int nActionsCopy = nActionsPlayed;
                for (int j = 0; j < nActionsPlayed; j++)
                    moveSeqCopy[j] = moveSeq[j];
                if (nestingLevel == 1) {
                    moveSeqCopy[nActionsCopy] = availableActions.get(i);
                    state.advance(moveSeqCopy[nActionsCopy]);
                    nActionsCopy++;
                    playout(state, moveSeqCopy, nActionsCopy, elapsedTimer);
                } else {
                    moveSeqCopy[nActionsCopy] = availableActions.get(i);
                    state.advance(moveSeqCopy[nActionsCopy]);
                    nActionsCopy++;
                    nested(state, nestingLevel - 1, moveSeqCopy, nActionsCopy, elapsedTimer);
                }
                if (elapsedTimer.remainingTimeMillis() < minRemainingTime)
                    return false;
                double score = score(state);
                if (score > scoreBestRollout[nestingLevel]) {
                    scoreBestRollout[nestingLevel] = score;
                    lengthBestRollout[nestingLevel] = maxRolloutLength;
                    for (int j = 0; j < maxRolloutLength; j++) {
                        bestRollout[nestingLevel][j] = moveSeqCopy[j];
                    }
                }
                i++;
                numIters++;
                acumTimeTaken += (elapsedTimerIteration.elapsedMillis()) ;
                avgTimeTaken  = acumTimeTaken/numIters;
                remaining = elapsedTimer.remainingTimeMillis();
            }
            stateObservation.advance(bestRollout[nestingLevel][nActionsPlayed]);
            moveSeq[nActionsPlayed] = bestRollout[nestingLevel][nActionsPlayed];
            nActionsPlayed++;

        }
    }

    public double score(StateObservation stateObs) {
        if (stateObs.isGameOver()) {
            Types.WINNER winner = stateObs.getGameWinner();
            if (winner == Types.WINNER.PLAYER_WINS) {
                return bestScoreNested;
            } else if (winner == Types.WINNER.PLAYER_LOSES) {
                return -bestScoreNested;
            }
        }
//        return stateObs.getGameScore();
        return (random.nextDouble()*epsilon + stateObs.getGameScore());
    }
}
