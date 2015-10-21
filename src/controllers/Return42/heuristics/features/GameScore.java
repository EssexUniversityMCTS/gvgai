package controllers.Return42.heuristics.features;

import controllers.Return42.GameStateCache;

public class GameScore extends Feature {

    private final double gameScoreFactor;

    public GameScore(double gameScoreFactor) {
        this.gameScoreFactor = gameScoreFactor;
        deactivate = Integer.MAX_VALUE;
    }

    @Override
    public boolean isUseful(GameStateCache state) {
        return true;
    }

    @Override
    public double evaluate(GameStateCache state) {
        return state.getGameScore() * gameScoreFactor;
    }

    @Override
    public double getWeight() {
        return gameScoreFactor * weight;
    }
}
