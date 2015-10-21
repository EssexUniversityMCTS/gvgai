package controllers.Return42.heuristics.features;

import core.game.Observation;

import java.util.List;

import controllers.Return42.GameStateCache;

public class SeekImmovables extends Feature {

    private final double movDistanceFactor;
    private final double movCounterFactor;

    public SeekImmovables(double movDistanceFactor, double movCounterFactor) {
        this.movDistanceFactor = movDistanceFactor;
        this.movCounterFactor = movCounterFactor;
    }

    @Override
    public boolean isUseful(GameStateCache state) {
        List<Observation>[] movPositions = state.getImmovablePositions();
        return movPositions != null && movPositions.length > 1; // walls are not interesting
        // TODO: any level with collectibles but no walls?
    }

    @Override
    public double evaluate(GameStateCache state) {
        List<Observation>[] movPositions = state.getImmovablePositions();
        if(movPositions != null && movPositions.length > 1) {
            double movMinDistance = 0;
            int movCounter = 0;

            for(int i = 1; i < movPositions.length; i++) {
                List<Observation> movs = movPositions[i];
                int size = movs.size();
                if(size > 0) {
                    movMinDistance += (Math.sqrt(movs.get(0).sqDist) / state.getBlockSize());  //weight abundant tiles e.g. water lower
                    movCounter += size;
                }
            }

            if(movCounter > 0) {
                return (-(movMinDistance / movCounter) * movDistanceFactor - movCounter * movCounterFactor) * weight;
            }
        }
        return 0;
    }

    @Override
    public double getWeight() {
        return (movDistanceFactor + movCounterFactor) * weight;
    }
}
