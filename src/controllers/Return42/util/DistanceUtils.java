package controllers.Return42.util;

import tools.Vector2d;

/**
 * Created by Oliver on 04.05.2015.
 */
public class DistanceUtils {

    public static double manhattanDistance( Vector2d lhs, Vector2d rhs, int blockSize ) {
        return Math.abs(lhs.x - rhs.x) / blockSize
                + Math.abs(lhs.y - rhs.y) / blockSize;
    }

    public static double manhattanDistance( Vector2d lhs, Vector2d rhs ) {
        return Math.abs(lhs.x - rhs.x) + Math.abs(lhs.y - rhs.y);
    }
}
