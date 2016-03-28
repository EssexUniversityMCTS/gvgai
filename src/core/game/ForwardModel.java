package core.game;

import java.awt.Dimension;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import core.SpriteGroup;
import core.VGDLSprite;
import ontology.Types;
import ontology.avatar.MovingAvatar;
import ontology.effects.TimeEffect;
import tools.Pair;
import tools.Vector2d;

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
     * Boolean map of sprite types that created by the avatar.
     * fromAvatar[spriteType]==true : spriteType is created by the avatar.
     */
    private boolean fromAvatar[];

    /**
     * Boolean map of sprite types that are unknown.
     * knownList[spriteType]==false : spriteType is unknown.
     */
    private boolean knownList[];

    /**
     * Boolean map of sprite types that are not hidden.
     * visibleList[spriteType]==true : sprite.hidden = false;
     */
    private boolean visibleList[];

    /**
     * List of (persistent) observations for all sprites, indexed by sprite ID.
     */
    private HashMap<Integer, Observation> observations;

    /**
     * Observation grid
     */
    private ArrayList<Observation>[][] observationGrid;

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
        historicEvents = new TreeSet<Event>();
        shieldedEffects = new ArrayList[numSpriteTypes];

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

                if(!spCopy.hidden) {
                    checkSpriteFeatures(spCopy, i);
                    updateObservation(spCopy);
                }
            }

            int nSprites = spriteGroups[i].numSprites();
            num_sprites += nSprites;

            //copy the shields
            shieldedEffects[i] = new ArrayList<>();
            for(Pair p : a_gameState.shieldedEffects[i])
                shieldedEffects[i].add(p.copy());
        }

        //events:
        Iterator<Event> itEvent = a_gameState.historicEvents.iterator();
        while(itEvent.hasNext())
        {
            historicEvents.add(itEvent.next().copy());
        }

        //copy the time effects:
        this.timeEffects = new TreeSet<TimeEffect>();
        Iterator<TimeEffect> timeEffects = a_gameState.timeEffects.descendingIterator();
        while(timeEffects.hasNext())
        {
            TimeEffect tef = timeEffects.next().copy();
            this.timeEffects.add(tef);
        }
        //System.out.println("Tef size: " + this.timeEffects.size());

        //Game state variables:
        this.gameTick = a_gameState.gameTick;
        this.isEnded = a_gameState.isEnded;
        this.winner = a_gameState.winner;
        this.score = a_gameState.score;
        this.avatarLastAction = a_gameState.avatarLastAction;
        this.nextSpriteID = a_gameState.nextSpriteID;
    }

    /**
     * Updates the persistent observation of this sprite, or creates it if the
     * observation is new.
     * @param sprite sprite to take the observation from.
     */
    private void updateObservation(VGDLSprite sprite)
    {
        int spriteId = sprite.spriteID;
        boolean moved = false, newObs = false;
        Observation obs;
        Vector2d oldPosition = null;
        if(observations.containsKey(spriteId))
        {
            obs = observations.get(spriteId);
            oldPosition = obs.position;
            moved = ! obs.position.equals(sprite.getPosition());
            obs.position = sprite.getPosition();
        }else
        {
            obs = createSpriteObservation(sprite);
            newObs = true;
        }

        updateGrid(obs, newObs, moved, oldPosition);
    }

    /**
     * Removes an sprite observation.
     * @param sprite sprite to remove.
     */
    public final void removeSpriteObservation(VGDLSprite sprite)
    {
        int spriteId = sprite.spriteID;

        if(observations.containsKey(spriteId))
        {
            Observation obs = observations.get(spriteId);
            removeObservationFromGrid(obs, obs.position);
            observations.remove(spriteId);
        }
    }

    /**
     * Updates a grid observation.
     * @param obs observation to update
     * @param newObs if this is a new observation.
     * @param moved if it is a past observation, and it moved.
     * @param oldPosition the old position of this observation if it moved.
     */
    private void updateGrid(Observation obs, boolean newObs, boolean moved, Vector2d oldPosition)
    {
        //Insert observation in the grid position.
        if(newObs || moved)
        {
            //First, remove observation if the sprite moved.
            if(moved)
                removeObservationFromGrid(obs, oldPosition);

            addObservationToGrid(obs, obs.position);
        }
    }

    /**
     * Removes an observation to the grid, from the position specified.
     * @param obs observation to delete.
     * @param position where the sprite was located last time seen.
     */
    private void removeObservationFromGrid(Observation obs, Vector2d position)
    {
        int x = (int) position.x / block_size;
        boolean validX = x >= 0 && x < observationGrid.length;
        boolean xPlus = (position.x % block_size) > 0 && (x+1 < observationGrid.length);
        int y = (int) position.y / block_size;
        boolean validY = y >= 0 && y < observationGrid[0].length;
        boolean yPlus = (position.y % block_size) > 0 && (y+1 < observationGrid[0].length);

        if(validX && validY)
        {
            observationGrid[x][y].remove(obs);
            if(xPlus)
                observationGrid[x+1][y].remove(obs);
            if(yPlus)
                observationGrid[x][y+1].remove(obs);
            if(xPlus && yPlus)
                observationGrid[x+1][y+1].remove(obs);
        }
    }

    /**
     * Adds an observation to the grid, in the position specified.
     * @param obs observation to add.
     * @param position where to be added.
     */
    private void addObservationToGrid(Observation obs, Vector2d position)
    {
        int x = (int) position.x / block_size;
        boolean validX = x >= 0 && x < observationGrid.length;
        boolean xPlus = (position.x % block_size) > 0 && (x+1 < observationGrid.length);
        int y = (int) position.y / block_size;
        boolean validY = y >= 0 && y < observationGrid[0].length;
        boolean yPlus = (position.y % block_size) > 0 && (y+1 < observationGrid[0].length);

        if(validX && validY)
        {
            observationGrid[x][y].add(obs);
            if(xPlus)
                observationGrid[x+1][y].add(obs);
            if(yPlus)
                observationGrid[x][y+1].add(obs);
            if(xPlus && yPlus)
                observationGrid[x+1][y+1].add(obs);
        }
    }

    /**
     * Prints the observation grid. For debug only.
     */
    public void printObservationGrid()
    {
        System.out.println("#########################");
        for(int j = 0; j < observationGrid[0].length; ++j)
        {
            for(int i = 0; i < observationGrid.length; ++i)
            {
                int n = observationGrid[i][j].size();
                if(n > 0)
                    System.out.print(n);
                else
                    System.out.print(' ');
            }
            System.out.println();
        }
    }


    /**
     * Creates the sprite observation of a given sprite.
     * @param sprite sprite to create the observation from.
     * @return the observation object.
     */
    private Observation createSpriteObservation(VGDLSprite sprite)
    {
        int category = getSpriteCategory(sprite);
        Observation obs = new Observation(sprite.getType(), sprite.spriteID, sprite.getPosition(), Types.NIL, category);
        observations.put(sprite.spriteID, obs);
        return obs;
    }

    /**
     * Gets the sprite observation of a given sprite. Creates it if id didn't exist.
     * @param sprite sprite to get/create the observation from.
     * @return the observation object.
     */
    private Observation getSpriteObservation(VGDLSprite sprite)
    {
        int spriteId = sprite.spriteID;
        if(observations.containsKey(spriteId))
        {
            return observations.get(spriteId);
        }else{
            return createSpriteObservation(sprite);
        }
    }

    /**
     * Checks some features of the sprite, to categorize it.
     * @param sp Sprite to categorize.
     * @param itype itype of the sprite.
     */
    private void checkSpriteFeatures(VGDLSprite sp, int itype)
    {

        int category = getSpriteCategory(sp);
        switch (category)
        {
            case Types.TYPE_AVATAR:
                this.avatar = (MovingAvatar) sp;
                break;
            case Types.TYPE_RESOURCE:
                resList[itype] = true;
                break;
            case Types.TYPE_PORTAL:
                portalList[itype] = true;
                break;
            case Types.TYPE_NPC:
                npcList[itype] = true;
                break;
            case Types.TYPE_STATIC:
                immList[itype] = true;
                break;
            case Types.TYPE_FROMAVATAR:
                fromAvatar[itype] = true;
                break;
            case Types.TYPE_MOVABLE:
                movList[itype] = true;
        }
        knownList[itype] = true;
        visibleList[itype] = !sp.hidden;
    }

    private int getSpriteCategory(VGDLSprite sp)
    {
        if(sp.is_avatar)
            return Types.TYPE_AVATAR;

        //Is it a resource?
        if(sp.is_resource)
            return Types.TYPE_RESOURCE;

        //Is it a portal?
        if(sp.portal)
            return Types.TYPE_PORTAL;

        //Is it npc?
        if(sp.is_npc)
            return Types.TYPE_NPC;

        //Is it immovable?
         if(sp.is_static)
            return Types.TYPE_STATIC;

        //is it created by the avatar?
        if(sp.is_from_avatar)
            return Types.TYPE_FROMAVATAR;

        return Types.TYPE_MOVABLE;
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
        this.avatarLastAction = a_gameState.avatarLastAction;

        //create the boolean maps of sprite types.
        npcList = new boolean[a_gameState.spriteGroups.length];
        immList = new boolean[a_gameState.spriteGroups.length];
        movList = new boolean[a_gameState.spriteGroups.length];
        resList = new boolean[a_gameState.spriteGroups.length];
        portalList  = new boolean[a_gameState.spriteGroups.length];
        fromAvatar  = new boolean[a_gameState.spriteGroups.length];
        knownList = new boolean[a_gameState.spriteGroups.length];
        visibleList = new boolean[a_gameState.spriteGroups.length];

        observations = new HashMap<Integer, Observation>();
        observationGrid = new ArrayList[screenSize.width/block_size][screenSize.height/block_size];
        for(int i = 0; i < observationGrid.length; ++i)
            for(int j = 0; j < observationGrid[i].length; ++j)
                observationGrid[i][j] = new ArrayList<Observation>();

        this.pathf = a_gameState.pathf;
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

    /**
     * Sets a new seed for the forward model's random generator (creates a new object)
     *
     * @param seed the new seed.
     */
    public void setNewSeed(int seed)
    {
        randomObs = new Random(seed);
    }


    /************** Useful functions for the agent *******************/

    /**
     * Performs one tick for the game: calling update(this) in all sprites. It follows the
     * same order of update calls as in the real game (inverse spriteOrder[]). Avatar moves
     * the first one. It uses the action received as the action of the avatar.
     * @param action Action to be performed by the avatar for this game tick.
     */
    protected void tick(Types.ACTIONS action)
    {
        this.ki.reset();
        this.ki.setAction(action);
        avatar.preMovement();
        avatar.move(this, this.ki.getMask());

        for(int i = spriteOrder.length-1; i >= 0; --i)
        {
            int spriteTypeInt = spriteOrder[i];

            Iterator<VGDLSprite> spriteIt = spriteGroups[spriteTypeInt].getSpriteIterator();
            if(spriteIt != null) while(spriteIt.hasNext())
            {
                VGDLSprite sp = spriteIt.next();

                if(sp != avatar)
                {
                    sp.preMovement();
                    sp.update(this);
                }
            }
        }

    }


    /**
     * Advances the forward model using the acction supplied.
     * @param action
     */
    final public void advance(Types.ACTIONS action)
    {
        if(!isEnded)
        {
            tick(action);
            eventHandling();
            clearAll(this);
            terminationHandling();
            checkTimeOut();
            updateAllObservations();
            gameTick++;
        }
    }

    /**
     * Updates all observations of this class.
     */
    final private void updateAllObservations()
    {
        //Now, update all others (but avatar).
        int typeIndex = spriteOrder.length-1;
        for(int i = typeIndex; i >=0; --i)   //For update, opposite order than drawing.
        {
            int spriteTypeInt = spriteOrder[i];

            Iterator<VGDLSprite> spriteIt = spriteGroups[spriteTypeInt].getSpriteIterator();
            if(spriteIt != null) while(spriteIt.hasNext())
            {
                VGDLSprite sp = spriteIt.next();
                updateObservation(sp);
            }
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

    /**
     * Returns the world dimensions, in pixels.
     * @return the world dimensions, in pixels.
     */
    public Dimension getWorldDimension()
    {
        return screenSize;
    }


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
     * the avatar. If the parameter 'includeNIL' is true, the array contains the (always available)
     * NIL action. If it is false, this is equivalent to calling getAvailableActions().
     * @param includeNIL true to include Types.ACTIONS.ACTION_NIL in the array of actions.
     * @return the available actions.
     */
    public ArrayList<Types.ACTIONS> getAvatarActions(boolean includeNIL)
    {
        if(isEnded)
            return new ArrayList<Types.ACTIONS>();
        if(includeNIL)
            return avatar.actionsNIL;
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

    /**
     * Returns the avatar's last move. At the first game cycle, it returns ACTION_NIL.
     * Note that this may NOT be the same as the last action given by the agent, as it may
     * have overspent in the last game cycle.
     * @return the action that was executed in the real game in the last cycle. ACTION_NIL
     * is returned in the very first game step.
     */
    public Types.ACTIONS getAvatarLastAction()
    {
        if(avatarLastAction != null)
            return avatarLastAction;
        else return Types.ACTIONS.ACTION_NIL;
    }


    /**
     * Returns the avatar's type. In case it has multiple types, it returns the most specific one.
     * @return the itype of the avatar.
     */
    public int getAvatarType()
    {
        return avatar.getType();
    }


    /**
     * Returns the health points of the avatar. A value of 0 doesn't necessarily
     * mean that the avatar is dead (could be that no health points are in use in that game).
     * @return a numeric value, the amount of remaining health points.
     */
    public int getAvatarHealthPoints() { return avatar.healthPoints; }

    /**
     * Returns the maximum amount of health points.
     * @return the maximum amount of health points the avatar can have.
     */
    public int getAvatarMaxHealthPoints() { return avatar.maxHealthPoints; }

    /**
     * Returns the limit of health points this avatar can have.
     * @return the limit of health points the avatar can have.
     */
    public int getAvatarLimitHealthPoints() {return avatar.limitHealthPoints;}


    /** Methods that return positions of things **/

    /**
     * Gets position from the sprites corresponding to the boolean map passed by parameter.
     * @param groupArray boolean map that indicates which sprite types must be considered.
     * @return List of arrays with Observations. Each entry in the array corresponds to a different
     * sprite type.
     */
    private ArrayList<Observation>[] getPositionsFrom(boolean[] groupArray, Vector2d refPosition)
    {
        //First, get how many types we have. Need to consider hidden sprites out.
        int numDiffTypes = 0;
        for(int i = 0; i < groupArray.length; ++i)
        {
            //There is a sprite type we don't know anything about. Need to check.
            if(!knownList[i] && spriteGroups[i].getFirstSprite() != null)
                checkSpriteFeatures(spriteGroups[i].getFirstSprite(), i);

            if(groupArray[i] && visibleList[i]) numDiffTypes++;
        }

        if(numDiffTypes == 0)
            return null; //Wait, no types? no sprites of this group then.

        ArrayList<Observation>[] observations = new ArrayList[numDiffTypes];
        Vector2d reference = refPosition;
        if(refPosition == null)
            reference = Types.NIL;

        int idx = 0;
        for(int i = 0; i < groupArray.length; ++i)
        {
            //For each one of the sprite types that belong to the specified category
            if(groupArray[i] && visibleList[i])
            {
                observations[idx] = new ArrayList<Observation>();
                Iterator<VGDLSprite> spriteIt = spriteGroups[i].getSpriteIterator();
                if(spriteIt != null) while(spriteIt.hasNext())
                {
                    VGDLSprite sp = spriteIt.next();

                    Observation observation = getSpriteObservation(sp);
                    observation.update(i, sp.spriteID, sp.getPosition(), reference, getSpriteCategory(sp));

                    observation.reference = reference;
                    observations[idx].add(observation);
                }

                if(reference != Types.NIL)
                {
                    Collections.sort(observations[idx]);
                }

                idx++;
            }
        }

        return observations;
    }

    /**
     * Returns a grid with all observations in the level.
     * @return the grid of observations
     */
    public ArrayList<Observation>[][] getObservationGrid()
    {
        return observationGrid;
    }

    /**
     * Returns the list of historic events happened in this game so far.
     * @return list of historic events happened in this game so far.
     */
    public TreeSet<Event> getEventsHistory()
    {
        return historicEvents;
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

    /**
     * Returns a list of observations of objects created by the avatar's actions.
     * @param refPosition Reference position to use when sorting this array,
     *                    by ascending distance to this point.
     * @return a list with observations of sprites.
     */
    public ArrayList<Observation>[] getFromAvatarSpPositions(Vector2d refPosition)
    {
        return getPositionsFrom(fromAvatar, refPosition);
    }

    /**
     * Checks if the observations of both models are the same.
     * DEBUG ONLY METHOD.
     * @param other the forward model to compare to.
     * @return true if everything is the same.
     */
    public boolean equalObservations(ForwardModel other)
    {
        for(int i = 0; i < spriteGroups.length; ++i)
        {
            ConcurrentHashMap<Integer, VGDLSprite> thisSpriteMap = this.spriteGroups[i].getSprites();
            ConcurrentHashMap<Integer, VGDLSprite> otherSpriteMap = other.spriteGroups[i].getSprites();
            if(thisSpriteMap.size() != otherSpriteMap.size())
            {
                if(thisSpriteMap.size() > 25 && otherSpriteMap.size() > 25)
                {
                    //For reasons I don't fully understand (balancing?), ConcurrentHashMap returns the keySet
                    // in different order some times when there are many elements. This makes the update happen
                    // in a different order and therefore (in stochastic environments) things change. If this
                    // happens, we ignore this case (this scenario has only been seen in Firestorms so far).
                    return true;
                }
                return false;
            }

            Set<Integer> allOtherSpriteKeys = otherSpriteMap.keySet();
            for(Integer key : allOtherSpriteKeys)
            {
                VGDLSprite sp = thisSpriteMap.get(key);
                if(!otherSpriteMap.get(key).equiv(sp))
                    return false;
            }
        }
        return true;
    }

    //Must override this:
	@Override
	public void buildStringLevel(String[] levelString) {
		throw new RuntimeException("buildLevel should not be called in this instance.");
	}

}
