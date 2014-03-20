package core.game;

import core.SpriteGroup;
import core.VGDLSprite;
import ontology.Types;
import ontology.avatar.MovingAvatar;
import tools.Vector2d;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 13/11/13
 * Time: 15:37
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class ForwardModel extends Game
{
    /**
     * Private sampleRandom generator. Rolling the state forward from this state
     * observation will use this sampleRandom generator, different from the one
     * that is used in the real game.
     */
    private Random randomObs;

    /**
     * Boolean map of sprite types that are NPCs.
     * npcList[spriteType]==true : spriteType is NPC.
     */
    private boolean npcList[];

    /**
     * Boolean map of sprite types that are immovable sprites.
     * immList[spriteType]==true : spriteType is immovable sprite.
     */
    private boolean immList[];

    /**
     * Boolean map of sprite types that can move.
     * movList[spriteType]==true : spriteType can move.
     */
    private boolean movList[];

    /**
     * Boolean map of sprite types that are resources.
     * resList[spriteType]==true : spriteType is resource.
     */
    private boolean resList[];

    /**
     * Boolean map of sprite types that are portals or doors.
     * portalList[spriteType]==true : spriteType is portal or door.
     */
    private boolean portalList[];


    /**
     * Constructor for StateObservation. Initializes everything
     * @param a_gameState
     */
    public ForwardModel(Game a_gameState)
    {
        //All static elements of the game are assigned from the game we create the copy from.
        initNonVolatile(a_gameState);

        //Init those variables that take a determined value at the beginning of a game.
        init();
    }


    /**
     * Dumps the game state into 'this' object. Effectively, creates a state observation
     * from a game state (of class Game).
     * @param a_gameState game to take the state from.
     */
    final public void update(Game a_gameState)
    {
        int numSpriteTypes = a_gameState.spriteGroups.length;
        kill_list = new ArrayList<VGDLSprite>();
        bucketList = new Bucket[numSpriteTypes];

        //Copy of sprites from the game.
        spriteGroups = new SpriteGroup[numSpriteTypes];
        num_sprites = 0;

        for(int i = 0; i < spriteGroups.length; ++i)
        {
            bucketList[i] = new Bucket();
            spriteGroups[i] = new SpriteGroup(i);

            Iterator<VGDLSprite> spriteIt = a_gameState.spriteGroups[i].getSpriteIterator();
            if(spriteIt != null) while(spriteIt.hasNext())
            {
                VGDLSprite sp = spriteIt.next();
                VGDLSprite spCopy = sp.copy();
                spriteGroups[i].addSprite(spCopy.spriteID, spCopy);
                checkSpriteFeatures(spCopy, i);
            }

            int nSprites = spriteGroups[i].numSprites();
            num_sprites += nSprites;
        }

        //Game state variables:
        this.gameTick = a_gameState.gameTick;
        this.isEnded = a_gameState.isEnded;
        this.winner = a_gameState.winner;
        this.score = a_gameState.score;
        this.nextSpriteID = a_gameState.nextSpriteID;
    }

    /**
     * Checks some features of the sprite, to categorize it.
     * @param sp Sprite to categorize.
     * @param itype itype of the sprite.
     */
    private void checkSpriteFeatures(VGDLSprite sp, int itype)
    {
        //Check for the avatar.
        if(sp.is_avatar)
            this.avatar = (MovingAvatar) sp;

        //Is it a resource?
        else if(sp.is_resource)
            resList[itype] = true;

        //Is it a portal?
        else if(sp.portal)
            portalList[itype] = true;

        //Is it npc?
        else if(sp.is_npc)
            npcList[itype] = true;

        //Is it immovable?
        else if(sp.is_static)
            immList[itype] = true;

        //Then it is movable.
        else
            movList[itype] = true;

    }

    /**
     * Initializes the variables of this game that have always a determined value at the beginning
     * of any game.
     */
    private void init()
    {
        this.randomObs = new Random();
        this.gameTick = 0;
        this.isEnded = false;
        this.winner = Types.WINNER.NO_WINNER;
    }

    /**
     * Initializes the non volatile elements of a game (constructors, termination conditions,
     * effects, etc). 'this' takes these from a_gameState,
     * @param a_gameState Reference to the original game
     */
    private void initNonVolatile(Game a_gameState)
    {
        //We skip this.resource_colors, ki and sampleRandom.
        this.spriteOrder = a_gameState.spriteOrder;
        this.singletons = a_gameState.singletons;
        this.classConst = a_gameState.classConst;
        this.collisionEffects = a_gameState.collisionEffects;
        this.definedEffects = a_gameState.definedEffects;
        this.eosEffects = a_gameState.eosEffects;
        this.definedEOSEffects = a_gameState.definedEOSEffects;
        this.iSubTypes = a_gameState.iSubTypes;
        this.charMapping = a_gameState.charMapping;
        this.terminations = a_gameState.terminations;
        this.resources_limits = a_gameState.resources_limits;
        this.screenSize = a_gameState.screenSize;
        this.size = a_gameState.size;
        this.block_size = a_gameState.block_size;
        this.score = a_gameState.score;
        this.frame_rate = a_gameState.frame_rate; //is this needed?
        this.MAX_SPRITES = a_gameState.MAX_SPRITES;

        //create the boolean maps of sprite types.
        npcList = new boolean[a_gameState.spriteGroups.length];
        immList = new boolean[a_gameState.spriteGroups.length];
        movList = new boolean[a_gameState.spriteGroups.length];
        resList = new boolean[a_gameState.spriteGroups.length];
        portalList = new boolean[a_gameState.spriteGroups.length];
    }


    /**
     * Returns the sampleRandom generator of this forward model. It is not the same as the
     * sampleRandom number generator of the main game copy.
     * @return the sampleRandom generator of this forward model.
     */
    final public Random getRandomGenerator()
    {
        return randomObs;
    }

    /************** Useful functions for the agent *******************/

    /**
     * Advances the forward model using the acction supplied.
     * @param action
     */
    final public void advance(Types.ACTIONS action)
    {
        if(!isEnded)
        {
            tick(action);
            gameTick++;
            eventHandling();
            clearAll();
            terminationHandling();
        }
    }

    /**
     * Creates a copy of this forward model.
     * @return the copy of this forward model.
     */
    final public ForwardModel copy()
    {
        ForwardModel copyObs = new ForwardModel(this);
        copyObs.update(this);
        return copyObs;
    }

    /**
     * Gets the game score of this state.
     * @return the game score.
     */
    public double getGameScore() { return this.score; }

    /**
     * Gets the current game tick of this particular state.
     * @return the game tick of the current game state.
     */
    public int getGameTick() { return this.gameTick; }

    /**
     * Indicates if there is a game winner in the current observation.
     * Possible values are Types.WINNER.PLAYER_WINS, Types.WINNER.PLAYER_LOSES and
     * Types.WINNER.NO_WINNER.
     * @return the winner of the game.
     */
    public Types.WINNER getGameWinner() { return this.winner; }

    /**
     * Indicates if the game is over or if it hasn't finished yet.
     * @return true if the game is over.
     */
    public boolean isGameOver() { return getGameWinner() != Types.WINNER.NO_WINNER; }


    /** avatar-dependent functions **/

    /**
     * Returns the position of the avatar. If the game is finished, we cannot guarantee that
     * this position reflects the real position of the avatar (the avatar itself could be
     * destroyed). If game finished, this returns Types.NIL.
     * @return position of the avatar, or Types.NIL if game is over.
     */
    public Vector2d getAvatarPosition()
    {
        if(isEnded)
            return Types.NIL;
        return avatar.getPosition();
    }

    /**
     * Returns the speed of the avatar. If the game is finished, we cannot guarantee that
     * this speed reflects the real speed of the avatar (the avatar itself could be
     * destroyed). If game finished, this returns 0.
     * @return orientation of the avatar, or 0 if game is over.
     */
    public double getAvatarSpeed()
    {
        if(isEnded)
            return 0;
        return avatar.speed;
    }

    /**
     * Returns the orientation of the avatar. If the game is finished, we cannot guarantee that
     * this orientation reflects the real orientation of the avatar (the avatar itself could be
     * destroyed). If game finished, this returns Types.NIL.
     * @return orientation of the avatar, or Types.NIL if game is over.
     */
    public Vector2d getAvatarOrientation()
    {
        if(isEnded)
            return Types.NIL;
        return avatar.orientation;
    }

    /**
     * Returns the actions that are available in this game for
     * the avatar.
     * @return the available actions. An empty list if the game is ended.
     */
    public ArrayList<Types.ACTIONS> getAvatarActions()
    {
        if(isEnded)
            return new ArrayList<Types.ACTIONS>();
        return avatar.actions;
    }


    /**
     * Returns the resources in the avatar's possession. As there can be resources of different
     * nature, each entry is a key-value pair where the key is the resource ID, and the value is
     * the amount of that resource type owned. It should be assumed that there might be other resources
     * available in the game, but the avatar could have none of them.
     * If the avatar has no resources, an empty HashMap is returned.
     * @return resources owned by the avatar.
     */
    public HashMap<Integer, Integer> getAvatarResources()
    {

        //Determine how many different resources does the avatar have.
        HashMap<Integer, Integer> owned = new HashMap<Integer, Integer>();

        if(avatar == null)
            return owned;

        //And for each type, add their amount.
        Set<Map.Entry<Integer, Integer>> entries = avatar.resources.entrySet();
        for(Map.Entry<Integer, Integer> entry : entries)
        {
            owned.put(entry.getKey(), entry.getValue());
        }

        return owned;
    }


    /** Methods that return positions of things **/

    /**
     * Gets position from the sprites corresponding to the boolean map passed by parameter.
     * @param groupArray boolean map that indicates which sprite types must be considered.
     * @return List of arrays with Observations. Each entry in the array corresponds to a different
     * sprite type.
     */
    private ArrayList<Observation>[] getPositionsFrom(boolean[] groupArray, Vector2d refPosition)
    {
        //First, get how many types we have.
        int numDiffTypes = 0;
        for(int i = 0; i < groupArray.length; ++i)    if(groupArray[i]) numDiffTypes++;

        if(numDiffTypes == 0)
            return null; //Wait, no types? no sprites of this group then.

        ArrayList<Observation>[] observations = new ArrayList[numDiffTypes];
        Vector2d reference = refPosition;
        if(refPosition == null)
            reference = Types.NIL;

        int idx = 0;
        for(int i = 0; i < groupArray.length; ++i)
        {
            if(groupArray[i])
            {
                observations[idx] = new ArrayList<Observation>();
                Iterator<VGDLSprite> spriteIt = spriteGroups[i].getSpriteIterator();
                if(spriteIt != null) while(spriteIt.hasNext())
                {
                    VGDLSprite sp = spriteIt.next();
                    Observation observation = new Observation(i, sp.spriteID, sp.getPosition(), reference);
                    observations[idx].add(observation);
                }
                if(reference != Types.NIL)
                    Collections.sort(observations[idx]);

                idx++;
            }
        }

        return observations;
    }

    /**
     * Returns a list of observations of NPC in the game. As there can be
     * NPCs of different type, each entry in the array corresponds to a sprite type.
     * Every ArrayList contains a list of objects of type Observation, ordered asc. by
     * distance to the avatar. Each Observation holds the position, unique id and
     * sprite id of that particular sprite.
     *
     * @param refPosition Reference position to use when sorting this array,
     *                    by ascending distance to this point.
     * @return Observations of NPCs in the game.
     */
    public ArrayList<Observation>[] getNPCPositions(Vector2d refPosition)
    {
        return getPositionsFrom(npcList, refPosition);
    }

    /**
     * Observations of static objects in the game.
     * @param refPosition Reference position to use when sorting this array,
     *                    by ascending distance to this point.
     * @return a list with the observations of static objects in the game..
     */
    public ArrayList<Observation>[] getImmovablePositions(Vector2d refPosition) {
        return getPositionsFrom(immList, refPosition);
    }

    /**
     * Returns a list with observations of sprites that move, but are NOT NPCs.
     * @param refPosition Reference position to use when sorting this array,
     *                    by ascending distance to this point.
     * @return a list with observations of sprites that move, but are NOT NPCs.
     */
    public ArrayList<Observation>[] getMovablePositions(Vector2d refPosition) {
        return getPositionsFrom(movList, refPosition);
    }

    /*
    * Returns a list with observations of resources.
     * @param refPosition Reference position to use when sorting this array,
     *                    by ascending distance to this point.
    * @return a list with observations of resources.
    */
    public ArrayList<Observation>[] getResourcesPositions(Vector2d refPosition) {
        return getPositionsFrom(resList, refPosition);
    }

    /*
     * Returns a list with observations of portals.
     * @param refPosition Reference position to use when sorting this array,
     *                    by ascending distance to this point.
     * @return a list with observations of portals.
     */
    public ArrayList<Observation>[] getPortalsPositions(Vector2d refPosition) {
        return getPositionsFrom(portalList, refPosition);
    }


    //Must override this:
    @Override
    public void buildLevel(String gamelvl) {
        throw new RuntimeException("buildLevel should not be called in this instance.");
    }

}
