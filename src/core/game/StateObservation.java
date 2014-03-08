package core.game;

import core.VGDLSprite;
import ontology.Types;
import ontology.avatar.MovingAvatar;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 13/11/13
 * Time: 15:37
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class StateObservation
{
    /**
     * This is the model of the game, used to apply an action and
     * get to the next state. This model MUST be private.
     */
    private ForwardModel model;

    /**
     * Constructor for StateObservation. Requires a forward model
     * @param a_model forward model of the game.
     */
    public StateObservation(ForwardModel a_model)
    {
        model = a_model;
    }

    /**
     * Returns an exact copy of the state observation object.
     * @return a copy of the state observation.
     */
    public StateObservation copy()
    {
        StateObservation copyObs = new StateObservation(model.copy());
        return copyObs;
    }

    /**
     * Advances the state using the action passed as the move of the agent.
     * It updates all entities in the game. It modifies the object 'this' to
     * represent the next state after the action has been executed and all
     * entities have moved.
     *
     * Note: stochastic events will not be necessarily the same as in the real game.
     *
     * @param action agent action to execute in the next cycle.
     */
    public void advance(Types.ACTIONS action)
    {
        model.advance(action);
    }

    /**
     * Returns the actions that are available in this game for
     * the avatar.
     * @return the available actions.
     */
    public ArrayList<Types.ACTIONS> getAvailableActions()
    {
        return model.getAvatarActions();
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
     * Returns the resources owned by the avatar. As there can be resources of different
     * nature, each entry of the array indicates the amount of resource of each type.
     * If the game is finished, we cannot guarantee that this information is meaningful
     * at all (the avatar itself could be destroyed). If game finished, this returns null.
     * @return resources owned by the avatar, or null if game is over.
     */
    public int[] getAvatarResources() {
        return model.getAvatarResources();
    }


    //Methods to retrieve the state external to the avatar, in the game...

    /**
     * Returns the positions of the other NPCs in the game. If no NPCs in the game,
     * it returns null. As there can be NPCs of different type, the positions are returned
     * as an array of lists, each array for each type.
     * @return a list with the positions of the other NPCs in the game, null if there are no NPCs.
     */
    public ArrayList<Vector2d>[] getNPCPositions()
    {
        return model.getNPCPositions();
    }

    /**
     * Returns a list with the positions of static objects. As there can be entities of different type,
     * the positions are returned as an array of lists, each array for each type.
     * @return a list with the positions of static objects.
     */
    public ArrayList<Vector2d>[] getImmovablePositions() {
        return model.getImmovablePositions();
    }

    /**
     * Returns a list with the positions of elements that move, but are NOT NPCs.
     * As there can be entities of different type, the positions are returned as an array of lists,
     * each array for each type.
     * @return a list with the positions of elements that move, but are NOT NPCs.
     */
    public ArrayList<Vector2d>[] getMovablePositions() {
        return model.getMovablePositions();
    }

    /*
    * Returns a list with the positions of resources.  As there can be resources of different type,
    * the positions are returned as an array of lists, each array for each type.
    * @return a list with the positions of resources.
    */
    public ArrayList<Vector2d>[] getResourcesPositions() {
        return model.getResourcesPositions();
    }

    /*
    * Returns a list with the positions of portals. As there can be portals of different type,
    * the positions are returned as an array of lists, each array for each type.
    * @return a list with the positions of portals.
    */
    public ArrayList<Vector2d>[] getPortalsPositions() {
        return model.getPortalsPositions();
    }

}
