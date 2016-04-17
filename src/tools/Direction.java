package tools;

/**
 * Created by dperez on 17/04/16.
 */
public class Direction extends Vector2d
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

    public Vector2d add(Vector2d v) { error_immutable(); return null; }
    public Vector2d add(double x, double y) { error_immutable(); return null; }
    public Vector2d add(Vector2d v, double w) { error_immutable(); return null; }
    public Vector2d subtract(Vector2d v) { error_immutable(); return null; }
    public Vector2d subtract(double x, double y) { error_immutable(); return null; }
    public Vector2d mul(double fac) { error_immutable(); return null; }
    public void normalise() { error_immutable(); }

    public void error_immutable() { throw new RuntimeException("Direction Vector2d is immutable."); }
}
