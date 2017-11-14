package tracks.singlePlayer.hyperparam;

import tools.Utils;
import tracks.ArcadeMachine;

public class GridSearch {
    int nbPoints = 10;
    double shrinkageCoeff = 0.5;
    int dim = 2;
    public GameEvaluation gameEvaluation;
    public double[][] bounds;
    public int resampling = 10;

    public GridSearch() {
        gameEvaluation = new GameEvaluation();
        bounds = new double[dim][2];
        setBounds();
    }

    public void setBounds() {
        bounds[0][0] = 0;
        bounds[0][1] = 5;
        bounds[1][0] = 1;
        bounds[1][1] = 50;
    }

    public GridSearch(int nbPoints, double shrinkageCoeff, double[][] bounds) {
        this.nbPoints = nbPoints;
        this.shrinkageCoeff = shrinkageCoeff;
        this.bounds = bounds;
        this.dim = bounds.length;
    }

    public void optimise(int budget) {
        // find and evaluate points
        int totalNbPoints = (int) Math.pow(nbPoints, dim);
        int totalEvals = 0;
        while (totalEvals <= budget + totalNbPoints) {
            double[] bestPoint = generatePoint(0);
            double bestFit = gameEvaluation.evaluate(bestPoint, resampling);
            for (int i=1; i<totalNbPoints; i++) {
                double[] newPoint = generatePoint(i);
                double newFitness = gameEvaluation.evaluate(newPoint, resampling);
                if (newFitness > bestFit) {
                    bestPoint = newPoint;
                    bestFit = newFitness;
                }
            }
            for (int d=0;d<dim;d++) {
                double bestVal = bestPoint[d];
                if (bestVal <= bounds[d][0]) {
                    bounds[d][0] = bestVal;
                }
                if (bestVal >= bounds[d][1]) {
                    bounds[d][1] = bestVal;
                }
                if (bounds[d][0] < bestVal) {
                    bounds[d][0] = bestVal - shrinkageCoeff*(bounds[d][1]-bounds[d][0])/2;
                }
                if (bounds[d][1] > bestVal) {
                    bounds[d][1] = bestVal + shrinkageCoeff*(bounds[d][1]-bounds[d][0])/2;
                }
            }
            totalEvals += totalNbPoints;
            System.out.println("OPT bestFit:" + bestFit + " bestPoint:" + bestPoint.toString() + " totalEvals:" + totalEvals);

        }

    }

    public double[][] generatePossibleValues() {
        double[][] possibleValues = new double[dim][nbPoints];
        for (int d=0;d<dim;d++) {
            double lowerBound = bounds[d][0];
            double upperBound = bounds[d][1];
            for (int i=0;i<nbPoints;i++) {
                possibleValues[d][i] = lowerBound + i*(upperBound-lowerBound)/(nbPoints -1);
            }
        }
        return possibleValues;
    }


    public double[] generatePoint(int idx) {
        double[] point = new double[dim];
        for (int d=0;d<dim;d++) {
            int base = (int) Math.pow(nbPoints,dim-d-1);
            int i = (int) (idx / base);
            point[d] = bounds[d][0] + i*(bounds[d][1]-bounds[d][0])/(nbPoints -1);
            idx -= i*base;
        }
        return point;
    }
    public static void main(String[] args) {
        int budget = 1000000;
        GridSearch gs = new GridSearch();
        gs.optimise(budget);
    }

}

