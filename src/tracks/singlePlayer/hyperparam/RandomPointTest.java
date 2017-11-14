package tracks.singlePlayer.hyperparam;

import java.util.*;

public class RandomPointTest {

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

        int resampling = 100;
        GameEvaluation gameEvaluation = new GameEvaluation(gameId, levelId);
        double[] points = gameEvaluation.generateRandomPoint();
        double fitness = gameEvaluation.evaluate(points, resampling);
    }
}
