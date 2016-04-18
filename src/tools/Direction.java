package tools;

/**
 * Created by dperez on 17/04/16.
 */
public class Direction
{
    private double xDir;
    private double yDir;

    public Direction(double x, double y) {
        xDir = x;
        yDir = y;
    }

    public double x() { return xDir; }
    public double y() { return yDir; }
    public Direction copy() {
        return new Direction(xDir,yDir);
    }
    public Vector2d getVector() {return new Vector2d(xDir, yDir);}

    /**
     * Returns a representative String of this vector.
     * @return a representative String of this vector.
     */
    @Override
    public String toString() {
        return "D " + xDir + " : " + yDir;
    }

    /**
     * Checks if a direction and this are the same.
     * @param d the other direction to check
     * @return true if their coordinates are the same.
     */
    @Override
    public boolean equals(Object d) {
        if (d instanceof Direction) {
            Direction dir = (Direction) d;
            return xDir == dir.x() && yDir == dir.y();
        } else {
            return false;
        }
    }

}
