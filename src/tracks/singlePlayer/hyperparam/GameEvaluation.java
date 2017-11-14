package tracks.singlePlayer.hyperparam;

import tools.Utils;
import tracks.ArcadeMachine;
import tracks.singlePlayer.hyperparam.newmcts.SingleTreeNode;

import java.util.Random;

public class GameEvaluation {
    static final String SP_GAMES_COLLECTION =  "examples/all_games_sp.csv";
    static final String[][] GAMES = Utils.readGames(SP_GAMES_COLLECTION);
    static String sampleMCTSController = "tracks.singlePlayer.hyperparam.newmcts.Agent";
    static double[][] bounds;
    public int dim = 2;
    int gameId = 0;
    int levelId = 0;

    public void setBounds() {
        bounds = new double[2][2];
        bounds[0][0] = 0;
        bounds[0][1] = 1;
        bounds[1][0] = 1;
        bounds[1][1] = 50;
        dim = 2;
    }

    public GameEvaluation() {
        setBounds();
    }

    public GameEvaluation(int gameId, int levelId) {
        this.gameId = gameId;
        this.levelId = levelId;
        setBounds();
    }

    public double[] playGame(double[] params) {
        String gameName = GAMES[gameId][1];
        String game = GAMES[gameId][0];
        String level = game.replace(gameName, gameName + "_lvl" + levelId);
//        System.out.println(level);
        int seed = new Random().nextInt();
        SingleTreeNode.K = params[0];
        SingleTreeNode.ROLLOUT_DEPTH = (int) params[1];

        // win/loss, score, game ticks
        double[] results = ArcadeMachine.runOneGame(game, level, false, sampleMCTSController, null, seed, 0);
        return results;
    }

    public double evaluate(double[] params, int resampling) {
        double sumScore = 0.0;
        System.out.println("PARAM K:" + (params[0] + 1) + " ROLLOUTDEPTH:" + (int) params[1]);
        String str = "RESULTS";
        double win = 0;
        for (int i=0;i<resampling;i++) {
            double[] results = playGame(params);
            win += results[0];
            sumScore += results[1];
            str += " " + results[0] + ":" + results[1] + ":" + results[2] + "";
        }
        double fitness = (1000 * win + sumScore) / resampling;
        str += " " + fitness;
        System.out.println(str);
        return fitness;
    }

    public double[] generateRandomPoint() {
        double[] point = new double[bounds.length];
        Random rdm = new Random();
        for (int i=0;i<bounds.length;i++) {
            point[i] = rdm.nextDouble()*(bounds[i][1]-bounds[i][0]) + bounds[i][0];
        }
        return point;
    }

}
