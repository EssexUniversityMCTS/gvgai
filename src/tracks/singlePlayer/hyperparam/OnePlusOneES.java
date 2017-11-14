package tracks.singlePlayer.hyperparam;

import java.util.*;

public class OnePlusOneES {
    public int dim = 2;
    public Random rdm = new Random();
    public double sigma;
    public double[] parent;
    public GameEvaluation gameEvaluation;
    public double[][] bounds;
    public int resampling = 100;

    public OnePlusOneES() {
        initParent();
        initSigma();
        gameEvaluation = new GameEvaluation();
        bounds = new double[dim][2];
        setBounds();
    }

    public OnePlusOneES(int gameId, int levelId, int resampling) {
        this.resampling = resampling;
        initParent();
        initSigma();
        gameEvaluation = new GameEvaluation(gameId, levelId);
        bounds = new double[dim][2];
        setBounds();
    }

    public void setBounds() {
        bounds[0][0] = 0;
        bounds[0][1] = 1;
        bounds[1][0] = 1;
        bounds[1][1] = 50;
    }

    public void initParent() {
        parent = new double[dim];
        for (int i=0;i<dim;i++) {
            parent[i] = 1/Math.sqrt(dim); // * (bounds[i][1] - bounds[i][0])/2;
        }
    }

    public void initSigma() {
        sigma = 1/Math.sqrt(dim);
    }

    public double[] mapToSpace(double[] v) {
        double[] mapped = new double[v.length];
        for (int i=0;i<mapped.length;i++) {
            mapped[i] = Utils.rMapToBounds(v[i] ,bounds[i][0], bounds[i][1]);
        }
        return mapped;
    }


    public void optimise(int budget) {
        double fitParent = gameEvaluation.evaluate(mapToSpace(parent), resampling);
        int totalEvals = resampling;
        int parentEvals = resampling;
        double[] offspring = new double[dim];
        double fitOffspring = Double.NEGATIVE_INFINITY;
        while (totalEvals <= budget - resampling*2) {
            for (int i=0;i<dim;i++) {
                offspring[i] = parent[i] + sigma * rdm.nextGaussian();
            }
            fitOffspring = gameEvaluation.evaluate(mapToSpace(offspring), resampling);
            totalEvals += resampling;

            fitParent = (fitParent + gameEvaluation.evaluate(mapToSpace(parent), resampling)) / 2 ;
            System.out.println("OPT fitParent:" + fitParent + " fitOffspring:" + fitOffspring + " totalEvals:" + totalEvals);
            if (fitOffspring >= fitParent) {
                parent = offspring;
                fitParent = fitOffspring;
                sigma = sigma*2;
                parentEvals = resampling;
            } else {
                sigma = sigma*0.84;
                parentEvals += resampling;
            }
        }
    }

    public static void main(String[] args) {
        int budget = 1000000;
        int gameId = 0;
        int levelId = 0;
        int resampling = 100;
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
        if (params.containsKey("budget")) {
            budget = Integer.parseInt(params.get("budget").get(0));
        }
        if (params.containsKey("levelId")) {
            levelId = Integer.parseInt(params.get("levelId").get(0));
        }
        if (params.containsKey("resampling")) {
            resampling = Integer.parseInt(params.get("resampling").get(0));
        }

//        if (args.length>=1) {
//            gameId = Integer.parseInt(args[0]);
//            if (args.length >= 2) {
//                levelId = Integer.parseInt(args[1]);
//                if (args.length >= 3) {
//                    budget = Integer.parseInt(args[2]);
//                }
//            }
//        }
        OnePlusOneES opoes = new OnePlusOneES(gameId, levelId, resampling);
        System.out.println("OPOES optimises MCTS parameters for game " + gameId + " level " + levelId);
        opoes.optimise(budget);
    }
}
