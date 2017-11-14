package tracks.singlePlayer.hyperparam;

import sun.nio.cs.ext.MacArabic;

public class Utils {
    static int winAward = 1000;
    static int penality = 1000;
    static double lambda = 5;

    public static double sigmoid(double v) {
        return 1 / (1 + Math.exp(lambda*v));
    }

    public static double normalise(double v, double bound) {
        return sigmoid(v/bound);
    }

    public static double rMapToZeroOne(double v) {
        return (0.5 + v / Math.sqrt(1+v*v) / 2);
    }

    public static double zeroOneMapToR(double v) {
        if (v < 1/2) {
            return 1 / v - 2;
        } else {
            return -1 / (1-v);
        }
    }

    public static double rMapToBounds(double v, double lowerBound, double upperBounder) {
        return rMapToZeroOne(v)*(upperBounder - lowerBound) + lowerBound;
    }

    public static double boundsMapToR(double v, double lowerBound, double upperBounder) {
        v = (v - lowerBound) / (upperBounder - lowerBound);
        if (v < 1/2) {
            return 1 / v - 2;
        } else {
            return -1 / (1-v);
        }
    }


}
