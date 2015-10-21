package controllers.Return42.heuristics;

import ontology.Types;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import controllers.Return42.GameStateCache;
import controllers.Return42.heuristics.features.CompareFeature;
import controllers.Return42.heuristics.features.KillNpc;
import controllers.Return42.heuristics.features.NextStepEnemy;
import controllers.Return42.heuristics.features.controller.FeatureController;

public class NextStepHeuristic implements CompareHeuristic {

    public double maxWeight = 100.0;
    private final List<CompareFeature> features;
    private final List<FeatureController> controller = new ArrayList<FeatureController>();
    private double weightMultiplier;
    private final Random rnd = new Random();

    private static final double winScore = Double.POSITIVE_INFINITY;
    private static final double lossScore = Double.NEGATIVE_INFINITY;

    public NextStepHeuristic(GameStateCache stateObs, boolean randomize) {
        List<CompareFeature> f = new ArrayList<CompareFeature>();

        f.add(new NextStepEnemy(10));
        f.add(new KillNpc(1));
        //f.add(new FacingEnemy(1));
        features = ComplexHeuristic.getUsefulFeatures(f, stateObs);

        double sum = 0;
        for(CompareFeature feature : features) {
            if(randomize) {
                feature.setWeight(Math.pow(rnd.nextDouble(), 2.0) * 10.0);
            }
            sum += feature.getWeight();
        }
        weightMultiplier = maxWeight / sum;
    }

    @Override
    public double evaluate(GameStateCache newState, GameStateCache oldState) {

        // Win / Loss

        if(newState.isGameOver()) {
            Types.WINNER win = newState.getGameWinner();
            if(win == Types.WINNER.PLAYER_LOSES)
                return lossScore;
            else if(win == Types.WINNER.PLAYER_WINS)
                return winScore;
        }

        for(FeatureController c : controller) {
            c.check(newState);
        }

        double score = 0;

        for(CompareFeature feature : features) {
            score += feature.evaluate(newState, oldState);// + rnd.nextDouble() * 0.00001;
        }

        return score * weightMultiplier;
    }

    @Override
    public List<CompareFeature> getFeatures() {
        return features;
    }

    @Override
    public List<FeatureController> getController() {
        return controller;
    }
}
