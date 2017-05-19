package tracks.singlePlayer.advanced.nestedMC;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;

import java.util.ArrayList;
import java.util.Arrays;
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

    public static int nestDepth = 2;
    public static int maxNestingDepth = 5;
    public static int maxRolloutLength = 20;

    int[] lengthBestRollout;
    double[] scoreBestRollout;
    Types.ACTIONS[][] bestRollout;
//    static int maxLegalMoves = Types.ACTIONS.values().length; // TODO: 17/05/17 should not use this

    double bestScoreNested = 1000000;

    public int minRemainingTime = 5;
    public double epsilon = 1e-6;
    Types.ACTIONS[] bestPrediction;
    public double bestScoreSoFar;
    
    public int maxModelCalls = 20000;
    public int nbModelCalls;
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
        bestRollout = new Types.ACTIONS[maxNestingDepth][maxRolloutLength];
        for (int i=0; i<maxNestingDepth; i++) {
            for (int j=0; j<maxRolloutLength; j++) {
                bestRollout[i][j] = Types.ACTIONS.ACTION_NIL;
            }
        }
        lengthBestRollout = new int[maxNestingDepth];
        scoreBestRollout = new double[maxNestingDepth];
        System.out.println(Arrays.toString(actions));
    }

    public void shiftBestPlayout() {
        int i=0;
        for (;i<maxRolloutLength-1;i++){
            bestPrediction[i] = bestPrediction[i+1];
        }
        bestPrediction[i] = Types.ACTIONS.ACTION_NIL;
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
        nbModelCalls=0;
        String str2 = "After shifted: actions are ";
        for (int j = 0; j < 10; j++) {
            str2 += bestPrediction[j] + ",";
        }
//        System.out.println(str2);

        //Set the state observation object as the new root of the tree.
        // we'll set up a game adapter and run the algorithm independently each
        // time at least to being with
        StateObservation obs = stateObs.copy();
        // move the below to constructor
//        bestRollout = new Types.ACTIONS[maxNestingDepth][maxRolloutLength];
//        for (int i=0; i<maxNestingDepth; i++) {
//            for (int j=0; j<maxRolloutLength; j++) {
//                bestRollout[i][j] = Types.ACTIONS.ACTION_NIL;
//            }
//        }
//        lengthBestRollout = new int[maxNestingDepth];
//        scoreBestRollout = new double[maxNestingDepth];


        //nested(obs, nestDepth, moveSeq, 0);
//        double bestScore = -bestScoreNested;
        Types.ACTIONS bestAction = actions[0];
        bestAction = actions[0];
        int nestedIdx=0;
        boolean finished = false;
        while (elapsedTimer.remainingTimeMillis()>minRemainingTime && nestedIdx<maxNestingDepth
            && nbModelCalls < maxModelCalls) {
            Types.ACTIONS tmpBestAction = actions[0];
            double bestScore = -bestScoreNested;
            for (int i=0; i<num_actions; i++) {

                StateObservation state = obs.copy();
                Types.ACTIONS[] moveSeqCopy = new Types.ACTIONS[maxRolloutLength];
                int nActionsPlayed = 0;
                state.advance(actions[i]);
                nbModelCalls++;
                moveSeqCopy[nActionsPlayed] = actions[i];
                nActionsPlayed++;
                if (nestedIdx > 0) {
                    finished = nested(state, nestedIdx, moveSeqCopy, nActionsPlayed, elapsedTimer);
                } else {
                    finished = playout(state, moveSeqCopy, 0, elapsedTimer);
                }
                if (finished) {
                    double score = score(state);
                    if (score > bestScore){// || (score==bestScore && random.nextDouble()>0.5)) {
                        bestScore = score;
                        tmpBestAction = actions[i];
//                        if (bestScore>bestScoreSoFar) {
//                            bestPrediction[nActionsPlayed] = tmpBestAction;
//                        }
                        if (score>bestScoreSoFar){// || (score==bestScoreSoFar && random.nextDouble()>0.5)) {
//                        if (bestScore>bestScoreSoFar ) {
                            for (int j = 0; j < maxRolloutLength; j++) {
                                bestPrediction[j] = moveSeqCopy[j];
                            }
                            String str = "score=" +bestScore+ ", actions are ";
                            for (int j = 0; j < 10; j++) {
                                str += bestPrediction[j] + ",";
                            }
//                            System.out.println(str);
//                          bestPrediction = bestRollout[nestingLevel].clone();
                            bestScoreSoFar = bestScore;
                        }
                    }
                }
            }
            if (finished || nestedIdx==0) {
//                System.out.println("previous bestAction is " + bestAction + " and new one is "
//                    + tmpBestAction + " with nestedIdx="+nestedIdx);
                bestAction = tmpBestAction;
            }
            nestedIdx++;
        }
//        System.out.println("BestAction is " + bestAction);
//        return bestAction;
//        System.out.println(bestPrediction.toString() + " bestScore=" + bestScoreSoFar);
        System.out.println("Move:"+bestPrediction[0]);
        System.out.println("nbModelCalls used =" + nbModelCalls);
        return bestPrediction[0];
    }


    boolean playout(StateObservation stateObservation, Types.ACTIONS[] moveSeq, int nActionsPlayed,
        ElapsedCpuTimer elapsedTimer) {
        // Types.ACTIONS[] moveSeq = new Types.ACTIONS[maxRolloutLength];

        while (!stateObservation.isGameOver() && nActionsPlayed < maxRolloutLength) {
            if (elapsedTimer.remainingTimeMillis() < minRemainingTime || nbModelCalls>=maxModelCalls)
                return false;
            ArrayList<Types.ACTIONS> availableActions = stateObservation.getAvailableActions();
            int move = random.nextInt(availableActions.size());
            moveSeq[nActionsPlayed] = availableActions.get(move);
            stateObservation.advance(moveSeq[nActionsPlayed]);
            nbModelCalls++;
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
        float res;

//        double avgTimeTaken = 0;
//        double acumTimeTaken = 0;
//        long remaining = elapsedTimer.remainingTimeMillis();
//        int numIters = 0;
//        int remainingLimit = 5;
        while (true) {
//            ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();
            if (stateObservation.isGameOver())
                return true;
            if (nActionsPlayed >= maxRolloutLength)
                return true;
            if (elapsedTimer.remainingTimeMillis() < minRemainingTime || nbModelCalls+nbMoves*maxNestingDepth>=maxModelCalls)
                return false;
            //return board.score ();
            for (int i = 0; i < nbMoves; i++) {
                StateObservation state = stateObservation.copy();
                Types.ACTIONS[] moveSeqCopy = new Types.ACTIONS[maxRolloutLength];
                int nActionsCopy = nActionsPlayed;
                for (int j = 0; j < nActionsPlayed; j++)
                    moveSeqCopy[j] = moveSeq[j];
                if (nestingLevel == 1) {
                    moveSeqCopy[nActionsCopy] = availableActions.get(i);
                    state.advance(moveSeqCopy[nActionsCopy]);
                    nbModelCalls++;
                    nActionsCopy++;
                    playout(state, moveSeqCopy, nActionsCopy, elapsedTimer);
                } else {
                    moveSeqCopy[nActionsCopy] = availableActions.get(i);
                    state.advance(moveSeqCopy[nActionsCopy]);
                    nbModelCalls++;
                    nActionsCopy++;
                    nested(state, nestingLevel - 1, moveSeqCopy, nActionsCopy, elapsedTimer);
                }
                if (elapsedTimer.remainingTimeMillis() < minRemainingTime)
                    return false;
                double score = score(state);
                if (score > scoreBestRollout[nestingLevel]) {
                    //System.out.println ("level " + nestingLevel + "score " + score);
                    scoreBestRollout[nestingLevel] = score;
                    lengthBestRollout[nestingLevel] = maxRolloutLength;
                    for (int j = 0; j < maxRolloutLength; j++) {
                        bestRollout[nestingLevel][j] = moveSeqCopy[j];

                    }
//                    System.out.println("score=" + score + ", bestScoreSoFar="+bestScoreSoFar);
                }
            }
//            if (bestRollout[nestingLevel][nActionsPlayed] == null) {
//                stateObservation.advance(Types.ACTIONS.ACTION_NIL);
//                moveSeq[nActionsPlayed] = Types.ACTIONS.ACTION_NIL;
//            } else {
                stateObservation.advance(bestRollout[nestingLevel][nActionsPlayed]);
                nbModelCalls++;
                moveSeq[nActionsPlayed] = bestRollout[nestingLevel][nActionsPlayed];
//            }
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
