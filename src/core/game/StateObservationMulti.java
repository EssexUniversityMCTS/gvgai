package core.game;

import ontology.Types;
import tools.Vector2d;

import java.util.*;

/**
 * Created by Raluca on 07-Apr-16.
 */
public class StateObservationMulti extends StateObservation {

    /**
     * Constructor for StateObservation in multi player games. Requires a forward model
     *
     * @param a_model forward model of the game.
     */
    public StateObservationMulti(ForwardModel a_model, int playerID) {
        super(a_model, playerID);
    }

    /**
     * Method overloaded for multi player games. Now passes an array of actions for all
     * players in the game, the index in the array corresponding to playerID.
     * Advances the state using the action passed as the move of the agents.
     * It updates all entities in the game. It modifies the object 'this' to
     * represent the next state after the actions have been executed and all
     * entities have moved.
     * <p/>
     * Note: stochastic events will not be necessarily the same as in the real game.
     *
     * @param actions array of agent actions to execute in the next cycle for all players.
     */
    public void advance(Types.ACTIONS[] actions) {
        model.advance(actions);
    }

    /**
     * Method overloaded for multi player games. Now passes the playerID.
     * Returns the actions that are available in this game for
     * the avatar.
     * @param playerID ID of the player to query
     * @return the available actions.
     */
    public ArrayList<Types.ACTIONS> getAvailableActions(int playerID) { return model.getAvatarActions(playerID, true); }

    /**
     * Method overloaded for multi player games. Now passes the playerID.
     * Gets the score of the game at this observation.
     * @param playerID ID of the player to query.
     * @return score of the player with the corresponding playerID.
     */
    public double getGameScore(int playerID)
    {
        return model.getGameScore(playerID);
    }

    /**
     * Similar to getGameWinner() in single player games, but for multi player. Now returns an
     * array of type Types.WINNER and length as the number of players in the game. The index
     * corresponds to a playerID, so the state of which player can be checked by accessing the
     * element in this array with the right index.
     * Indicates if there is a game winner in the current observation.
     * Possible values in the array are Types.WINNER.PLAYER_WINS, Types.WINNER.PLAYER_LOSES and
     * Types.WINNER.NO_WINNER.
     * @return array of type Types.WINNER, length of number of players in the game.
     */
    public Types.WINNER[] getMultiGameWinner()
    {
        return model.getMultiGameWinner();
    }

    /**
     * Method overridden for multi player games.
     * Indicates if the game is over or if it hasn't finished yet.
     * @return true if the game is over.
     */
    public boolean isGameOver() { return model.isMultiGameOver(); }

    /**
     * Method overloaded for multi player games. Now passes the player ID.
     * Returns the position of the avatar. If the game is finished, we cannot guarantee that
     * this position reflects the real position of the avatar (the avatar itself could be
     * destroyed). If game finished, this returns Types.NIL.
     * @param playerID ID of the player to query.
     * @return position of the avatar, or Types.NIL if game is over.
     */
    public Vector2d getAvatarPosition(int playerID)
    {
        return model.getAvatarPosition(playerID);
    }

    /**
     * Method overloaded for multi player games. Now passes the player ID.
     * Returns the speed of the avatar. If the game is finished, we cannot guarantee that
     * this speed reflects the real speed of the avatar (the avatar itself could be
     * destroyed). If game finished, this returns 0.
     * @param playerID ID of the player to query.
     * @return orientation of the avatar, or 0 if game is over.
     */
    public double getAvatarSpeed(int playerID)
    {
        return model.getAvatarSpeed(playerID);
    }

    /**
     * Method overloaded for multi player games. Now passes the player ID.
     * Returns the orientation of the avatar. If the game is finished, we cannot guarantee that
     * this orientation reflects the real orientation of the avatar (the avatar itself could be
     * destroyed). If game finished, this returns Types.NIL.
     * @param playerID ID of the player to query.
     * @return orientation of the avatar, or Types.NIL if game is over.
     */
    public Vector2d getAvatarOrientation(int playerID) {
        return model.getAvatarOrientation(playerID);
    }

    /**
     * Method overloaded for multi player games. Now passes the player ID.
     * Returns the resources in the avatar's possession. As there can be resources of different
     * nature, each entry is a key-value pair where the key is the resource ID, and the value is
     * the amount of that resource type owned. It should be assumed that there might be other resources
     * available in the game, but the avatar could have none of them.
     * If the avatar has no resources, an empty HashMap is returned.
     * @param playerID ID of the player to query.
     * @return resources owned by the avatar.
     */
    public HashMap<Integer, Integer> getAvatarResources(int playerID) {
        return model.getAvatarResources(playerID);
    }

    /**
     * Method overloaded for multi player games. Now passes the player ID.
     * Returns the avatar's last move. At the first game cycle, it returns ACTION_NIL.
     * Note that this may NOT be the same as the last action given by the agent, as it may
     * have overspent in the last game cycle.
     * @param playerID ID of the player to query.
     * @return the action that was executed in the real game in the last cycle. ACTION_NIL
     * is returned in the very first game step.
     */
    public Types.ACTIONS getAvatarLastAction(int playerID)
    {
        return model.getAvatarLastAction(playerID);
    }

    /**
     * Method overloaded for multi player games. Now passes the player ID.
     * Returns the avatar's type. In case it has multiple types, it returns the most specific one.
     * @param playerID ID of the player to query.
     * @return the itype of the avatar.
     */
    public int getAvatarType(int playerID) { return model.getAvatarType(playerID); }

    /**
     * Method overloaded for multi player games. Now passes the player ID.
     * Returns the health points of the avatar. A value of 0 doesn't necessarily
     * mean that the avatar is dead (could be that no health points are in use in that game).
     * @param playerID ID of the player to query.
     * @return a numeric value, the amount of remaining health points.
     */
    public int getAvatarHealthPoints(int playerID) { return model.getAvatarHealthPoints(playerID); }

    /**
     * Method overloaded for multi player games. Now passes the player ID.
     * Returns the maximum amount of health points.
     * @param playerID ID of the player to query.
     * @return the maximum amount of health points the avatar ever had.
     */
    public int getAvatarMaxHealthPoints(int playerID) { return model.getAvatarMaxHealthPoints(playerID); }

    /**
     * Method overloaded for multi player games. Now passes the player ID.
     * Returns the limit of health points this avatar can have.
     * @param playerID ID of the player to query.
     * @return the limit of health points the avatar can have.
     */
    public int getAvatarLimitHealthPoints(int playerID) {return model.getAvatarLimitHealthPoints(playerID);}

    /**
     * returns true if the avatar is alive.
     * @return true if the avatar is alive.
     */
    public boolean isAvatarAlive(int playerID) {return model.isAvatarAlive(playerID);}


    public StateObservationMulti copy() {
        StateObservationMulti copyObs = new StateObservationMulti(model.copy(), this.playerID);
        return copyObs;
    }

    /**
     * Method overwritten with multi player optimisations.
     * @param o Object to compare this to.
     * @return true if o has the same components as this.
     */
    @Override
    public boolean equiv(Object o) {
        System.out.println("StateObservation.equiv() is a Deprecated Method. And it always returns False, now.");
        return false;
    }
}
