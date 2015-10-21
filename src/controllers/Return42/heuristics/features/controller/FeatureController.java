package controllers.Return42.heuristics.features.controller;

import controllers.Return42.GameStateCache;

public abstract class FeatureController {
    protected boolean active = true;

    public boolean isActive() {
        return active;
    }

    public abstract boolean isUseful(GameStateCache state);

    public abstract void check(GameStateCache state);

    public void reactivate() {
        active = true;
    }
}
