package serialization;

/**
 * Created by Daniel on 16/03/17.
 */

public class Observation {

    /**
     * Category of this observation (static, resource, npc, etc.).
     */
    public int category;

    /**
     * Type of sprite of this observation.
     */
    public int itype;

    /**
     * unique ID for this observation
     */
    public int obsID;

    /**
     * Position of the observation.
     */
    public Vector2d position;

    /**
     * Reference to the position used for comparing this
     * observation with others.
     */
    public Vector2d reference;

    /**
     * Distance from this observation to the reference.
     */
    public double sqDist;

    /**
     * Getters for each of the fields above.
     */
    public int getCategory() {
        return category;
    }

    public double getSqDist() {
        return sqDist;
    }

    public int getItype() {
        return itype;
    }

    public int getObsID() {
        return obsID;
    }

    public Vector2d getPosition() {
        return position;
    }

    public Vector2d getReference() {
        return reference;
    }

    @Override
    public String toString() {
        return "Observation{" +
                "category=" + category +
                ", itype=" + itype +
                ", obsID=" + obsID +
                ", position=" + position +
                ", reference=" + reference +
                ", sqDist=" + sqDist +
                "}\n";
    }
}