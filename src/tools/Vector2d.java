package tools;

/**
 * This class represents a vector, or a position, in the map.
 * PTSP-Competition
 * Created by Diego Perez, University of Essex.
 * Date: 19/12/11
 */
public class Vector2d
{
    /**
     * X-coordinate of the vector.
     */
    public double x;

    /**
     * Y-coordinate of the vector.
     */
    public double y;

    /**
     * Default constructor.
     */
    public Vector2d() {
        this(0, 0);
    }

    /**
     * Checks if a vector and this are the same.
     * @param o the other vector to check
     * @return true if their coordinates are the same.
     */
    @Override
	public boolean equals(Object o) {
        if (o instanceof Vector2d) {
            Vector2d v = (Vector2d) o;
            return x == v.x && y == v.y;
        } else {
            return false;
        }
    }

    /**
     * Builds a vector from its coordinates.
     * @param x x coordinate
     * @param y y coordinate
     */
    public Vector2d(double x, double y) {
        this.x = x;
        this.y = y;
    }


    /**
     * Builds a vector from another vector.
     * @param v Vector to copy from.
     */
    public Vector2d(Vector2d v) {
        this.x = v.x;
        this.y = v.y;
    }

    /**
     * Creates a copy of this vector
     * @return a copy of this vector
     */
    public Vector2d copy() {
        return new Vector2d(x,y);
    }

    /**
     * Sets this vector's coordinates to the coordinates of another vector.
     * @param v that other vector.
     */
    public void set(Vector2d v) {
        this.x = v.x;
        this.y = v.y;
    }

    /**
     * Sets this vector's coordinates to the coordinates given.
     * @param x x coordinate.
     * @param y y coordinate.
     */
    public void set(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Sets the vector's coordinates to (0,0)
     */
    public void zero() {
        x = 0.0;
        y = 0.0;
    }

    /**
     * Returns a representative String of this vector.
     * @return a representative String of this vector.
     */
    @Override
	public String toString() {
        return x + " : " + y;
    }

    /**
     * Adds another vector to this.
     * @param v vector to add.
     * @return this, after the addition.
     */
    public Vector2d add(Vector2d v) {
        this.x += v.x;
        this.y += v.y;
        return this;
    }

    /**
     * Adds to this vector two coordinates
     * @param x x coordinate
     * @param y y coordinate
     * @return returns this, after the addition.
     */
    public Vector2d add(double x, double y) {
        this.x += x;
        this.y += y;
        return this;
    }

    /**
     * Adds to this vector another vector, scaled it by a factor..
     * @param v Vector to add, to be scaled by w
     * @param w Scale of v.
     * @return this vector, after the addition.
     */
    public Vector2d add(Vector2d v, double w) {
        // weighted addition
        this.x += w * v.x;
        this.y += w * v.y;
        return this;
    }

    /**
     * Performs a wrap operation over this vector.
     * @param w width
     * @param h height
     * @return This vector, after the wrap.
     */
    public Vector2d wrap(double w, double h) {
//        w = 2 * w;
//        h = 2 * h;
        x = (x + w) % w;
        y = (y + h) % h;
        return this;
    }

    /**
     * Subtracts another vector from this.
     * @param v vector to subtract.
     * @return this, after the subtraction.
     */
    public Vector2d subtract(Vector2d v) {
        this.x -= v.x;
        this.y -= v.y;
        return this;
    }

    /**
     * Subtracts two coordinates to this vector.
     * @param x x coordinate
     * @param y y coordinate
     * @return returns this, after the subtraction.
     */
    public Vector2d subtract(double x, double y) {
        this.x -= x;
        this.y -= y;
        return this;
    }

    /**
     * Multiplies this vector by a factor.
     * @param fac factor to multiply this vector by.
     * @return This vector, after the operation.
     */
    public Vector2d mul(double fac) {
        x *= fac;
        y *= fac;
        return this;
    }

    /**
     * Rotates the vector an angle given, in radians.
     * @param theta angle given, in radians
     */
    public void rotate(double theta) {
        // rotate this vector by the angle made to the horizontal by this line
        // theta is in radians
        double cosTheta = Math.cos(theta);
        double sinTheta = Math.sin(theta);

        double nx = x * cosTheta - y * sinTheta;
        double ny = x * sinTheta + y * cosTheta;

        x = nx;
        y = ny;
    }

    /**
     * Calculates the scalar product of this vector and the one passed by parameter
     * @param v vector to do the scalar product with.
     * @return the value of the scalar product.
     */
    public double scalarProduct(Vector2d v) {
        return x * v.x + y * v.y;
    }

    /**
     * Gets the square value of the parameter passed.
     * @param x parameter
     * @return x * x
     */
    public static double sqr(double x) {
        return x * x;
    }

    /**
     * Returns the square distance from this vector to the one in the arguments.
     * @param v the other vector, to calculate the distance to.
     * @return the square distance, in pixels, between this vector and v.
     */
    public double sqDist(Vector2d v) {
        return sqr(x - v.x) + sqr(y - v.y);
    }

    /**
     * Gets the magnitude of the vector.
     * @return the magnitude of the vector (Math.sqrt(sqr(x) + sqr(y)))
     */
    public double mag() {
        return Math.sqrt(sqr(x) + sqr(y));
    }

    /**
     * Returns the distance from this vector to the one in the arguments.
     * @param v the other vector, to calculate the distance to.
     * @return the distance, in pixels, between this vector and v.
     */
    public double dist(Vector2d v) {
        return Math.sqrt(sqDist(v));
    }

    /**
     * Returns the distance from this vector to a pair of coordinates.
     * @param xx x coordinate
     * @param yy y coordinate
     * @return the distance, in pixels, between this vector and the pair of coordinates.
     */
    public double dist(double xx, double yy) {
        return Math.sqrt(sqr(x - xx) + sqr(y - yy));
    }


    /**
     * Returns the atan2 of this vector.
     * @return the atan2 of this vector.
     */
    public double theta() {
        return Math.atan2(y, x);
    }

    /**
     * Normalises this vector.
     */
    public void normalise() {
        double mag = mag();
        if(mag == 0)
        {
            x = y = 0;
        }else{
            x /= mag;
            y /= mag;
        }
    }

    /**
     * Calculates the dot product between this vector and the one passed by parameter.
     * @param v the other vector.
     * @return the dot product between these two vectors.
     */
    public double dot(Vector2d v) {
        double tot = this.x * v.x + this.y * v.y;
        return tot;
    }

    public Vector2d unitVector()
    {
        double l = this.mag();
        if(l > 0)
        {
            return new Vector2d(this.x/l,this.y/l);
        }
        else return new Vector2d(1,0);
    }

}

