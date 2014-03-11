package core.game;

import core.VGDLSprite;
import ontology.Types;
import ontology.avatar.MovingAvatar;
import tools.Vector2d;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.Set;

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
        lastCollisions = new ArrayList[numSpriteTypes];

        //Copy of sprites from the game.
        spriteGroups = new ArrayList[numSpriteTypes];
        numSprites = new int[numSpriteTypes];
        num_sprites = 0;

        for(int i = 0; i < spriteGroups.length; ++i)
        {
            lastCollisions[i] = new ArrayList<VGDLSprite>();
            spriteGroups[i] = new ArrayList<VGDLSprite>();
            ArrayList<VGDLSprite> sprites = a_gameState.spriteGroups[i];
            for(VGDLSprite sp : sprites)
            {
                VGDLSprite spCopy = sp.copy();
                spriteGroups[i].add(spCopy);
                //For each sprite type, record some of its features.
                checkSpriteFeatures(spCopy, i);
            }
            int nSprites = spriteGroups[i].size();
            numSprites[i] = nSprites;
            num_sprites += nSprites;
        }

        //Game state variables:
        this.gameTick = a_gameState.gameTick;
        this.isEnded = a_gameState.isEnded;
        this.winner = a_gameState.winner;
        this.score = a_gameState.score;
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
     * Returns the resources owned by the avatar. As there can be resources of different
     * nature, each entry of the array indicates the amount of resource of each type.
     * If the game is finished, we cannot guarantee that this information is meaningful
     * at all (the avatar itself could be destroyed). If game finished, this returns null.
     * @return resources owned by the avatar, or null if game is over.
     */
    public int[] getAvatarResources()
    {
        //Determine how many different resources does the avatar have.
        int numTypesResources = avatar.resources.size();
        int[] owned = new int[numTypesResources];
        int idx = 0;

        //And for each type, add their amount.
        Set<Map.Entry<Integer, Integer>> entries = avatar.resources.entrySet();
        for(Map.Entry<Integer, Integer> entry : entries)
        {
            owned[idx++] = entry.getValue();
        }

        return owned;
    }


    /** Methods that return positions of things **/

    /**
     * Gets position from the sprites corresponding to the boolean map passed by parameter.
     * @param groupArray boolean map that indicates which sprite types must be considered.
     * @return List of arrays with positions. Each entry in the array corresponds to a different
     * sprite type.
     */
    private ArrayList<Vector2d>[] getPositionsFrom(boolean[] groupArray)
    {
        //First, get how many types we have.
        int numDiffTypes = 0;
        for(int i = 0; i < groupArray.length; ++i)    if(groupArray[i]) numDiffTypes++;

        if(numDiffTypes == 0)
            return null; //Wait, no types? no sprites of this group then.

        //Create the array of types and make some room for their positions.
        ArrayList<Vector2d> allPositions[] = new ArrayList[numDiffTypes];
        int idx = 0;
        for(int i = 0; i < groupArray.length; ++i)
        {
            if(groupArray[i])
            {
                //Create an arraylist for this type of sprite.
                ArrayList<Vector2d> positions = new ArrayList<Vector2d>();
                ArrayList<VGDLSprite> sprites =  spriteGroups[i];

                //Add all sprites to this arraylist.
                int numSprites = sprites.size();
                for(int j = 0; j < numSprites; j++)
                {
                    VGDLSprite sp = sprites.get(j);
                    positions.add(sp.getPosition());
                }

                //add this arraylist to the array of positions.
                allPositions[idx++] = positions;
            }
        }

        return allPositions;
    }

    /**
     * Returns the positions of the other NPCs in the game. If no NPCs in the game,
     * it returns null. As there can be NPCs of different type, the positions are returned
     * as an array of lists, each array for each type.
     * @return a list with the positions of the other NPCs in the game, null if there are no NPCs.
     */
    public ArrayList<Vector2d>[] getNPCPositions()
    {
        return getPositionsFrom(npcList);
    }

    /**
     * Returns a list with the positions of static objects.
     * @return a list with the positions of static objects.
     */
    public ArrayList<Vector2d>[] getImmovablePositions() {
        return getPositionsFrom(immList);
    }

    /**
     * Returns a list with the positions of elements that move, but are NOT NPCs.
     * @return a list with the positions of elements that move, but are NOT NPCs.
     */
    public ArrayList<Vector2d>[] getMovablePositions() {
        return getPositionsFrom(movList);
    }

    /*
    * Returns a list with the positions of resources.
    * @return a list with the positions of resources.
    */
    public ArrayList<Vector2d>[] getResourcesPositions() {
        return getPositionsFrom(resList);
    }

    /*
     * Returns a list with the positions of portals.
     * @return a list with the positions of portals.
     */
    public ArrayList<Vector2d>[] getPortalsPositions() {
        return getPositionsFrom(portalList);
    }


    //Must override this:
    @Override
    public void buildLevel(String gamelvl) {
        throw new RuntimeException("buildLevel should not be called in this instance.");
    }

}
