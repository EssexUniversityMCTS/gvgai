package core.game;

import tools.Vector2d;

/**
 * Created by Diego on 19/03/14.
 */
public class Observation implements Comparable<Observation>
{

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

    public Observation() {
        // used for learning track
        category = -1;
        itype = -1;
        obsID = -1;
        position = new Vector2d();
        reference = null;
        sqDist = -1;
    }

    /**
     * New observation. It is the observation of a sprite, recording its ID and position.
     * @param itype type of the sprite of this observation
     * @param id ID of the observation.
     * @param pos position of the sprite.
     * @param posReference reference to compare this position to others.
     * @param category category of this observation (NPC, static, resource, etc.)
     */
    public Observation(int itype, int id, Vector2d pos, Vector2d posReference, int category)
    {
        this.itype = itype;
        this.obsID = id;
        this.position = pos;
        this.reference = posReference;
        sqDist = pos.sqDist(posReference);
        this.category = category;
    }

    /**
     * Updates this observation
     * @param itype type of the sprite of this observation
     * @param id ID of the observation.
     * @param pos position of the sprite.
     * @param posReference reference to compare this position to others.
     * @param category category of this observation (NPC, static, resource, etc.)
     */
    public void update(int itype, int id, Vector2d pos, Vector2d posReference, int category)
    {
        this.itype = itype;
        this.obsID = id;
        this.position = pos;
        this.reference = posReference;
        sqDist = pos.sqDist(posReference);
        this.category = category;
    }

    /**
     * Compares this observation to others, using distances to the reference position.
     * @param o other observation.
     * @return -1 if this precedes o, 1 if same distance or o is closer to reference.
     */
    @Override
    public int compareTo(Observation o) {
        double oSqDist = o.position.sqDist(reference);
        if(sqDist < oSqDist)        return -1;
        else if(sqDist > oSqDist)   return 1;
        return 0;
    }

    /**
     * Compares two Observations to check if they are equal. The reference attribute is NOT
     * compared in this object.
     * @param other the other observation.
     * @return true if both objects are the same Observation.
     */
    public boolean equals(Object other)
    {
        if(other == null || !(other instanceof Observation))
            return false;

        Observation o = (Observation) other;
        if(this.itype != o.itype) return false;
        if(this.obsID != o.obsID) return false;
        if(!this.position.equals(o.position)) return false;
        if(this.category != o.category) return false;
        return true;
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