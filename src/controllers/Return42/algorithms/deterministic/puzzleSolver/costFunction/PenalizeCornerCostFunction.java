package controllers.Return42.algorithms.deterministic.puzzleSolver.costFunction;

import core.game.Event;
import core.game.Observation;
import core.game.StateObservation;

import java.util.List;

import controllers.Return42.util.StateObservationUtils;

/**
 * Created by Oliver on 06.05.2015.
 */
public class PenalizeCornerCostFunction implements AStarCostFunction {

    private final boolean[][] walls;
    private final int movableType;

    public PenalizeCornerCostFunction( StateObservation state, int movableType ) {
        this.walls = extractWalls( state );
        this.movableType = movableType;
    }

    private boolean[][] extractWalls(StateObservation state) {
        int width = state.getWorldDimension().width / state.getBlockSize();
        int height = state.getWorldDimension().height / state.getBlockSize();
        boolean[][] walls = new boolean[width][height];

        for( Observation immovable: StateObservationUtils.flatten( state.getImmovablePositions() )) {
            if (immovable.itype == 0) {
                int x = (int)immovable.position.x / state.getBlockSize();
                int y = (int)immovable.position.y / state.getBlockSize();
                walls[x][y] = true;
            }
        }

        return walls;
    }

    @Override
    public double evaluate(StateObservation lastState, StateObservation newState) {
        if (!didNewEventOccur(lastState, newState) )
            return 0;

        Observation collidingMovableObject = findMovableObjectWithWhichPlayerCollided(newState);
        if (collidingMovableObject == null)
            return 0;

        if (!isObjectInCorner( newState, collidingMovableObject ) )
            return 0;

        // we moved an object into a corner
        return 5;
    }

    private boolean didNewEventOccur(StateObservation lastState, StateObservation newState) {
        return lastState.getEventsHistory().size() != newState.getEventsHistory().size();
    }

    private Observation findMovableObjectWithWhichPlayerCollided(StateObservation newState) {
        // TODO: we should look for all events in the last action.
        Event lastEvent = newState.getEventsHistory().last();

        // we're not interested in other collisions.
        if (lastEvent.passiveTypeId != movableType)
            return null;

        List<Observation> allMovables = StateObservationUtils.flatten( newState.getMovablePositions() );
        for( Observation aMovable: allMovables ) {
            if (lastEvent.passiveSpriteId == aMovable.obsID) {
                return aMovable;
            }
        }

        return null;
    }

    private boolean isObjectInCorner( StateObservation state, Observation collidingMovableObject ) {
        int x = (int) collidingMovableObject.position.x / state.getBlockSize();
        int y = (int) collidingMovableObject.position.y / state.getBlockSize();

        int width = walls.length;
        int height = walls[0].length;

        boolean isLeftWall =  x-1 < 0       || walls[x-1][y];
        boolean isRightWall = x+1 >= width  || walls[x+1][y];
        boolean isUpperWall = y+1 >= height || walls[x][y+1];
        boolean isLowerWall = y-1 < 0       || walls[x][y-1];

        if (isLeftWall && isUpperWall)
            return true;
        if (isUpperWall && isRightWall)
            return true;
        if (isRightWall && isLowerWall)
            return true;
        if (isLowerWall && isLeftWall)
            return true;

        return false;
    }

}
