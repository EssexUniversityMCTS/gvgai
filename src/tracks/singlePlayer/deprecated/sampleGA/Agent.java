package tracks.singlePlayer.deprecated.sampleGA;


import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeoutException;

import tracks.singlePlayer.tools.Heuristics.StateHeuristic;
import tracks.singlePlayer.tools.Heuristics.WinScoreHeuristic;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Utils;

/**
 * Created with IntelliJ IDEA.
 * User: ssamot
 * Date: 26/02/14
 * Time: 15:17
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Agent extends AbstractPlayer {

    private double GAMMA = 0.90;
    private long BREAK_MS = 5;
    private int SIMULATION_DEPTH = 7;
    private int POPULATION_SIZE = 5;

    private double RECPROB = 0.1;
    private double MUT = (1.0 / SIMULATION_DEPTH);
    private final int N_ACTIONS;

    private ElapsedCpuTimer timer;

    private int genome[][][];
    private final HashMap<Integer, Types.ACTIONS> action_mapping;
    private final HashMap<Types.ACTIONS, Integer> r_action_mapping;
    protected Random randomGenerator;

    private int numSimulations;

    /**
     * Public constructor with state observation and time due.
     *
     * @param stateObs     state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {

        randomGenerator = new Random();

        action_mapping = new HashMap<Integer, Types.ACTIONS>();
        r_action_mapping = new HashMap<Types.ACTIONS, Integer>();
        int i = 0;
        for (Types.ACTIONS action : stateObs.getAvailableActions()) {
            action_mapping.put(i, action);
            r_action_mapping.put(action, i);
            i++;
        }

        N_ACTIONS = stateObs.getAvailableActions().size();
        initGenome(stateObs);


    }


    double microbial_tournament(int[][] actionGenome, StateObservation stateObs, StateHeuristic heuristic) throws TimeoutException {
        int a, b, c, W, L;
        int i;


        a = (int) ((POPULATION_SIZE - 1) * randomGenerator.nextDouble());
        do {
            b = (int) ((POPULATION_SIZE - 1) * randomGenerator.nextDouble());
        } while (a == b);

        double score_a = simulate(stateObs, heuristic, actionGenome[a]);
        double score_b = simulate(stateObs, heuristic, actionGenome[b]);

        if (score_a > score_b) {
            W = a;
            L = b;
        } else {
            W = b;
            L = a;
        }

        int LEN = actionGenome[0].length;

        for (i = 0; i < LEN; i++) {
            if (randomGenerator.nextDouble() < RECPROB) {
                actionGenome[L][i] = actionGenome[W][i];
            }
        }


        for (i = 0; i < LEN; i++) {
            if (randomGenerator.nextDouble() < MUT) actionGenome[L][i] = randomGenerator.nextInt(N_ACTIONS);
        }

        return Math.max(score_a, score_b);

    }

    private void initGenome(StateObservation stateObs) {

        genome = new int[N_ACTIONS][POPULATION_SIZE][SIMULATION_DEPTH];


        // Randomize initial genome
        for (int i = 0; i < genome.length; i++) {
            for (int j = 0; j < genome[i].length; j++) {
                for (int k = 0; k < genome[i][j].length; k++) {
                    genome[i][j][k] = randomGenerator.nextInt(N_ACTIONS);
                }
            }
        }
    }


    private double simulate(StateObservation stateObs, StateHeuristic heuristic, int[] policy) throws TimeoutException {


        //System.out.println("depth" + depth);
        long remaining = timer.remainingTimeMillis();
        if (remaining < BREAK_MS) {
            //System.out.println(remaining);
            throw new TimeoutException("Timeout");
        }


        int depth = 0;
        stateObs = stateObs.copy();
        for (; depth < policy.length; depth++) {
            Types.ACTIONS action = action_mapping.get(policy[depth]);

            stateObs.advance(action);

            if (stateObs.isGameOver()) {
                break;
            }
        }

        numSimulations++;
        double score = Math.pow(GAMMA, depth) * heuristic.evaluateState(stateObs);
        return score;


    }

    private Types.ACTIONS microbial(StateObservation stateObs, int maxdepth, StateHeuristic heuristic, int iterations) {

        double[] maxScores = new double[stateObs.getAvailableActions().size()];

        for (int i = 0; i < maxScores.length; i++) {
            maxScores[i] = Double.NEGATIVE_INFINITY;
        }


        outerloop:
        for (int i = 0; i < iterations; i++) {
            for (Types.ACTIONS action : stateObs.getAvailableActions()) {


                StateObservation stCopy = stateObs.copy();
                stCopy.advance(action);

                double score = 0;
                try {
                    score = microbial_tournament(genome[r_action_mapping.get(action)], stCopy, heuristic) + randomGenerator.nextDouble()*0.00001;
                } catch (TimeoutException e) {
                    break outerloop;
                }

                try {
                    int int_act = this.r_action_mapping.get(action);

                    if (score > maxScores[int_act]) {
                        maxScores[int_act] = score;
                    }
                }catch (Exception e){}

            }
        }

        Types.ACTIONS maxAction = this.action_mapping.get(Utils.argmax(maxScores));


        return maxAction;

    }

    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     *
     * @param stateObs     Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {

        this.timer = elapsedTimer;
        numSimulations = 0;

        Types.ACTIONS lastGoodAction = microbial(stateObs, SIMULATION_DEPTH, new WinScoreHeuristic(stateObs), 100);

        return lastGoodAction;
    }


    @Override
    public void draw(Graphics2D g)
    {
        //g.drawString("Num Simulations: " + numSimulations, 10, 20);
    }
}
