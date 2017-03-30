package shiftBufferRHCA.ntuple;


/**
 * Created by simonmarklucas on 27/11/2016.
 */
public class UrnModel {
//
//    public static void main(String[] args) {
//
//        ElapsedTimer t = new ElapsedTimer();
//        // facTest(200);
//        //System.out.println(t);
//        // System.exit(0);
//
//        int nBalls = 50;
//        int nPicks = 170;
//
//
//        UrnModel ut = new UrnModel(nBalls, nPicks);
//
//
//
//
//        ut.pWin(3);
//
//        ut.normaliseUrnProbs(ut.jpa);
//
//        // ut.pWin(3);
//
//        ut.printWinProbs();
//
//        ut.printWinRates();
//
//        System.out.println("Calculated Urn posteriors");
//        System.out.println(t);
//
//        // System.exit(0);
//
//        for (int nBlack = 0; nBlack <= nPicks; nBlack++) {
//            // System.out.println("nWins = " + nBlack);
//            ut.posteriorProbs(nBlack);
//        }
//
//    }

    int nBalls, nUrns, nPicks;
    double[][] jpa;

    double[][] pairProbs;
    Double probAsupB;
    // Double probEqual


    public UrnModel(int nBalls, int nPicks) {
        this.nBalls = nBalls;
        nUrns = nBalls + 1; // e.g. 10 balls, 11 urns, 0 .. 10.
        this.nPicks = nPicks;
        jointProbArray();
    }

    public void printWinProbs() {
        // iterate over the number of balls observed
        for (int i=0; i<=nBalls; i++) {
            System.out.format("%d \t %.4f\n", i, pWin(i));
        }
    }

    public double pWin(int nWins) {
        // iterate over all Urns

        double p = 0;
        double denom = 0;
        for (int i=0; i<nUrns; i++) {
            // for each Urn look up the probability given the number of wins
            // and multiply it by the win prob for that Urn
            denom += jpa[nWins][i];
            p += jpa[nWins][i] * ((double) i / nBalls);
        }
        // System.out.println(p + "\t " + p / denom);
        return p / denom;
    }


    public void printWinRates() {
        // iterates over each observation
        for (int i = 0; i <= nPicks; i++) {
            // in each case it must be in one of the urns
            // so normalise
            double tot = 0;
            double supTot = 0;
            for (int j = 0; j < nUrns; j++) {
                tot += jpa[i][j];
                if (j > nUrns / 2) {
                    supTot += jpa[i][j];
                }
                if (j == nUrns / 2) supTot += jpa[i][j] / 2;
            }

            System.out.println("Tot = " + tot);
            System.out.format("%2d wins, \t %.5f\n", i, supTot / tot);
        }
    }


    public void normaliseUrnProbs() {
        normaliseUrnProbs(jpa);
    }

    public void normaliseUrnProbs(double[][] pa) {
        for (int n = 0; n < nPicks + 1; n++) {
            double tot = 0;
            for (int u = 0; u < nUrns; u++) {

                tot += pa[n][u];
            }
            for (int u = 0; u < nUrns; u++) {

                pa[n][u] /= tot;
            }

        }
    }

    public double[] getUrnVec(int nWins) {
        return jpa[nWins];
    }

    // set a min value for the probability of an Joint Probability Array entry before normalisation
    static double eps = 1e-10;

    double jointProb(int urn, int nBlack) {

        double fu = (double) urn / nBalls;
        // System.out.format("\n urn %d, p(black) = %f\n", urn, fu);
        double jp = (1.0 / (nUrns)) * choose(nBlack, nPicks) * Math.pow(fu, nBlack) *
            Math.pow((1 - fu), nPicks - nBlack);
//        System.out.format("JP: urn: %d, \t nBlack: %d, \t nPicks: %d, \t jp: %f\n",
//                urn, nBlack, nPicks, jp);
//        System.out.println(Math.pow(0, 0));
//        System.out.println(nBalls - nBlack);



        return Math.max(jp, eps);
    }

    private void jointProbArray() {
        double pTot = 0;
        jpa = new double[nPicks + 1][nUrns];
        for (int nBlack = 0; nBlack <= nPicks; nBlack++) {
            for (int urn = 0; urn < nUrns; urn++) {
                jpa[nBlack][urn] = jointProb(urn, nBlack);
                pTot += jpa[nBlack][urn];
                // System.out.format("%d \t %d \t %f\n", nBlack, urn, jpa[nBlack][urn]);
            }
        }
        // System.out.println("Sanity check, pTot = " + pTot);
    }

    public void posteriorProbs(int nBlack) {
        double pnb = 0;
        for (int urn = 0; urn < jpa[0].length; urn++) {
            pnb += jpa[nBlack][urn];
        }
        // System.out.println("pnb = " + pnb);
        double pCum = 0;
        for (int urn = 0; urn < jpa[0].length; urn++) {
            double pu = jpa[nBlack][urn] / pnb;
            pCum += pu;
            // System.out.format("%d \t %f %f %f\n", urn, pu, pCum, 1-(pCum-pu));
        }
    }

    static double choose(int n, int m) {
        // ways of choosing n items from a bag of m items without replacement
        double res = fac(m) / (fac(m - n) * fac(n));
        // System.out.format("Choosing %d from %d: nWays = %f\n", n, m, res);
        return res;
    }

    static double quickChoose(int n, int m) {
        // ways of choosing n items from a bag of m items without replacement
        double res = stirlingFac(m) / (stirlingFac(m - n) * stirlingFac(n));
        // System.out.format("Choosing %d from %d: nWays = %f\n", n, m, res);
        return res;
    }

    public static double stirlingFac(double n) {
        return Math.sqrt(2 * Math.PI * n) * Math.pow(n / Math.E, n);
    }


    public static double fac(int n) {
        if (n < 1) return 1;
        double f = 1;
        for (int i = 2; i <= n; i++) {
            f *= i;
        }
        return f;
    }
}
