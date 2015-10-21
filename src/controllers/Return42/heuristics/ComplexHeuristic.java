package controllers.Return42.heuristics;

import core.game.StateObservation;
import ontology.Types;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import controllers.Return42.GameStateCache;
import controllers.Return42.heuristics.features.*;
import controllers.Return42.heuristics.features.controller.FeatureController;
import controllers.Return42.heuristics.features.controller.PortalController;

public class ComplexHeuristic implements CompareHeuristic {

    private static final double gameScoreFactor = 2.0;

    private static final double portalDistanceFactor = 0.05;

    private static final double npcDistanceFactor = 0;
    private static final double npcCounterFactor = 1.0;

    private static final double resDistanceFactor = 0.1;
    private static final double resCounterFactor = 2.0;

    private static final double movDistanceFactor = 0;
    private static final double movCounterFactor = 1.0;

    private static final double immovDistanceFactor = 0.1;
    private static final double immovCounterFactor = 2.0;

    private static final double walkDistanceFactor = 0.1;

    private static final double inventoryFactor = 1.0;

    private static final double winScore = Double.POSITIVE_INFINITY;
    private static final double lossScore = Double.NEGATIVE_INFINITY;

    private static final double maxWeight = 100.0;

    //the overall weight should be equal with different heuristics to be comparable
    private double weightMultiplier;

    private final List<CompareFeature> features;
    private final List<FeatureController> controller;

    private Random rnd = new Random();

    public ComplexHeuristic(GameStateCache stateObs, boolean randomize) {
        List<CompareFeature> f = new ArrayList<>();
        List<FeatureController> c = new ArrayList<>();

        PortalController pc = new PortalController();
        c.add(pc);


        f.add(new HuntNpc(npcDistanceFactor, npcCounterFactor));
        f.add(new FleeNpc(npcDistanceFactor, npcCounterFactor));

        f.add(new PortalDistance(portalDistanceFactor, pc));
        f.add(new ResourceCollection(resDistanceFactor, resCounterFactor));
        f.add(new GameScore(gameScoreFactor));
        f.add(new Moving(walkDistanceFactor));
        //f.add(new WannabeHeatMap(stateObs, walkDistanceFactor));
        f.add(new Inventory(inventoryFactor));

        // hunt nearly always wins - either score equal or higher. no kill = no gamescore change. resource gathering while fleeing or hunt is random and should not be counted as indication
        //f.add(getSuitableFeature(new HuntNpc(npcDistanceFactor, npcCounterFactor), new FleeNpc(npcDistanceFactor, npcCounterFactor), stateObs, 40));

        // both rather useless and not worth the extra time.
        //f.add(getSuitableFeature(new SeekMovables(movDistanceFactor, movCounterFactor), new SeekMovables(-movDistanceFactor, movCounterFactor), stateObs, 40));

        f.add(new SeekMovables(movDistanceFactor, movCounterFactor));

        //zelda-like keys
        f.add(new SeekImmovables(immovDistanceFactor, immovCounterFactor));

        controller = getUsefulController(c, stateObs);
        features = getUsefulFeatures(f, stateObs);


        double sum = 0;
        for(CompareFeature feature : features) {
            if(randomize) {
                feature.setWeight(Math.pow(rnd.nextDouble(), 2.0) * 10.0);
            }
            sum += feature.getWeight();
        }
        weightMultiplier = maxWeight / sum;
    }

    /**
     * Evaluates all available positions and distances as well as the distance the player moved
     *
     * @param newState current gamestate
     * @param oldState state before simulations
     * @return evaluated score
     */
    public double evaluate(GameStateCache newState, GameStateCache oldState) {

        // Win / Loss

        if(newState.isGameOver()) {
            Types.WINNER win = newState.getGameWinner();
            if(win == Types.WINNER.PLAYER_LOSES)
                return lossScore;
            else if(win == Types.WINNER.PLAYER_WINS)
                return winScore;
        }

        double score = 0;
        //System.out.println("---");

        for(CompareFeature feature : features) {
            score += feature.evaluate(newState, oldState);// + rnd.nextDouble() * 0.1;
            //System.out.println(feature.getWeight());
        }

        //adjustRndFeature();

        return score * weightMultiplier;
    }

    private void adjustRndFeature() {
        features.get(rnd.nextInt(features.size())).adjustWeight(0.01);
    }

    public List<CompareFeature> getFeatures() {
        return features;
    }

    public List<FeatureController> getController() {
        return controller;
    }

    /**
     * Simulates two features against each other to determine which is more useful.
     * Used mainly to decide which actions to take e.g. flee or hunt npcs
     *
     * @param a     first feature
     * @param b     second feature
     * @param state current state
     * @param steps number of steps to simulate
     */
    public static CompareFeature getSuitableFeature(CompareFeature a, CompareFeature b, StateObservation state, int steps) {
        double scoreA = new FeatureExplorer(a).getScoreAfter(state, steps);
        double scoreB = new FeatureExplorer(b).getScoreAfter(state, steps);
        if(scoreA >= scoreB) {
            System.out.println("A is better " + scoreA + ":" + scoreB);
            return a;
        } else {
            System.out.println("B is better " + scoreA + ":" + scoreB);
            return b;
        }
        //return scoreA >= scoreB ? a : b;
    }

    /**
     * Simulate to check if features are applicable for the game
     *
     * @param f        List of features to check
     * @param stateObs current state
     * @param steps    steps to simulate
     * @return List of features that are useful
     */
    public static List<CompareFeature> getUsefulFeatures(List<CompareFeature> f, GameStateCache stateObs) {
        List<CompareFeature> features = new ArrayList<>();
        GameStateCache cache = stateObs.getFutureCache(40);
        for(CompareFeature feat : f) {
            if(feat.isUseful(cache)) {
                features.add(feat);
            }
        }
        return features;
    }

    /**
     * Simulate to check if features are applicable for the game
     *
     * @param f        List of features to check
     * @param stateObs current state
     * @param steps    steps to simulate
     * @return List of features that are useful
     */
    public static List<FeatureController> getUsefulController(List<FeatureController> f, GameStateCache stateObs) {
        List<FeatureController> features = new ArrayList<>();
        GameStateCache cache = stateObs.getFutureCache(40);
        for(FeatureController feat : f) {
            if(feat.isUseful(cache)) {
                features.add(feat);
            }
        }
        return features;
    }
}