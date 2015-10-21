package controllers.Return42.algorithms.pattern;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;

import java.util.ArrayList;
import java.util.List;

import controllers.Return42.GameStateCache;
import controllers.Return42.heuristics.patterns.EnemyPattern;
import controllers.Return42.heuristics.patterns.Pattern;

public class Agent extends AbstractPlayer {
    List<Pattern> patterns = new ArrayList<>();
    AbstractPlayer fallback;

    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer) {
        patterns.add(new EnemyPattern());
        patterns = getUsefulPaterns(patterns, new GameStateCache(so));
        fallback = new controllers.Return42.algorithms.GA.Agent(so, elapsedTimer);
    }

    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer)
    {
        GameStateCache state = new GameStateCache(stateObs);
        Pattern max = null;
        Double score = Double.NEGATIVE_INFINITY;
        for(Pattern p : patterns) {
            double s = p.applies(state);
            if(s > score) {
                score = s;
                max = p;
            }
        }
        if(score > 0) {
            return max.getAction();
        } else {
            System.out.println("No pattern matches using GA");
            return fallback.act(stateObs, elapsedTimer);
        }
    }

    public static List<Pattern> getUsefulPaterns(List<Pattern> f, GameStateCache stateObs) {
        List<Pattern> features = new ArrayList<>();
        GameStateCache cache = stateObs.getFutureCache(40);
        for(Pattern feat : f) {
            if(feat.appliesToGame(cache)) {
                features.add(feat);
            }
        }
        return features;
    }
}
