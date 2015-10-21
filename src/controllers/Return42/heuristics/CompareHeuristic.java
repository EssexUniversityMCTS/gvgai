package controllers.Return42.heuristics;

import java.util.List;

import controllers.Return42.GameStateCache;
import controllers.Return42.heuristics.features.CompareFeature;
import controllers.Return42.heuristics.features.controller.FeatureController;

public interface CompareHeuristic {
    public double evaluate(GameStateCache newState, GameStateCache oldState);

    public List<CompareFeature> getFeatures();

    public List<FeatureController> getController();
}
