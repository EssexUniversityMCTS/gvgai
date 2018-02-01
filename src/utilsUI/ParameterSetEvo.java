package utilsUI;

import static utilsUI.Constants.*;

/**
 * Created by rdgain on 3/20/2017.
 */
public class ParameterSetEvo extends ParameterSet {

    public Integer POPULATION_SIZE = 5;
    public Integer CROSSOVER_TYPE = UNIFORM_CROSS; // 0 - 1point; 1 - uniform
    public Boolean REEVALUATE = false;
    public Integer MUTATION = 1;
    public Integer TOURNAMENT_SIZE = 2;
    public Integer NO_PARENTS = 2;
    public Integer RESAMPLE = 1;
    public Integer ELITISM = 1;

    public boolean canCrossover() {
        return POPULATION_SIZE > 1;
    }

    public boolean canTournament() {
        return POPULATION_SIZE > TOURNAMENT_SIZE;
    }

    public boolean isRMHC() { return POPULATION_SIZE == 1; }

    @Override
    public String[] getParamNames() {
        return new String[]{"Population size", "Simulation depth", "Heuristic type", "Crossover type", "Reevaluate individuals",
        "N genes mutated", "Tournament size", "N parents", "Resample rate", "Elitism", "Discount reward"};
    }

    @Override
    public Object[] getParams() {
        return new Object[]{POPULATION_SIZE, SIMULATION_DEPTH, HEURISTIC_TYPE, CROSSOVER_TYPE, REEVALUATE, MUTATION,
                TOURNAMENT_SIZE, NO_PARENTS, RESAMPLE, ELITISM, DISCOUNT_FACTOR};
    }

    @Override
    public String[] getValues() {
        return new String[]{""+POPULATION_SIZE, ""+SIMULATION_DEPTH, ""+HEURISTIC_TYPE, ""+CROSSOVER_TYPE,
                ""+REEVALUATE, ""+MUTATION, ""+TOURNAMENT_SIZE, ""+NO_PARENTS, ""+RESAMPLE, ""+ELITISM,
                ""+DISCOUNT_FACTOR};
    }

    @Override
    public Object[][] getValueOptions() {
        Object[][] opts = new Object[getParams().length][];
        opts[0] = new Integer[0]; //pop size, any int
        opts[1] = new Integer[0]; //sim depth, any int
        opts[2] = new Integer[]{HEURISTIC_WINSCORE, HEURISTIC_SIMPLESTATE};
        opts[3] = new Integer[]{POINT1_CROSS, UNIFORM_CROSS};
        opts[4] = new Boolean[]{false, true}; //reeval
        opts[5] = new Integer[0]; //no genes mutated
        opts[6] = new Integer[0]; //tournament size
        opts[7] = new Integer[0]; //no parents
        opts[8] = new Integer[0]; //no resamples
        opts[9] = new Integer[0]; //elitsim
        opts[10] = new Double[0]; //discount reward
        return opts;
    }

    @Override
    public String toString() {
        String s = "";

        String heur = "none";
        if (HEURISTIC_TYPE == HEURISTIC_WINSCORE) heur = "WinScore";
        else if (HEURISTIC_TYPE == HEURISTIC_SIMPLESTATE) heur = "SimpleState";

        String cross = "none";
        if (CROSSOVER_TYPE == UNIFORM_CROSS) cross = "uniform";
        else if (CROSSOVER_TYPE == POINT1_CROSS) cross = "1-Point";

        s += "---------- PARAMETER SET ----------\n";
        s += String.format("%1$-20s", "Population size") + ": " + POPULATION_SIZE + "\n";
        s += String.format("%1$-20s", "Individual length") + ": " + SIMULATION_DEPTH + "\n";
        s += "\n";
        s += String.format("%1$-20s", "Resampling") + ": " + RESAMPLE + "\n";
        s += String.format("%1$-20s", "Heuristic") + ": " + heur + "\n";
        s += String.format("%1$-20s", "Value discount") + ": " + DISCOUNT_FACTOR + "\n";
        s += String.format("%1$-20s", "Elitism") + ": " + ELITISM + "\n";
        s += String.format("%1$-20s", "Reevaluate?") + ": " + REEVALUATE + "\n";
        s += "\n";
        s += String.format("%1$-20s", "Tournament size") + ": " + TOURNAMENT_SIZE + "\n";
        s += String.format("%1$-20s", "Crossover type") + ": " + cross + "\n";
        s += String.format("%1$-20s", "Genes mutated") + ": " + MUTATION + "\n";
        s += "---------- ------------- ----------\n";

        return s;
    }
}
