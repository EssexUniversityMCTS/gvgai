package controllers.Return42.heuristics.features;

import controllers.Return42.GameStateCache;
import ontology.Types;

public class Gameover extends Feature {

    @Override
    public boolean isUseful(GameStateCache state) {
        return true;
    }

    @Override
    public double evaluate(GameStateCache state) {
        if(state.isGameOver()) {
            return state.getGameWinner() == Types.WINNER.PLAYER_WINS ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
        } else {
            return 0;
        }
    }

    @Override
    public double getWeight() {
        return 0;
    }
}
