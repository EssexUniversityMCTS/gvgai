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
    public double[] position;

    /**
     * Reference to the position used for comparing this
     * observation with others.
     */
    public double[] reference;

    /**
     * Distance from this observation to the reference.
     */
    public double sqDist;
}