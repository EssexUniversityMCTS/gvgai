package tracks.singlePlayer.advanced.sampleRS;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tracks.singlePlayer.tools.Heuristics.StateHeuristic;
import tracks.singlePlayer.tools.Heuristics.WinScoreHeuristic;

import java.util.*;

public class Agent extends AbstractPlayer {

    // variable
    private int SIMULATION_DEPTH = 10;
    private double DISCOUNT = 1; //0.99;

    // constants
    private final long BREAK_MS = 10;
    public static final double epsilon = 1e-6;

    private ArrayList<Individual> population;
    private int NUM_INDIVIDUALS;
    private HashMap<Integer, Types.ACTIONS> action_mapping;
    private int N_ACTIONS;

    private ElapsedCpuTimer timer;
    private Random randomGenerator;

    private StateHeuristic heuristic;
    private double acumTimeTakenEval = 0,avgTimeTakenEval = 0;
    private int numEvals = 0;
    private long remaining;

    /**
     * Public constructor with state observation and time due.
     *
     * @param stateObs     state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        randomGenerator = new Random();
        heuristic = new WinScoreHeuristic(stateObs);
        this.timer = elapsedTimer;

        // INITIALISE POPULATION
        init_pop(stateObs);
    }

    @Override
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        this.timer = elapsedTimer;
        numEvals = 0;
        acumTimeTakenEval = 0;
        remaining = timer.remainingTimeMillis();
        NUM_INDIVIDUALS = 0;

        // INITIALISE POPULATION
        init_pop(stateObs);

        // RETURN ACTION
        Types.ACTIONS best = get_best_action(population);
        return best;
    }


    /**
     * Evaluates an individual by rolling the current state with the actions in the individual
     * and returning the value of the resulting state; random action chosen for the opponent
     * @param individual - individual to be valued
     * @param heuristic - heuristic to be used for state evaluation
     * @param state - current state, root of rollouts
     * @return - value of last state reached
     */
    private double evaluate(Individual individual, StateHeuristic heuristic, StateObservation state) {

        ElapsedCpuTimer elapsedTimerIterationEval = new ElapsedCpuTimer();

        StateObservation st = state.copy();
        int i;
        for (i = 0; i < SIMULATION_DEPTH; i++) {
            double acum = 0, avg;
            if (! st.isGameOver()) {
                ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();
                st.advance(action_mapping.get(individual.actions[i]));
                acum += elapsedTimerIteration.elapsedMillis();
                avg = acum / (i+1);
                remaining = timer.remainingTimeMillis();
                if (remaining < 2*avg || remaining < BREAK_MS) break;
            } else {
                break;
            }
        }

        StateObservation first = st.copy();
        double value = heuristic.evaluateState(first);

        // Apply discount factor
        value *= Math.pow(DISCOUNT,i);

        individual.value = value;

        numEvals++;
        acumTimeTakenEval += (elapsedTimerIterationEval.elapsedMillis());
        avgTimeTakenEval = acumTimeTakenEval / numEvals;
        remaining = timer.remainingTimeMillis();

        return value;
    }


    /**
     * Insert a new individual into the population at the specified position by replacing the old one.
     * @param newind - individual to be inserted into population
     * @param pop - population
     * @param idx - position where individual should be inserted
     * @param stateObs - current game state
     */
    private void add_individual(Individual newind, Individual[] pop, int idx, StateObservation stateObs) {
        evaluate(newind, heuristic, stateObs);
        pop[idx] = newind.copy();
    }

    /**
     * Initialize population
     * @param stateObs - current game state
     */
    private void init_pop(StateObservation stateObs) {

        double remaining;

        N_ACTIONS = stateObs.getAvailableActions().size() + 1;
        action_mapping = new HashMap<>();
        int k = 0;
        for (Types.ACTIONS action : stateObs.getAvailableActions()) {
            action_mapping.put(k, action);
            k++;
        }
        action_mapping.put(k, Types.ACTIONS.ACTION_NIL);

        NUM_INDIVIDUALS = 0;

        population = new ArrayList<>();
        do {
            Individual newInd = new Individual(SIMULATION_DEPTH, N_ACTIONS, randomGenerator);
            evaluate(newInd, heuristic, stateObs);
            population.add(newInd);
            remaining = timer.remainingTimeMillis();
            NUM_INDIVIDUALS++;

        } while(remaining > avgTimeTakenEval && remaining > BREAK_MS);

        if (NUM_INDIVIDUALS > 1)
            Collections.sort(population, new Comparator<Individual>() {
                @Override
                public int compare(Individual o1, Individual o2) {
                    if (o1 == null && o2 == null) {
                        return 0;
                    }
                    if (o1 == null) {
                        return 1;
                    }
                    if (o2 == null) {
                        return -1;
                    }
                    return o1.compareTo(o2);
                }});
    }

    /**
     * @param pop - last population obtained after evolution
     * @return - first action of best individual in the population (found at index 0)
     */
    private Types.ACTIONS get_best_action(ArrayList<Individual> pop) {
        int bestAction = pop.get(0).actions[0];
        return action_mapping.get(bestAction);
    }

}
