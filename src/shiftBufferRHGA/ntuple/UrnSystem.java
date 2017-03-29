package shiftBufferRHGA.ntuple;


/**
 * Created by simonmarklucas on 27/11/2016.
 */

public class UrnSystem {

//    public static void main(String[] args) {
//        ElapsedTimer t = new ElapsedTimer();
//        UrnSystem us = new UrnSystem();
//        System.out.println(t);
//        double nWins = 15;
//        double nPicks = 15;
////        nWins *=2;
////        nPicks *= 2;
//
//        BarChart bc = new BarChart();
//        bc.update(us.pVec(nWins, nPicks));
//        new JEasyFrame(bc, nWins + " / " + nPicks);
//        System.out.println(Arrays.toString(us.pVec(nWins, nPicks)));
//    }

    static int nBalls = 100;

    UrnModel[] models;

    int maxPicks = 100;

    public UrnSystem() {
        init();
    }

    public void init() {
        models = new UrnModel[maxPicks+1];
        for (int i=1; i<=maxPicks; i++) {
            models[i] = new UrnModel(nBalls, i);
            models[i].normaliseUrnProbs();
        }
    }

    public double[] pVec(double nWins, double nPicks) {
        if (nPicks > maxPicks) {
            nWins *= maxPicks / nPicks;
            nPicks = maxPicks;
        }
        // in the case of a double being passed, need to be smart
        return pVec((int) nWins, (int) nPicks);
    }


    public double[] pVec(int nWIns, int nPicks) {



        // first of all find the right UrnModel
        // then get the right column from it

        UrnModel model = models[nPicks];
        // return

        return model.getUrnVec(nWIns);

    }


    public double[] getPrior() {
        double[] p = new double[nBalls+1];
        double x = 1.0 / (nBalls+1.0);
        for (int i=0; i<p.length; i++) {
            p[i] = x;
        }
        System.out.println("nUrns in prior = " + p.length);
        return p;
    }
}
