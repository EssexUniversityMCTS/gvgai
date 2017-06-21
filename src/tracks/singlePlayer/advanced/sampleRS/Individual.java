package tracks.singlePlayer.advanced.sampleRS;
import java.util.Random;

public class Individual implements Comparable{

    protected int[] actions; // actions in individual. length of individual = actions.length
    protected int n; // number of legal actions
    protected double value;
    private Random gen;

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
