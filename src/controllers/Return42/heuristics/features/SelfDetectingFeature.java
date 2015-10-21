package controllers.Return42.heuristics.features;

import java.util.Random;

import controllers.Return42.GameStateCache;

public abstract class SelfDetectingFeature implements CompareFeature {
    protected int counter = 0;
    protected int deactivate = 100;
    protected int recheck = 1000;
    protected double weight = 1.0;

    protected double baseWeight = 1.0;
    protected static final Random rnd = new Random();
    protected double multi = Math.pow(-1, rnd.nextInt(2));
    protected static final double maxFactor = 10;

    public double evaluate(GameStateCache newState, GameStateCache oldState) {
        if(counter > deactivate) {
            if(counter > recheck) {
                double score = evaluateFeature(newState, oldState);
                if(score == 0) {
                    System.out.println("inactive: " + this.getClass().getName());
                    counter = 100;
                } else {
                    System.out.println("reactivate: " + this.getClass().getName());
                    counter = 0;
                    return score;
                }
            }
            counter++;
            return 0;
        } else {
            double score = evaluateFeature(newState, oldState);
            if(score == 0) {
                counter++;
            } else {
                counter = 0;
            }
            return score;
        }
    }

    public void adjustWeight(double factor) {
        if(Math.abs(weight) > baseWeight * maxFactor) {
            multi *= -1;
        }
        double a = rnd.nextDouble() * factor * multi;
        weight += a;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    protected abstract double evaluateFeature(GameStateCache newState, GameStateCache oldState);
}
