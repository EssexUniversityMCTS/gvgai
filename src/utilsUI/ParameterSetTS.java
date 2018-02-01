package utilsUI;

import static utilsUI.Constants.*;

/**
 * Created by rdgain on 3/20/2017.
 */
public class ParameterSetTS extends ParameterSet {

    public Integer MCTS_ITERATIONS = 100;
    public Double K = Math.sqrt(2);

    @Override
    public String[] getParamNames() {
        return new String[]{"Simulation depth", "Heuristic type", "Discount reward", "MCTS Iterations", "K"};
    }

    @Override
    public String[] getValues() {
        return new String[]{""+SIMULATION_DEPTH, ""+HEURISTIC_TYPE, ""+DISCOUNT_FACTOR, ""+MCTS_ITERATIONS, ""+K};
    }

    @Override
    public Object[] getParams() {
        return new Object[]{SIMULATION_DEPTH, HEURISTIC_TYPE, DISCOUNT_FACTOR, MCTS_ITERATIONS, K};
    }

    @Override
    public Object[][] getValueOptions() {
        Object[][] opts = new Object[getParams().length][];
        opts[0] = new Integer[0]; //sim depth, any int
        opts[1] = new Integer[]{HEURISTIC_WINSCORE, HEURISTIC_SIMPLESTATE};
        opts[2] = new Double[0]; //discount reward
        opts[3] = new Integer[0]; //MCTS iterations
        opts[4] = new Double[0]; //K
        return opts;
    }

    @Override
    public String toString() {
        String s = "";

        String heur = "none";
        if (HEURISTIC_TYPE == HEURISTIC_WINSCORE) heur = "WinScore";
        else if (HEURISTIC_TYPE == HEURISTIC_SIMPLESTATE) heur = "SimpleState";

        s += "---------- PARAMETER SET ----------\n";
        s += String.format("%1$-20s", "Simulation depth") + ": " + SIMULATION_DEPTH + "\n";
        s += String.format("%1$-20s", "MCTS Iterations") + ": " + MCTS_ITERATIONS + "\n";
        s += "\n";
        s += String.format("%1$-20s", "Heuristic") + ": " + heur + "\n";
        s += String.format("%1$-20s", "Value discount") + ": " + DISCOUNT_FACTOR + "\n";
        s += String.format("%1$-20s", "K") + ": " + K + "\n";
        s += "---------- ------------- ----------\n";

        return s;
    }
}
