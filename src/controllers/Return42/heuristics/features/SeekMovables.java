package controllers.Return42.heuristics.features;

import core.game.Observation;

import java.util.List;

import controllers.Return42.GameStateCache;

public class SeekMovables extends Feature {

    private final double movDistanceFactor;
    private final double movCounterFactor;

    public SeekMovables(double movDistanceFactor, double movCounterFactor) {
        this.movDistanceFactor = movDistanceFactor;
        this.movCounterFactor = movCounterFactor;
    }

    @Override
    public boolean isUseful(GameStateCache state) {
        return state.getMovablePositions() != null;
    }

    @Override
    public double evaluate(GameStateCache state) {
        List<Observation>[] movPositions = state.getMovablePositions();
        if(movPositions != null) {
            double movMinDistance = 0;
            int movCounter = 0;

            for(List<Observation> npcs : movPositions) {
                if(npcs.size() > 0) {
                    movMinDistance += Math.sqrt(npcs.get(0).sqDist) / state.getBlockSize();
                    movCounter += npcs.size();
                }
            }

            if(movCounter > 0) {
                //go to movables and try to eliminate them
                return -movMinDistance * movDistanceFactor - movCounter * movCounterFactor;
            }
        }
        return 0;
    }

    @Override
    public double getWeight() {
        return (movDistanceFactor + movCounterFactor) * weight;
    }
}
