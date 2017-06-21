package tracks.singlePlayer.advanced.sampleRHEA;
import java.util.Random;

public class Individual implements Comparable{

    protected int[] actions; // actions in individual. length of individual = actions.length
    protected int n; // number of legal actions
    protected double value;
    private Random gen;

    private boolean MUT_BIAS = false;

    public Individual(int L, int n, Random gen) {
        actions = new int[L];
        for (int i = 0; i < L; i++) {
            actions[i] = gen.nextInt(n);
        }
        this.n = n;
        this.gen = gen;
    }

    public void setActions (int[] a) {
        System.arraycopy(a, 0, actions, 0, a.length);
    }

    /**
     * Returns new individual
     * @param MUT
     * @return
     */
    public Individual mutate (int MUT) {
        Individual b = this.copy();
        b.setActions(actions);

        int count = 0;
        if (n > 1) { // make sure you can actually mutate
            while (count < MUT) {

                int a; // index of action to mutate

                // random mutation of one action
                a = gen.nextInt(b.actions.length);

                int s;
                s = gen.nextInt(n); // find new action
                b.actions[a] = s;

                count++;
            }
        }

        return b;
    }

    /**
     * Modifies individual
     * @param cross
     * @param CROSSOVER_TYPE
     */
    public void crossover (Individual[] cross, int CROSSOVER_TYPE) {
        if (CROSSOVER_TYPE == Agent.POINT1_CROSS) {
            // 1-point
            int p = gen.nextInt(actions.length - 3) + 1;
            for ( int i = 0; i < actions.length; i++) {
                if (i < p)
                    actions[i] = cross[0].actions[i];
                else
                    actions[i] = cross[1].actions[i];
            }

        } else if (CROSSOVER_TYPE == Agent.UNIFORM_CROSS) {
            // uniform
            for (int i = 0; i < actions.length; i++) {
                actions[i] = cross[gen.nextInt(cross.length)].actions[i];
            }
        }
    }

    @Override
    public int compareTo(Object o) {
        Individual a = this;
        Individual b = (Individual)o;
        if (a.value < b.value) return 1;
        else if (a.value > b.value) return -1;
        else return 0;
    }

    @Override
    public boolean equals(Object o) {
        Individual a = this;
        Individual b = (Individual)o;

        for (int i = 0; i < actions.length; i++) {
            if (a.actions[i] != b.actions[i]) return false;
        }

        return true;
    }

    public Individual copy () {
        Individual a = new Individual(this.actions.length, this.n, this.gen);
        a.value = this.value;
        a.setActions(this.actions);

        return a;
    }

    @Override
    public String toString() {
        String s = "" + value + ": ";
        for (int i = 0; i < actions.length; i++)
            s += actions[i] + " ";
        return s;
    }
}
