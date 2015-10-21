package controllers.Return42.heuristics.features;


import java.util.HashMap;
import java.util.Random;

import controllers.Return42.GameStateCache;

public class Inventory implements CompareFeature {

    private final double invFactor;
    private double weight = 1.0;

    protected double baseWeight = 1.0;
    protected static final Random rnd = new Random();
    protected double multi = Math.pow(-1, rnd.nextInt(2));
    protected static final double maxFactor = 10;

    public Inventory(double invFactor) {
        this.invFactor = invFactor;
    }

    @Override
    public boolean isUseful(GameStateCache state) {
        return true;//state.getAvailableActions().size() > 3;
    }

    @Override
    public double evaluate(GameStateCache newState, GameStateCache oldState) {
        HashMap<Integer, Integer> newInv = newState.getAvatarResources();
        if(newInv != null) {
            return Math.abs(newInv.size() - oldState.getAvatarResources().size()) * invFactor;
        } else {
            System.out.println("no inv");
            return 0;
        }
    }

    public void adjustWeight(double factor) {
        if(Math.abs(weight) > baseWeight * maxFactor) {
            multi *= -1;
        }
        double a = rnd.nextDouble() * factor * multi;
        weight += a;
    }

    @Override
    public double getWeight() {
        return invFactor * weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
}