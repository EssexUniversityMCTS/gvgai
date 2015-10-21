package controllers.Return42.heuristics;

import core.game.StateObservation;
import ontology.Types;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import controllers.Return42.GameStateCache;
import controllers.Return42.heuristics.features.CompareFeature;
import controllers.Return42.heuristics.features.controller.FeatureController;

public class SimpleHeuristic implements CompareHeuristic {

    protected Random rnd = new Random();
    protected double rndMod = 0.2;
    protected List<FeatureController> controller = new ArrayList<>();

    public double evaluate(GameStateCache newState, GameStateCache oldState) {
        if(newState.isGameOver()) {
            if(newState.getGameWinner() == Types.WINNER.PLAYER_LOSES) {
                return Double.NEGATIVE_INFINITY;
            } else if(newState.getGameWinner() == Types.WINNER.PLAYER_WINS) {
                return Double.POSITIVE_INFINITY;
            }
        }
        return newState.getGameScore() + rnd.nextDouble() * rndMod;
    }

    public List<CompareFeature> getFeatures() {
        return new ArrayList<>();
    }

    public List<FeatureController> getController() {
        return controller;
    }
}
