package controllers.Return42.heuristics.features;

import java.util.Random;

import controllers.Return42.GameStateCache;

public abstract class Feature extends SelfDetectingFeature {
    protected double weight = 1.0;

    protected double baseWeight = 1.0;
    protected static final Random rnd = new Random();
    protected double multi = Math.pow(-1, rnd.nextInt(2));
    protected static final double maxFactor = 10;

    public abstract double evaluate(GameStateCache state);

    public double evaluateFeature(GameStateCache newState, GameStateCache oldState) {
        return evaluate(newState) * weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
        baseWeight = weight;
    }

    public void adjustWeight(double factor) {
        if(Math.abs(weight) > baseWeight * maxFactor) {
            multi *= -1;
        }
        double a = rnd.nextDouble() * factor * multi;
        weight += a;
    }
}
