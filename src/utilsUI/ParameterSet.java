package utilsUI;

import static utilsUI.Constants.*;

/**
 * Created by rdgain on 3/20/2017.
 */
public class ParameterSet {

    public Integer SIMULATION_DEPTH = 10; //try 6,8,10
    public Double DISCOUNT_FACTOR = 1.0; //0.99;
    public Integer HEURISTIC_TYPE = HEURISTIC_WINSCORE;

    public String[] getParamNames(){
        return new String[]{"Simulation depth", "Heuristic type", "Discount reward"};
    }

    public String[] getValues(){return new String[]{""+SIMULATION_DEPTH, ""+HEURISTIC_TYPE, ""+DISCOUNT_FACTOR};
    }

    public Object[] getParams() {
        return new Object[]{SIMULATION_DEPTH, HEURISTIC_TYPE, DISCOUNT_FACTOR};
    }

    public Object[][] getValueOptions(){
        Object[][] opts = new Object[getParams().length][];
        opts[0] = new Integer[0]; //sim depth, any int
        opts[1] = new Integer[]{HEURISTIC_WINSCORE, HEURISTIC_SIMPLESTATE};
        opts[2] = new Double[0]; //discount reward
        return opts;
    }

    public String toString() {
        String s = "";

        String heur = "none";
        if (HEURISTIC_TYPE == HEURISTIC_WINSCORE) heur = "WinScore";
        else if (HEURISTIC_TYPE == HEURISTIC_SIMPLESTATE) heur = "SimpleState";

        s += "---------- PARAMETER SET ----------\n";
        s += String.format("%1$-20s", "Simulation depth") + ": " + SIMULATION_DEPTH + "\n";
        s += String.format("%1$-20s", "Heuristic") + ": " + heur + "\n";
        s += String.format("%1$-20s", "Value discount") + ": " + DISCOUNT_FACTOR + "\n";
        s += "---------- ------------- ----------\n";

        return s;
    }
}
