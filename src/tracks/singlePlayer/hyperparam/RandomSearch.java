package tracks.singlePlayer.hyperparam;

import java.util.*;

public class RandomSearch {
    public int nbPoint = 3000;
    public int resampling = 100;
    public GameEvaluation gameEvaluation;
    public double[][] points;
    public Random rdm = new Random();
    public int validationTimes = 2000;
    public RandomSearch(int gameId, int levelId) {
        gameEvaluation = new GameEvaluation(gameId, levelId);


    }

    public static void main(String[] args) {
        int gameId = 0;
        int levelId = 0;
        /** Get arguments */
        Map<String, List<String>> params = new HashMap<>();
        List<String> options = null;
        for (int i = 0; i < args.length; i++) {
            final String a = args[i];
            if (a.charAt(0) == '-') {
                if (a.length() < 2) {
                    System.err.println("Error at argument " + a);
                    return;
                }
                options = new ArrayList<>();
                params.put(a.substring(1), options);
            } else if (options != null) {
                options.add(a);
            }
            else {
                System.err.println("Illegal parameter usage");
                return;
            }
        }
        /** Update params */
        if (params.containsKey("gameId")) {
            gameId = Integer.parseInt(params.get("gameId").get(0));
        }
        if (params.containsKey("levelId")) {
            levelId = Integer.parseInt(params.get("levelId").get(0));
        }
        RandomSearch rs = new RandomSearch(gameId, levelId);
        rs.optimise();
    }

    public void optimise() {
        double bestFitness = Double.NEGATIVE_INFINITY;
        double[] bestPoint = new double[gameEvaluation.dim];
        for (int i=0;i<nbPoint;i++) {
            double[] points = gameEvaluation.generateRandomPoint();
            double newFitness = gameEvaluation.evaluate(points, resampling);
            if (newFitness >= bestFitness) {
                bestFitness = newFitness;
                bestPoint = points;
            }
        }
        // validation
        double validationFitness = gameEvaluation.evaluate(bestPoint, validationTimes);
        System.out.println("VALIDATION " + validationFitness + " " + printArray(bestPoint));
    }

    public String printArray(double[] points) {
        String str = "";
        for (int i=0;i<points.length-1;i++) {
            str += points[i] + ",";
        }
        str += points[points.length-1];
        return str;
    }
}
