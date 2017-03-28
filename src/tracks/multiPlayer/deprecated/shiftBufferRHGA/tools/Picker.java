package tracks.multiPlayer.deprecated.shiftBufferRHGA.tools;

public class Picker<T> {
    // keeps just the best item so far

    public static void main(String[] args) {
        Picker<Integer> picker = new Picker<Integer>(Picker.MAX_FIRST);
        picker.add(2.0, 1);
        picker.add(6.0, 2);
        picker.add(1.1, 3);
        picker.add(5.0, 0);
        System.out.println(picker);
    }

    T bestYet;
    Double bestScore;
    public int nItems;
    //
    int order;
    public static int MAX_FIRST = 1;
    public static int MIN_FIRST = -1;
    // boolean strict = true;

    public Picker() {
        this(MAX_FIRST);
    }

    public Picker(int order) {
        this.order = order;
        reset();
    }

    public void add(double score, T value) {
        // each value must be unique: keep it in the set of values
        // and throw an exception if violated

        if (bestYet == null) {
            bestScore = score;
            bestYet = value;
        } else {
            // System.out.println(order * score + " >? " + bestScore * order + " : " + (order * score > bestScore * order));
            if (order * score > bestScore * order) {
                bestScore = score;
                bestYet = value;
            }
        }
        nItems++;
    }

    public T getBest() {
        return bestYet;
    }

    public Double getBestScore () {
        return bestScore;
    }

    public void reset() {
        nItems = 0;
        bestScore = (order == MAX_FIRST) ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
    }

    public String toString() {
        return "Picker: " + bestYet + " : " + bestScore;
    }
}
