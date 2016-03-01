package core.game;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import ontology.Types;
import tools.Vector2d;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 13/11/13
 * Time: 15:37
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class StateObservation {
    /**
     * This is the model of the game, used to apply an action and
     * get to the next state. This model MUST be private.
     */
    private ForwardModel model;

    /**
     * Constructor for StateObservation. Requires a forward model
     *
     * @param a_model forward model of the game.
     */
    public StateObservation(ForwardModel a_model) {
        model = a_model;
    }

    /**
     * Returns an exact copy of the state observation object.
     *
     * @return a copy of the state observation.
     */
    public StateObservation copy() {
        StateObservation copyObs = new StateObservation(model.copy());
        return copyObs;
    }

    /**
     * Advances the state using the action passed as the move of the agent.
     * It updates all entities in the game. It modifies the object 'this' to
     * represent the next state after the action has been executed and all
     * entities have moved.
     * <p/>
     * Note: stochastic events will not be necessarily the same as in the real game.
     *
     * @param action agent action to execute in the next cycle.
     */
    public void advance(Types.ACTIONS action) {
        model.advance(action);
    }

    /**
     * Sets a new seed for the forward model's random generator (creates a new object)
     *
     * @param seed the new seed.
     */
    public void setNewSeed(int seed)
    {
        model.setNewSeed(seed);
    }

    /**
     * Returns the actions that are available in this game for
     * the avatar.
     * @return the available actions.
     */
    public ArrayList<Types.ACTIONS> getAvailableActions()
    {
        return model.getAvatarActions(false);
    }

    /**
     * Returns the actions that are available in this game for
     * the avatar. If the parameter 'includeNIL' is true, the array contains the (always available)
     * NIL action. If it is false, this is equivalent to calling getAvailableActions().
     * @param includeNIL true to include Types.ACTIONS.ACTION_NIL in the array of actions.
     * @return the available actions.
     */
    public ArrayList<Types.ACTIONS> getAvailableActions(boolean includeNIL)
    {
        return model.getAvatarActions(includeNIL);
    }


    /**
     * Gets the score of the game at this observation.
     * @return score of the game.
     */
    public double getGameScore()
    {
        return model.getGameScore();
    }

    /**
     * Returns the game tick of this particular observation.
     * @return the game tick.
     */
    public int getGameTick()
    {
        return model.getGameTick();
    }

    /**
     * Indicates if there is a game winner in the current observation.
     * Possible values are Types.WINNER.PLAYER_WINS, Types.WINNER.PLAYER_LOSES and
     * Types.WINNER.NO_WINNER.
     * @return the winner of the game.
     */
    public Types.WINNER getGameWinner()
    {
        return model.getGameWinner();
    }

    /**
     * Indicates if the game is over or if it hasn't finished yet.
     * @return true if the game is over.
     */
    public boolean isGameOver()
    {
        return model.isGameOver();
    }

    /**
     * Returns the world dimensions, in pixels.
     * @return the world dimensions, in pixels.
     */
    public Dimension getWorldDimension()
    {
        return model.getWorldDimension();
    }

    /**
     * Indicates how many pixels form a block in the game.
     * @return how many pixels form a block in the game.
     */
    public int getBlockSize()
    {
        return model.getBlockSize();
    }

    //Methods to retrieve the state of the avatar, in the game...


    /**
     * Returns the position of the avatar. If the game is finished, we cannot guarantee that
     * this position reflects the real position of the avatar (the avatar itself could be
     * destroyed). If game finished, this returns Types.NIL.
     * @return position of the avatar, or Types.NIL if game is over.
     */
    public Vector2d getAvatarPosition()
    {
        return model.getAvatarPosition();
    }

    /**
     * Returns the speed of the avatar. If the game is finished, we cannot guarantee that
     * this speed reflects the real speed of the avatar (the avatar itself could be
     * destroyed). If game finished, this returns 0.
     * @return orientation of the avatar, or 0 if game is over.
     */
    public double getAvatarSpeed()
    {
        return model.getAvatarSpeed();
    }

    /**
     * Returns the orientation of the avatar. If the game is finished, we cannot guarantee that
     * this orientation reflects the real orientation of the avatar (the avatar itself could be
     * destroyed). If game finished, this returns Types.NIL.
     * @return orientation of the avatar, or Types.NIL if game is over.
     */
    public Vector2d getAvatarOrientation() {
        return model.getAvatarOrientation();
    }

    /**
     * Returns the resources in the avatar's possession. As there can be resources of different
     * nature, each entry is a key-value pair where the key is the resource ID, and the value is
     * the amount of that resource type owned. It should be assumed that there might be other resources
     * available in the game, but the avatar could have none of them.
     * If the avatar has no resources, an empty HashMap is returned.
     * @return resources owned by the avatar.
     */
    public HashMap<Integer, Integer> getAvatarResources() {
        return model.getAvatarResources();
    }

    /**
     * Returns the avatar's last move. At the first game cycle, it returns ACTION_NIL.
     * Note that this may NOT be the same as the last action given by the agent, as it may
     * have overspent in the last game cycle.
     * @return the action that was executed in the real game in the last cycle. ACTION_NIL
     * is returned in the very first game step.
     */
    public Types.ACTIONS getAvatarLastAction()
    {
        return model.getAvatarLastAction();
    }

    /**
     * Returns the avatar's type. In case it has multiple types, it returns the most specific one.
     * @return the itype of the avatar.
     */
    public int getAvatarType()
    {
        return model.getAvatarType();
    }

    /**
     * Returns the health points of the avatar. A value of 0 doesn't necessarily
     * mean that the avatar is dead (could be that no health points are in use in that game).
     * @return a numeric value, the amount of remaining health points.
     */
    public int getAvatarHealthPoints() { return model.getAvatarHealthPoints(); }

    /**
     * Returns the maximum amount of health points.
     * @return the maximum amount of health points the avatar ever had.
     */
    public int getAvatarMaxHealthPoints() { return model.getAvatarMaxHealthPoints(); }

    /**
     * Returns the limit of health points this avatar can have.
     * @return the limit of health points the avatar can have.
     */
    public int getAvatarLimitHealthPoints() {return model.getAvatarLimitHealthPoints();}

    //Methods to retrieve the state external to the avatar, in the game...

    /**
     * Returns a grid with all observations in the level, accessible by the x,y coordinates
     * of the grid. Each grid cell has a width and height of getBlockSize() pixels. Each cell
     * contains a list with all observations in that position. Note that the same observation
     * may occupy more than one grid cell.
     * @return the grid of observations
     */
    public ArrayList<Observation>[][] getObservationGrid()
    {
        return model.getObservationGrid();
    }

    /**
     * This method retrieves a list of events that happened so far in the game. In this
     * context, events are collisions of the avatar with other sprites in the game. Additionally,
     * the list also contains information about collisions of a sprite created by the avatar
     * (usually by using the action Types.ACTIONS.ACTION_USE) with other sprites. The list
     * is ordered asc. by game step.
     *
     * @return list of events triggered by the avatar or sprites it created.
     */
    public TreeSet<Event> getEventsHistory()
    {
         return model.getEventsHistory();
    }

    /**
     * Returns a list of observations of NPC in the game. As there can be
     * NPCs of different type, each entry in the array corresponds to a sprite type.
     * Every ArrayList contains a list of objects of type Observation.
     * Each Observation holds the position, unique id and
     * sprite id of that particular sprite.
     *
     * @return Observations of NPCs in the game.
     */
    public ArrayList<Observation>[] getNPCPositions()
    {
        return model.getNPCPositions(null);
    }


    /**
     * Returns a list of observations of NPC in the game. As there can be
     * NPCs of different type, each entry in the array corresponds to a sprite type.
     * Every ArrayList contains a list of objects of type Observation, ordered asc. by
     * distance to the reference passed. Each Observation holds the position, sprite type id and
     * sprite id of that particular sprite.
     *
     * @param reference   Reference position to use when sorting this array,
     *                    by ascending distance to this point.
     * @return Observations of NPCs in the game.
     */
    public ArrayList<Observation>[] getNPCPositions(Vector2d reference)
    {
        return model.getNPCPositions(reference);
    }

    /**
     * Returns a list of observations of immovable sprites in the game. As there can be
     * immovable sprites of different type, each entry in the array corresponds to a sprite type.
     * Every ArrayList contains a list of objects of type Observation.
     * Each Observation holds the position, unique id and
     * sprite id of that particular sprite.
     *
     * @return Observations of immovable sprites in the game.
     */
    public ArrayList<Observation>[] getImmovablePositions() {
        return model.getImmovablePositions(null);
    }

    /**
     * Returns a list of observations of immovable sprites in the game. As there can be
     * immovable sprites of different type, each entry in the array corresponds to a sprite type.
     * Every ArrayList contains a list of objects of type Observation, ordered asc. by
     * distance to the reference passed. Each Observation holds the position, sprite type id and
     * sprite id of that particular sprite.
     *
     * @param reference   Reference position to use when sorting this array,
     *                    by ascending distance to this point.
     * @return Observations of immovable sprites in the game.
     */
    public ArrayList<Observation>[] getImmovablePositions(Vector2d reference) {
        return model.getImmovablePositions(reference);
    }

    /**
     * Returns a list of observations of sprites that move, but are NOT NPCs in the game.
     * As there can be movable sprites of different type, each entry in the array
     * corresponds to a sprite type. Every ArrayList contains a list of objects of type
     * Observation. Each Observation holds the position,
     * unique id and sprite id of that particular sprite.
     *
     * @return Observations of movable, not NPCs, sprites in the game.
     */
    public ArrayList<Observation>[] getMovablePositions() {
        return model.getMovablePositions(null);
    }

    /**
     * Returns a list of observations of movable (not NPCs) sprites in the game. As there can be
     * movable (not NPCs) sprites of different type, each entry in the array corresponds to a sprite type.
     * Every ArrayList contains a list of objects of type Observation, ordered asc. by
     * distance to the reference passed. Each Observation holds the position, sprite type id and
     * sprite id of that particular sprite.
     *
     * @param reference   Reference position to use when sorting this array,
     *                    by ascending distance to this point.
     * @return Observations of movable (not NPCs) sprites in the game.
     */
    public ArrayList<Observation>[] getMovablePositions(Vector2d reference) {
        return model.getMovablePositions(reference);
    }

    /**
     * Returns a list of observations of resources in the game. As there can be
     * resources of different type, each entry in the array corresponds to a sprite type.
     * Every ArrayList contains a list of objects of type Observation.
     * Each Observation holds the position, unique id and
     * sprite id of that particular sprite.
     *
     * @return Observations of resources in the game.
     */
    public ArrayList<Observation>[] getResourcesPositions() {
        return model.getResourcesPositions(null);
    }

    /**
     * Returns a list of observations of resources in the game. As there can be
     * resources of different type, each entry in the array corresponds to a sprite type.
     * Every ArrayList contains a list of objects of type Observation, ordered asc. by
     * distance to the reference passed. Each Observation holds the position, sprite type id and
     * sprite id of that particular sprite.
     *
     * @param reference   Reference position to use when sorting this array,
     *                    by ascending distance to this point.
     * @return Observations of resources in the game.
     */
    public ArrayList<Observation>[] getResourcesPositions(Vector2d reference) {
        return model.getResourcesPositions(reference);
    }

    /**
     * Returns a list of observations of portals in the game. As there can be
     * portals of different type, each entry in the array corresponds to a sprite type.
     * Every ArrayList contains a list of objects of type Observation. Each Observation
     * holds the position, unique id and sprite id of that particular sprite.
     *
     * @return Observations of portals in the game.
     */
    public ArrayList<Observation>[] getPortalsPositions() {
        return model.getPortalsPositions(null);
    }

    /**
     * Returns a list of observations of portals in the game. As there can be
     * portals of different type, each entry in the array corresponds to a sprite type.
     * Every ArrayList contains a list of objects of type Observation, ordered asc. by
     * distance to the reference passed. Each Observation holds the position, sprite type id and
     * sprite id of that particular sprite.
     *
     * @param reference   Reference position to use when sorting this array,
     *                    by ascending distance to this point.
     * @return Observations of portals in the game.
     */
    public ArrayList<Observation>[] getPortalsPositions(Vector2d reference) {
        return model.getPortalsPositions(reference);
    }



    /**
     * Returns a list of observations of sprites created by the avatar (usually, by applying the
     * action Types.ACTIONS.ACTION_USE). As there can be sprites of different type, each entry in
     * the array corresponds to a sprite type. Every ArrayList contains a list of objects of
     * type Observation. Each Observation holds the position, unique id and sprite id
     * of that particular sprite.
     *
     * @return Observations of sprites the avatar created.
     */
    public ArrayList<Observation>[] getFromAvatarSpritesPositions() {
        return model.getFromAvatarSpPositions(null);
    }

    /**
     * Returns a list of observations of sprites created by the avatar (usually, by applying the
     * action Types.ACTIONS.ACTION_USE). As there can be sprites of different type, each entry in
     * the array corresponds to a sprite type. Every ArrayList contains a list of objects of
     * type Observation, ordered asc. by distance to the reference passed. Each Observation holds
     * the position, sprite type id and sprite id of that particular sprite.
     *
     * @param reference   Reference position to use when sorting this array,
     *                    by ascending distance to this point.
     * @return Observations of sprites the avatar created.
     */
    public ArrayList<Observation>[] getFromAvatarSpritesPositions(Vector2d reference) {
        return model.getFromAvatarSpPositions(reference);
    }

    /**
     * Compares if this and the received StateObservation state are equivalent.
     * DEBUG ONLY METHOD.
     * @param o Object to compare this to.
     * @return true if o has the same components as this.
     */
    public boolean equiv(Object o)
    {
        //First simple object-level checks.
        if(this == o) return true;
        if(!(o instanceof StateObservation)) return false;
        StateObservation other = (StateObservation)o;

        //Game state checks.
        if(this.getGameScore() != other.getGameScore()) return false;
        if(this.getGameTick() != other.getGameTick()) return false;
        if(this.getGameWinner() != other.getGameWinner()) return false;
        if(this.isGameOver() != other.isGameOver()) return false;
        if(this.getAvatarSpeed() != other.getAvatarSpeed()) return false;
        if(!this.getAvatarPosition().equals(other.getAvatarPosition())) return false;
        if (!this.getAvatarOrientation().equals(other.getAvatarOrientation())) return false;

        //Check resources
        HashMap<Integer, Integer> thisResources = this.getAvatarResources();
        HashMap<Integer, Integer> otherResources = other.getAvatarResources();
        if(thisResources.size() != otherResources.size()) return false;
        try
        {
            Set<Integer> resKeys = otherResources.keySet();
            for (Integer k : resKeys) {
                if (!(otherResources.get(k).equals(thisResources.get(k))))
                    return false;
            }
        }catch(Exception e)
        {
            System.out.println(e.toString());
            return false;
        }

        //Check events.
        TreeSet<Event> thisEvents = this.getEventsHistory();
        TreeSet<Event> otherEvents = other.getEventsHistory();
        if(thisEvents.size() != otherEvents.size()) return false;
        try
        {
            Iterator<Event> otherIt = otherEvents.descendingIterator();
            Iterator<Event> thisIt = thisEvents.descendingIterator();

            while(otherIt.hasNext())
            {
                if(!otherIt.next().equals(thisIt.next()))
                    return false;
            }

        }catch(Exception e)
        {
            System.out.println(e.toString());
            return false;
        }

        //Check observations:
        return this.model.equalObservations(other.model);
    }

}
