package controllers.Return42.heuristics.features;

import controllers.Return42.GameStateCache;

public class Moving extends SelfDetectingFeature {

    private final double moveFactor;

    public Moving(double gameScoreFactor) {
        this.moveFactor = gameScoreFactor;
    }

    @Override
    public boolean isUseful(GameStateCache state) {
        return true;//state.getAvailableActions().size() > 3;
    }

    @Override
    protected double evaluateFeature(GameStateCache newState, GameStateCache oldState) {
        return (newState.getAvatarPosition().dist(oldState.getAvatarPosition()) / newState.getBlockSize()) * moveFactor;
    }

    @Override
    public double getWeight() {
        return moveFactor * weight;
    }
}