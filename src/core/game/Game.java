package core.game;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JOptionPane;

import core.SpriteGroup;
import core.VGDLFactory;
import core.VGDLRegistry;
import core.VGDLSprite;
import core.VGDLViewer;
import core.competition.CompetitionParameters;
import core.content.Content;
import core.content.GameContent;
import core.content.SpriteContent;
import core.game.GameDescription.InteractionData;
import core.game.GameDescription.SpriteData;
import core.game.GameDescription.TerminationData;
import core.player.AbstractPlayer;
import core.termination.Termination;
import ontology.Types;
import ontology.avatar.MovingAvatar;
import ontology.effects.Effect;
import ontology.effects.TimeEffect;
import ontology.sprites.Resource;
import tools.IO;
import tools.JEasyFrame;
import tools.KeyHandler;
import tools.KeyInput;
import tools.KeyPulse;
import tools.Pair;
import tools.Vector2d;
import tools.WindowInput;
import tools.pathfinder.Node;
import tools.pathfinder.PathFinder;


/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 17/10/13
 * Time: 13:42
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public abstract class Game
{

    /**
     * z-level of sprite types (in case of overlap)
     */
    protected int[] spriteOrder;

    /**
     * Singletons of the game.
     */
    protected boolean[] singletons;

    /**
     * Content objects for the different sprite types..
     * The index is the type of object
     * Content encloses information about the class of the object and its parameters.
     */
    protected Content[] classConst;

    /**
     * Groups of sprites in the level. Each element of the array is a
     * collection of sprites of a given type, which is also the index
     * of the array.
     */
    protected SpriteGroup[] spriteGroups;

    /**
     * Relationships for collisions: double array of (list of) effects. Interaction between
     * two sprites can trigger more than one effect.
     *  collisionEffects[]   -> int id of the FIRST element taking part on the effects.
     *  collisionEffects[][] -> int id of the SECOND element taking part on the effects.
     *
     */
    protected ArrayList<Effect>[][] collisionEffects;


    /**
     * Pairs of all defined effects in the game.
     */
    protected ArrayList<Pair<Integer,Integer>> definedEffects;


    /**
     * List of EOS effects
     */
    protected ArrayList<Effect>[] eosEffects;

    /**
     * List of TIME effects
     */
    protected TreeSet<TimeEffect> timeEffects;


    /**
     * List of types that can trigger an EOS effect.
     */
    protected ArrayList<Integer> definedEOSEffects;

    /**
     * Historic of events related to the avatar happened during the game.
     * The entries are ordered asc. by game step.
     */
    protected TreeSet<Event> historicEvents;


    /**
     * For each entry, int identifier of sprite type, a list with all the itypes this
     * sprite belongs to.
     */
    protected ArrayList<Integer>[] iSubTypes;


    /**
     * For each entry, int identifier of sprite type, a list with all the itypes this
     * sprite belongs to.
     */
    protected ArrayList<Pair<Integer,Long>>[] shieldedEffects;


    /**
     * Arraylist to hold collisions between objects in every frame
     */
    protected Bucket[] bucketList;

    /**
     * Mapping between characters in the level and the entities they represent.
     */
    protected HashMap<Character,ArrayList<String>> charMapping;


    /**
     * Termination set conditions to finish the game.
     */
    protected ArrayList<Termination> terminations;

    /**
     * List of sprites killed in the game.
     */
    protected ArrayList<VGDLSprite> kill_list;

    /**
     * Limit number of each resource type
     */
    protected int[] resources_limits;

    /**
     * Color for each resource
     */
    protected Color[] resources_colors;

    /**
     * Screen size.
     */
    protected Dimension screenSize;

    /**
     * Dimensions of the game.
     */
    protected Dimension size;

    /**
     * Indicates if the game is stochastic.
     */
    protected boolean is_stochastic;

    /**
     * Number of sprites this game has.
     */
    protected int num_sprites;

    /**
     * Game tick
     */
    protected int gameTick;

    /**
     * Key input
     */
    public static KeyHandler ki = CompetitionParameters.KEY_HANDLER == CompetitionParameters.KEY_INPUT ? 
    		new KeyInput() : new KeyPulse();

    /**
     * Handling when the window is closed
     */
    public static WindowInput wi = new WindowInput();
    
    /**
     * Size of the block in pixels.
     */
    protected int block_size = 10;

    /**
     * Quick reference to the gamer
     */
    protected MovingAvatar avatar;

    /**
     * Indicates if the game is ended.
     */
    protected boolean isEnded;

    /**
     * Indicates if the game has been won by the player.
     * See Types.WINNER for the values of this variable.
     */
    protected Types.WINNER winner = Types.WINNER.NO_WINNER;

    /**
     * Default frame rate of the basic game.
     */
    protected int frame_rate;

    /**
     * State observation for this game.
     */
    protected ForwardModel fwdModel;

    /**
     * Score of the game.
     */
    protected double score;

    /**
     * Maximum number of sprites in a game.
     */
    protected int MAX_SPRITES;

    /**
     * Random number generator for this game. It can only be received when the game is started.
     */
    private Random random;

    /**
     * Id of the sprite type "avatar".
     */
    private int avatarId;

    /**
     * Id of the sprite type "wall".
     */
    private int wallId;

    /**
     * Flag that can only be set to true externally. If true,
     * the agent is disqualified.
     */
    private boolean disqualified;

    /**
     * Next ID to generate for sprites;
     */
    protected int nextSpriteID;


    /**
     * Key Handler for human play. The default is CompetitionParameters.KEY_INPUT
     */
    public String key_handler;


    /**
     * Pathfinder.
     */
    protected PathFinder pathf;


    /**
     * Avatar last action.
     */
    protected Types.ACTIONS avatarLastAction;

    /**
     * Default constructor.
     */
    public Game()
    {
        //data structures to hold the game definition.
        definedEffects = new ArrayList<Pair<Integer,Integer>>();
        definedEOSEffects = new ArrayList<Integer>();
        charMapping = new HashMap<Character,ArrayList<String>>();
        terminations = new ArrayList<Termination>();
        historicEvents = new TreeSet<Event>();
        timeEffects = new TreeSet<TimeEffect>();

        //Game attributes:
        size = new Dimension();
        is_stochastic = false;
        disqualified = false;
        num_sprites = 0;
        nextSpriteID = 0;

        loadDefaultConstr();
    }

    /**
     * Loads the constructor information for default objects (walls, avatar).
     */
    private void loadDefaultConstr()
    {
        //If more elements are added here, initSprites() must be modified accordingly!
        VGDLRegistry.GetInstance().registerSprite("wall");
        VGDLRegistry.GetInstance().registerSprite("avatar");
    }


    /**
     * Initializes the sprite structures that hold the game.
     * @param spOrder order of sprite types to be drawn on the screen.
     * @param sings sprites that are marked as singletons.
     * @param constructors map of sprite constructor's information.
     */
    public void initSprites(ArrayList<Integer> spOrder, ArrayList<Integer> sings,
                            HashMap<Integer, SpriteContent> constructors)
    {
        ArrayList<Resource> resources = new ArrayList<Resource>();
        spriteOrder = new int[spOrder.size()];

        //We need here the default 2 sprites:
        avatarId = VGDLRegistry.GetInstance().getRegisteredSpriteValue("avatar");
        wallId = VGDLRegistry.GetInstance().getRegisteredSpriteValue("wall");

        //1. "avatar" ALWAYS at the end of the array.
        spriteOrder[spriteOrder.length-1] = avatarId;

        //2. Other sprite types are sorted using spOrder
        int i = 0;
        for(Integer intId : spOrder)
        {
            if(intId != avatarId)
            {
                spriteOrder[i++] = intId;
            }
        }

        //Singletons
        singletons = new boolean[VGDLRegistry.GetInstance().numSpriteTypes()];
        for(Integer intId : sings)
        {
            singletons[intId] = true;
        }

        //Constructors, as many as number of sprite types, so they are accessed by its id:
        classConst = new Content[VGDLRegistry.GetInstance().numSpriteTypes()];

        //By default, we have 2 constructors:
        Content wallConst = new SpriteContent("wall", "Immovable");
        wallConst.parameters.put("color","DARKGRAY");
        ((SpriteContent)wallConst).itypes.add(wallId);
        classConst[wallId] = wallConst;
        Content avatarConst = new SpriteContent("avatar", "MovingAvatar");
        ((SpriteContent)avatarConst).itypes.add(avatarId);
        classConst[avatarId] = avatarConst;

        //Now, the other constructors.
        Set<Map.Entry<Integer, SpriteContent>> entries = constructors.entrySet();
        for(Map.Entry<Integer, SpriteContent> entry : entries)
        {
            classConst[entry.getKey()] = entry.getValue();

            //Special case: we create a dummy Resource sprite of each resource type.
            String refClass = entry.getValue().referenceClass;
            if(refClass != null && refClass.equals("Resource"))
            {
                VGDLSprite resourceTest = VGDLFactory.GetInstance().
                        createSprite(entry.getValue(), new Vector2d(0,0), new Dimension(1,1));
                resources.add((Resource)resourceTest);
            }
        }

        //Structures to hold game sprites, as many as number of sprite types, so they are accessed by its id:
        spriteGroups = new SpriteGroup[classConst.length];
        shieldedEffects = new ArrayList[classConst.length];
        collisionEffects = new ArrayList[classConst.length][classConst.length];
        eosEffects = new ArrayList[classConst.length];
        iSubTypes = new ArrayList[classConst.length];
        bucketList = new Bucket[classConst.length];
        resources_limits = new int[classConst.length];
        resources_colors = new Color[classConst.length];

        //For each sprite type...
        for(int j = 0; j < spriteGroups.length; ++j)
        {
            //Create the space for the sprites and effects of this type.
            spriteGroups[j] = new SpriteGroup(j);
            shieldedEffects[j] = new ArrayList<>();
            eosEffects[j] = new ArrayList<Effect>();
            timeEffects = new TreeSet<TimeEffect>();
            bucketList[j] = new Bucket();

            //Declare the extended types list of this sprite type.
            iSubTypes[j] = (ArrayList<Integer>) ((SpriteContent)classConst[j]).subtypes.clone();

            for(int k = 0; k < spriteGroups.length; ++k)
            {
                //Create the array list of collision effects for each pair of sprite types.
                collisionEffects[j][k] = new ArrayList<Effect>();
            }

        }

        //Add walls and avatar to the subtypes list.
        if(!iSubTypes[wallId].contains(wallId))
            iSubTypes[wallId].add(wallId);

        if(!iSubTypes[avatarId].contains(avatarId))
            iSubTypes[avatarId].add(avatarId);

        //Resources: use the list of resources created before to store limit and color of each resource.
        for(i = 0; i < resources.size(); ++i)
        {
            Resource r = resources.get(i);
            resources_limits[r.resource_type] = r.limit;
            resources_colors[r.resource_type] = r.color;
        }
    }
    
    /**
     * Check if the current itype has no children nodes
     * @param itype	sprite index
     * @return		true if its lead node, false otherwise
     */
    private boolean isLeafNode(int itype){
    	SpriteContent sc = (SpriteContent)classConst[itype];
    	
    	return sc.subtypes.size() <= 1 || 
    			sc.subtypes.get(sc.subtypes.size() - 1) == itype;
    }
    
    /**
     * Get all parent sprites for a certain sprite
     * @param itype id for the current node
     * @return a list of all parent nodes' ids
     */
    private ArrayList<Integer> parentNodes(int itype){
    	SpriteContent sc = (SpriteContent)classConst[itype];
    	
    	ArrayList<Integer> parents = new ArrayList<Integer>();
    	parents.addAll(sc.itypes);
    	parents.remove(parents.size() - 1);
    	
    	return parents;
    }
    
    /**
     * Expand a non leaf node using its children
     * @param itype	sprite index
     * @return		a list of all leaf children under the hierarchy of itype sprite
     */
    private ArrayList<String> expandNonLeafNode(int itype){
    	ArrayList<String> result = new ArrayList<String>();
    	boolean[] visited = new boolean[classConst.length];
    	ArrayList<Integer> queue = new ArrayList<Integer>();
    	queue.add(itype);
    	
    	while(!queue.isEmpty()){
    		int current = queue.remove(0);
    		if(visited[current]){
    			continue;
    		}
    		
    		if(isLeafNode(current)){
    			result.add(VGDLRegistry.GetInstance().getRegisteredSpriteKey(current));
    		}
    		else{
    			SpriteContent sc = (SpriteContent)classConst[current];
    			queue.addAll(sc.subtypes);
    		}
    		visited[current] = true;
    	}
    	
    	return result;
    }
    
    /**
     * return sprite type of certain sprite
     * @param sp	sprite object
     * @return		sprite type (avatar, resource, portal, npc, static, moving)
     */
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
     * Convert a sprite content object to Sprite Data object
     * @param sc	sprite content object for a certain sprite
     * @return		sprite data object for the current sprite content
     */
    private SpriteData initializeSpriteData(SpriteContent sc){
    	SpriteData data = new SpriteData();
    	data.name = sc.identifier;
    	data.type = sc.referenceClass;
    	
    	VGDLSprite sprite = VGDLFactory.GetInstance().createSprite(sc, new Vector2d(), new Dimension(1, 1));
    	switch(getSpriteCategory(sprite)){
    	case Types.TYPE_NPC:
    		data.isNPC = true;
    		break;
    	case Types.TYPE_AVATAR:
    		data.isAvatar = true;
    		break;
    	case Types.TYPE_PORTAL:
    		data.isPortal = true;
    		break;
    	case Types.TYPE_RESOURCE:
    		data.isResource = true;
    		break;
    	case Types.TYPE_STATIC:
    		data.isStatic = true;
    		break;
    	}
    	
    	ArrayList<String> dependentSprites = sprite.getDependentSprites();
    	for(String s:dependentSprites){
    		ArrayList<String> expandedSprites = expandNonLeafNode(VGDLRegistry.GetInstance().getRegisteredSpriteValue(s));
    		data.sprites.addAll(expandedSprites);
    	}
    	
    	return data;
    }
    
    /**
     * Get an array of sprite data objects for all leaf sprite nodes.
     * @return	Array of sprite data
     */
    public ArrayList<SpriteData> getSpriteData(){
    	ArrayList<SpriteData> result = new ArrayList<SpriteData>();
    	
    	for(int i = 0; i < classConst.length; i++){
    		SpriteContent sc = (SpriteContent)classConst[i];
    		if(isLeafNode(i)){
    			result.add(initializeSpriteData(sc));
    		}
    	}
    	
    	return result;
    }
    
    /**
     * Construct and return a temporary avatar sprite
     * @return a temproary avatar sprite
     */
    public VGDLSprite getTempAvatar(SpriteData sprite){
    	avatarId = VGDLRegistry.GetInstance().getRegisteredSpriteValue(sprite.name);
    	if(((SpriteContent)classConst[avatarId]).referenceClass != null){
    		VGDLSprite result = VGDLFactory.GetInstance().createSprite((SpriteContent) classConst[avatarId], 
    				new Vector2d(), new Dimension(1, 1));
    		if(result != null){
    			return result;
    		}
    	}
    	
    	return null;
    }
    
    /**
     * Return an array of termination data objects. These objects represents 
     * the termination conditions for the game
     * @return array of Termination Data objects
     */
    public ArrayList<TerminationData> getTerminationData(){
    	ArrayList<TerminationData> result = new ArrayList<TerminationData>();
    	
    	TerminationData td;
    	for(Termination tr:terminations){
    		td = new TerminationData();
    		int lastDot = tr.getClass().getName().lastIndexOf('.');
    		td.type = tr.getClass().getName().substring(lastDot + 1);
    		td.limit = tr.limit;
    		td.win = tr.win;
    		
    		ArrayList<String> sprites = tr.getTerminationSprites();
    		for(String s:sprites){
    			int itype = VGDLRegistry.GetInstance().getRegisteredSpriteValue(s);
    			if(isLeafNode(itype)){
    				td.sprites.add(s);
    			}
    			else{
    				td.sprites.addAll(expandNonLeafNode(itype));
    			}
    		}
    		
    		result.add(td);
    	}
    	
    	return result;
    }
    
    /**
     * Get a list of interaction data objects between two sprite types. 
     * These objects represents the effect happened to the first sprite type.
     * @param itype1	The first sprite type object
     * @param itype2	The second sprite type object
     * @return			array of interaction data objects.
     */
    public ArrayList<InteractionData> getInteractionData(int itype1, int itype2){
    	ArrayList<InteractionData> results = new ArrayList<InteractionData>();
    	
    	ArrayList<Integer> parent1 = new ArrayList<Integer>();
    	ArrayList<Integer> parent2 = new ArrayList<Integer>();
    	
    	if(itype1 != -1){
    		parent1.addAll(parentNodes(itype1));
    		parent1.add(itype1);
    	}
    	
    	if(itype2 != -1){
    		parent2.addAll(parentNodes(itype2));
    		parent2.add(itype2);
    	}
    	
    	ArrayList<Effect> effects = new ArrayList<Effect>();
    	if(parent1.size() > 0 && parent2.size() > 0){
    		for(int p1:parent1){
    			for(int p2:parent2){
    				effects.addAll(getCollisionEffects(p1, p2));
    			}
    		}
    	}
    	else if(parent1.size() > 0){
    		for(int p1:parent1){
    			effects.addAll(getEosEffects(p1));

    		}
    	}
    	else if(parent2.size() > 0){
    		for(int p2:parent2){
    			effects.addAll(getEosEffects(p2));
    		}
    	}
    	
    	InteractionData temp;
    	for(Effect e:effects){
    		temp = new InteractionData();
    		temp.type = e.getClass().getName();
    		temp.type = temp.type.substring(temp.type.lastIndexOf('.') + 1);
    		temp.scoreChange = e.scoreChange;
    		temp.sprites.addAll(e.getEffectSprites());
    			
    		results.add(temp);
     	}

    	return results;
    }

    /**
     * Sets the game back to the state prior to load a level.
     */
    public void reset()
    {
        num_sprites = 0;
        winner = Types.WINNER.NO_WINNER;
        isEnded = false;
        gameTick=-1;
        avatar = null;
        score = 0;
        disqualified=false;

        //For each sprite type...
        for(int i = 0; i < spriteGroups.length; ++i)
        {
            //Create the space for the sprites and effects of this type.
            spriteGroups[i].clear();
        }
        
        if(kill_list != null){
        	kill_list.clear();
        }
        for(int j = 0; j < spriteGroups.length; ++j)
        {
            bucketList[j].clear();
        }

        resetShieldEffects();
    }

    /**
     * Starts the forward model for the game.
     */
    public void initForwardModel()
    {
        fwdModel = new ForwardModel(this);
        fwdModel.update(this);
    }

    /**
     * Reads the parameters of a game type.
     * @param content list of parameter-value pairs.
     */
    protected void parseParameters(GameContent content)
    {
        VGDLFactory factory = VGDLFactory.GetInstance();
        Class refClass = VGDLFactory.registeredGames.get(content.referenceClass);
        //System.out.inn("refClass" + refClass.toString());
        if (!this.getClass().equals(refClass)) {
            System.out.println("Error: Game subclass instance not the same as content.referenceClass" +
                    " " + this.getClass() +
                    " " + refClass);
            return;
        }

        factory.parseParameters(content, this);

        if(key_handler != null && key_handler.equalsIgnoreCase("Pulse"))
            CompetitionParameters.KEY_HANDLER = CompetitionParameters.KEY_PULSE;

        ki = CompetitionParameters.KEY_HANDLER == CompetitionParameters.KEY_INPUT ?
                new KeyInput() : new KeyPulse();
    }

    /**
     * Adds a new sprite to the pool of sprites of the game. Increments the sprite
     * counter and also modifies is_stochastic and the avatar accordingly.
     * @param sprite the new sprite to add.
     * @param itype main int type of this sprite (leaf of the hierarchy of types).
     */
    protected void addSprite(VGDLSprite sprite, int itype)
    {
        sprite.spriteID = nextSpriteID;
        spriteGroups[itype].addSprite(nextSpriteID++, sprite);
        num_sprites++;

        if(sprite.is_stochastic)
            this.is_stochastic = true;

        if(itype == wallId)
        {
            sprite.loadImage("wall.png");
        }else if(itype == avatarId)
        {
            sprite.loadImage("avatar.png");
        }
    }

    /**
     * Returns the number of sprites of the type given by parameter, and all its subtypes
     * @param itype parent itype requested.
     * @return the number of sprites of the type and subtypes.
     */
    public int getNumSprites(int itype)
    {
        int acum = 0;
        for( Integer subtype : this.iSubTypes[itype] )
        {
            acum += spriteGroups[subtype].numSprites();
        }
        return acum;
    }

    /**
     * Runs a game, without graphics.
     * @param player Player that plays this game.
     * @param randomSeed sampleRandom seed for the whole game.
     * @return the score of the game played.
     */
    public double runGame(AbstractPlayer player, int randomSeed)
    {
        //Prepare some structures and references for this game.
        prepareGame(player, randomSeed);

        //Play until the game is ended
        while(!isEnded)
        {
            this.gameCycle(); //Execute a game cycle.
        }

        //Update the forward model for the game state sent to the controller.
        fwdModel.update(this);

        return handleResult();
    }


    /**
     * Plays the game, graphics enabled.
     * @param player Player that plays this game.
     * @param randomSeed sampleRandom seed for the whole game.
     * @param isHuman indicates if a human is playing the game.
     * @return the score of the game played.
     */
    public double playGame(AbstractPlayer player, int randomSeed, boolean isHuman)
    {
        //Prepare some structures and references for this game.
        prepareGame(player, randomSeed);

        //Create and initialize the panel for the graphics.
        VGDLViewer view = new VGDLViewer(this, player);
        JEasyFrame frame;
        frame = new JEasyFrame(view, "Java-VGDL");
        frame.addKeyListener(ki);
        frame.addWindowListener(wi);
        wi.windowClosed = false;

        //Determine the delay for playing with a good fps.
        double delay = CompetitionParameters.LONG_DELAY;
        if(player instanceof controllers.human.Agent)
            delay = 1000.0/CompetitionParameters.DELAY; //in milliseconds

        boolean firstRun = true;

        //Play until the game is ended
        while(!isEnded && !wi.windowClosed)
        {	
            //Determine the time to adjust framerate.
            long then = System.currentTimeMillis();

            this.gameCycle(); //Execute a game cycle.

            //Get the remaining time to keep fps.
            long now = System.currentTimeMillis();
            int remaining = (int) Math.max(0, delay - (now-then));

            //Wait until de next cycle.
            waitStep(remaining);

            //Draw all sprites in the panel.
            view.paint(this.spriteGroups);

            //Update the frame title to reflect current score and tick.
            this.setTitle(frame);
            
            if(firstRun && isHuman){
            	if(CompetitionParameters.dialogBoxOnStartAndEnd){
            		JOptionPane.showMessageDialog(frame, 
            				"Click OK to start.");
            	}
            	
            	firstRun = false;
            }
        }
        
        if(isHuman && !wi.windowClosed && CompetitionParameters.killWindowOnEnd){
        	if(CompetitionParameters.dialogBoxOnStartAndEnd){
        		JOptionPane.showMessageDialog(frame,
        				"GAMEOVER: YOU " + (winner == Types.WINNER.PLAYER_WINS? "WIN.": "LOSE."));
        	}
        	frame.dispose();
        }

        //Update the forward model for the game state sent to the controller.
        fwdModel.update(this);

        return handleResult();
    }

    /**
     * Sets the title of the game screen, depending on the game ending state.
     * @param frame The frame whose title needs to be set.
     */
    private void setTitle (JEasyFrame frame)
    {
        if(!isEnded)
            frame.setTitle("Java-VGDL: Score:" + score + ". Tick:" + this.getGameTick());
        else if(winner == Types.WINNER.PLAYER_WINS)
            frame.setTitle("Java-VGDL: Score:" + score + ". Tick:" + this.getGameTick() + " [Player WINS!]");
        else
            frame.setTitle("Java-VGDL: Score:" + score + ". Tick:" + this.getGameTick() + " [Player LOSES!]");

    }

    /**
     * Initializes some variables for the game to be played, such as
     * the game tick, sampleRandom number generator, forward model and assigns
     * the player to the avatar.
     * @param player Player that plays this game.
     * @param randomSeed sampleRandom seed for the whole game.
     */
    private void prepareGame(AbstractPlayer player, int randomSeed)
    {
        //Start tick counter.
        gameTick = -1;

        //Create the sampleRandom generator.
        random = new Random(randomSeed);

        //Initialize state observation (sets all non-volatile references).
        initForwardModel();

        //Assigns the player to the avatar of the game.
        assignPlayer(player);
    }

    /**
     * This is a standard game cycle in J-VGDL. It advances the game tick,
     * updates the forward model and rolls an action in all entities, handling
     * collisions and end game situations.
     */
    private void gameCycle()
    {
        gameTick++; //next game tick.

        //Update our state observation (forward model) with the information of the current game state.
        fwdModel.update(this);

        //Execute a game cycle:
        this.tick();                    //update for all entities.
        this.eventHandling();           //handle events such collisions.
        this.clearAll(fwdModel);        //clear all additional data, including dead sprites.
        this.terminationHandling();     //check for game termination.
        this.checkTimeOut();            //Check for end of game by time steps.

        //if(gameTick == 0 || isEnded)
        //    fwdModel.printObservationGrid(); //uncomment this to show the observation grid.
    }

    /**
     * Handles the result for the game, considering disqualifications. Prints the result
     * (score, time and winner) and returns the score of the game.
     * @return the result of the game.
     */
    public double handleResult()
    {
        //If the player got disqualified, set it here.
        if(disqualified){
            winner = Types.WINNER.PLAYER_DISQ;
            score = Types.SCORE_DISQ;
        }

        //For sanity: winning a game always gives a positive score
        if(winner == Types.WINNER.PLAYER_WINS)
            if(score <= 0) score = 1;

        //Prints the result: score, time and winner.
        printResult();

        return score;
    }

    /**
     * Checks if the game must finish because of number of cycles played. This is
     * a value stored in CompetitionParameters.MAX_TIMESTEPS. If the game is due to
     * end, the winner is determined and the flag isEnded is set to true.
     */
    protected void checkTimeOut()
    {
        if(gameTick >= CompetitionParameters.MAX_TIMESTEPS)
        {
            isEnded = true;
            if(winner != Types.WINNER.PLAYER_WINS)
                winner = Types.WINNER.PLAYER_LOSES;
        }
    }

    /**
     * Prints the result of the game, indicating the winner, the score and the
     * number of game ticks played, in this order.
     */
    private void printResult()
    {
        System.out.println("Result (1->win; 0->lose):"+ winner.key() + ", Score:" + score + ", timesteps:" + this.getGameTick());
    }

    /**
     * Disqualifies the player in the game, and also sets the isEnded flag to true.
     */
    public void disqualify()
    {
        disqualified = true;
        isEnded = true;
    }

    /**
     * Looks for the avatar of the game in the existing sprites. If the player
     * received as a parameter is not null, it is assigned to it.
     * @param player the player that will play the game.
     */
    private void assignPlayer(AbstractPlayer player )
    {
        //Avatar will usually be the first element, starting from the end.
        int idx = spriteOrder.length-1;
        while(avatar == null)
        {
            int spriteTypeId = spriteOrder[idx];
            if(spriteGroups[spriteTypeId].numSprites() > 0)
            {
                //There should be just one sprite in the avatar's group.
                VGDLSprite thisSprite = spriteGroups[spriteTypeId].getFirstSprite();
                if(thisSprite.is_avatar)
                    avatar = (MovingAvatar) thisSprite;
                else idx--;
            }else idx--;
        }

        if(player != null){
            avatar.player = player;
        }
    }

    /**
     * Holds the game for the specified duration milliseconds
     * @param duration time to wait.
     */
    void waitStep(int duration) {

       try
        {
            Thread.sleep(duration);
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Performs one tick for the game: calling update(this) in all sprites. It follows the
     * opposite order of the drawing order (inverse spriteOrder[]). Avatar is always
     * updated first.
     */
    protected void tick()
    {
        //Now, do the avatar.
        avatar.preMovement();
        avatar.update(this);
        //random = new Random(this.gameTick * 100); //uncomment this for testing a new rnd generator after avatar's move

        int spriteOrderCount = spriteOrder.length;
        for(int i = spriteOrderCount-1; i >= 0 ; --i)
        {
            int spriteTypeInt = spriteOrder[i];
            Integer[] keys = spriteGroups[spriteTypeInt].getKeys();

            if(keys!=null) for(Integer spriteKey : keys)
            {
                VGDLSprite sp = spriteGroups[spriteTypeInt].getSprite(spriteKey);
                if(sp != avatar)
                {
                    sp.preMovement();
                    sp.update(this);
                }

            }
        }

    }

    /**
     * Handles collisions and triggers events.
     */
    protected void eventHandling()
    {
        //Array to indicate that the sprite type has no representative in collisions.
        boolean noSprites[] = new boolean[spriteGroups.length];

        //First, check the effects that are triggered in a timely manner.
        while (timeEffects.size() > 0 && timeEffects.first().nextExecution <= gameTick)
        {
            TimeEffect ef = timeEffects.pollFirst();
            int intId = ef.itype;

            //if intId==-1, we have no sprite
            if(intId == -1)
            {
                //With no sprite, the effect is independent from particular sprites.
                ef.execute(null,null,this);

                //Affect score:
                if(ef.applyScore)
                    this.score += ef.scoreChange;

            }else {

                if (!noSprites[intId] && bucketList[intId].size() == 0) {
                    //Take all the subtypes in the hierarchy of this sprite.
                    ArrayList<Integer> allTypes = iSubTypes[intId];
                    for (Integer itype : allTypes) {
                        //Add all sprites of this subtype to the list of sprites.
                        //This are sprites that could potentially collide with EOS
                        Collection<VGDLSprite> sprites = this.getSprites(itype).values();
                        for (VGDLSprite sp : sprites) {
                            //bucketList[intId].insert(sp);
                            bucketList[intId].add(sp);
                        }
                    }

                    //If no sprites were added here, mark it in the array.
                    if (bucketList[intId].size() == 0)
                        noSprites[intId] = true;
                }

                //For all sprites that can collide.
                for (VGDLSprite s1 : bucketList[intId].getAllSprites()) {
                    //Check that they are not dead (could happen in this same cycle).
                    if (!kill_list.contains(s1)) {
                        executeEffect(ef, s1, null);
                    }
                }

                //Clear the array of sprites for this effect.
                bucketList[intId].clear();
                noSprites[intId] = false;
            }

            //If the time effect is repetitive, need to reinsert in the list of effects
            if(ef.repeating)
            {
                this.addTimeEffect(ef);
            }

        }


        //Secondly, we handle single sprite events (EOS). Take each sprite itype that has
        //a EOS effect defined.
        for(Integer intId : definedEOSEffects)
        {
            //For each effect that this sprite has assigned.
            for(Effect ef : eosEffects[intId])
            {
                if(!noSprites[intId] && bucketList[intId].size() == 0)
                {
                    //Take all the subtypes in the hierarchy of this sprite.
                    ArrayList<Integer> allTypes = iSubTypes[intId];
                    for(Integer itype : allTypes)
                    {
                        //Add all sprites of this subtype to the list of sprites.
                        //This are sprites that could potentially collide with EOS
                        Collection<VGDLSprite> sprites = this.getSprites(itype).values();
                        for(VGDLSprite sp : sprites)
                        {
                            //bucketList[intId].insert(sp);
                            bucketList[intId].add(sp);
                        }
                    }

                    //If no sprites were added here, mark it in the array.
                    if(bucketList[intId].size() == 0)
                        noSprites[intId] = true;
                }

                //For all sprites that can collide.
                for(VGDLSprite s1 : bucketList[intId].getAllSprites())
                {
                    //Check if they are at the edge to trigger the effect. Also check that they
                    //are not dead (could happen in this same cycle).
                    if(isAtEdge(s1.rect) && !kill_list.contains(s1)) {
                        executeEffect(ef, s1, null);
                    }
                }

                //Clear the array of sprites for this effect.
                bucketList[intId].clear();
                noSprites[intId] = false;
            }

        }



        // Now, we handle events between pairs of sprites, for each pair of sprites that
        // has a paired effect defined:
        for(Pair<Integer,Integer> p : definedEffects)
        {
            // We iterate over the (potential) multiple effects that these
            // two sprites could have defined between them.
            for(Effect ef : collisionEffects[p.first][p.second])
            {

                if(shieldedEffects[p.first].size() > 0) {
                    if (shieldedEffects[p.first].contains(new Pair(p.second, ef.hashCode)))
                        continue;
                }


                for (int i = 0; i < bucketList.length; i++) {
                    bucketList[i].clear();
                    noSprites[i] = false;
                }

                // Consider the two types to populate the array bucketList, that encloses the sprites
                // of both types that could take part in any interaction.
                for(int intId : new int[]{p.first, p.second})
                {
                    if(!noSprites[intId] && bucketList[intId].size() == 0)
                    {
                        //Take all the subtypes in the hierarchy of this sprite.
                        ArrayList<Integer> allTypes = iSubTypes[intId];
                        for(Integer itype : allTypes)
                        {
                            //Add all sprites of this subtype to the list of sprites
                            Collection<VGDLSprite> sprites = this.getSprites(itype).values();
                            for(VGDLSprite sp : sprites)
                            {
                                bucketList[intId].add(sp);
                            }
                        }

                        //If no sprites were added here, mark it in the array.
                        if(bucketList[intId].size() == 0)
                            noSprites[intId] = true;
                    }
                }

                //Take the collections of sprites, one for each type, of the two sprite types of this effect.
                TreeMap<Integer, ArrayList<VGDLSprite>> first = bucketList[p.first].getSpriteList();
                TreeMap<Integer, ArrayList<VGDLSprite>> second = bucketList[p.second].getSpriteList();

                if(first.size() == 0 || second.size() == 0)
                    break;

                for(Integer bucket1 : first.keySet())
                {
                    //For each bucket with sprites of this collision pair, get the sprites of p.first.
                    ArrayList<VGDLSprite> sprites1nBucket1 = first.get(bucket1);

                    //For every sprite:
                    if(sprites1nBucket1!=null)
                        for(VGDLSprite s1 : sprites1nBucket1)
                        {
                            //Decide in what buckets to look.
                            int[] buckets;
                            if(s1.bucketSharp)  buckets = new int[]{s1.bucket-1, s1.bucket};
                            else                buckets = new int[]{s1.bucket, s1.bucket+1};


                            for(int bucketId : buckets)
                            {
                                //On each bucket, take the sprites if the p.second sprite type.
                                ArrayList<VGDLSprite> spritesInBucket2 = second.get(bucketId);
                                if(spritesInBucket2 != null && !kill_list.contains(s1))
                                {
                                    int numSprites2 = spritesInBucket2.size();
                                    s2loop: for(int idx2 = 0; idx2 < numSprites2; idx2++)
                                    {
                                        //Take each sprite of p.second and check for collision
                                        VGDLSprite s2 = spritesInBucket2.get(idx2);
                                        if(s1 != s2 && s1.rect.intersects(s2.rect))
                                        {
                                            executeEffect(ef, s1, s2);

                                            if(kill_list.contains(s1))
                                                break s2loop; //Stop checking sprite 1 if it was killed.

                                        }

                                    } //end FOR sprites s2.

                                }

                            } //end FOR buckets p.second.

                        }//end FOR sprites s1

                }//end FOR buckets p.first

            }//end FOR each effect registered between p.first and p.second

        }//end FOR all effects in game.

    }

    private void executeEffect(Effect ef, VGDLSprite s1, VGDLSprite s2)
    {
        //There is a collision. Apply the effect.
        ef.execute(s1,s2,this);

        //Affect score:
        if(ef.applyScore)
            this.score += ef.scoreChange;

        //Add to events history.
        if(s1 != null && s2 != null)
            addEvent(s1, s2);
    }

    private void addEvent(VGDLSprite s1, VGDLSprite s2)
    {
        if(s1.is_avatar)
            historicEvents.add(new Event(gameTick, false, s1.getType(), s2.getType(),
                                         s1.spriteID, s2.spriteID, s1.getPosition()));

        else if(s1.is_from_avatar)
            historicEvents.add(new Event(gameTick, true, s1.getType(), s2.getType(),
                                         s1.spriteID, s2.spriteID, s1.getPosition()));

        else if(s2.is_avatar)
            historicEvents.add(new Event(gameTick, false, s2.getType(), s1.getType(),
                                         s2.spriteID, s1.spriteID, s2.getPosition()));

        else if(s2.is_from_avatar)
            historicEvents.add(new Event(gameTick, true, s2.getType(), s1.getType(),
                                         s2.spriteID, s1.spriteID, s2.getPosition()));
    }

    /**
     * Checks if a given rectangle is at the edge of the screen.
     * @param rect the rectangle to check
     * @return true if rect is at the edge of the screen.
     */
    private boolean isAtEdge(Rectangle rect)
    {
        Rectangle r = new Rectangle(screenSize);
        if(!r.contains(rect))
        {
            return true;
        }
        return false;
    }

    /**
     * Handles termination conditions, for every termination defined in 'terminations' array.
     */
    protected void terminationHandling()
    {
        int numTerminations = terminations.size();
        for(int i = 0; !isEnded && i < numTerminations; ++i)
        {
            Termination t = terminations.get(i);
            if(t.isDone(this))
            {
                isEnded = true;
                winner = t.win? Types.WINNER.PLAYER_WINS : Types.WINNER.PLAYER_LOSES;
            }
        }
    }

    /**
     * Deletes all the sprites killed in the previous step. Also, clears the array of collisions
     * from the last step.
     * @param fm Forward model where we are cleaning sprites.
     */
    protected void clearAll(ForwardModel fm)
    {
        for(VGDLSprite sprite : kill_list)
        {
            int spriteType = sprite.getType();
            this.spriteGroups[spriteType].removeSprite(sprite.spriteID);
            if(fm != null) {
                fm.removeSpriteObservation(sprite);
            }


            if(sprite.is_avatar && sprite == this.avatar)
                this.avatar = null;

            num_sprites--;

        }
        kill_list.clear();

        for(int j = 0; j < spriteGroups.length; ++j)
        {
            bucketList[j].clear();
        }

        resetShieldEffects();
    }

    /**
     * Cleans the array of shielded effects.
     */
    private void resetShieldEffects()
    {
        for(int i =0; i < shieldedEffects.length; ++i)
            shieldedEffects[i].clear();
    }

    /**
     * Adds a new Shield effect to the scene.
     * @param type1 Recipient of the effect (sprite ID)
     * @param type2 Second sprite ID
     * @param functHash Hash of the effect name to shield.
     */
    public void addShield(int type1, int type2, long functHash)
    {
        Pair newShield = new Pair(type2, functHash);
        shieldedEffects[type1].add(newShield);
    }

    /**
     * Adds a sprite given a content and position.
     * @param itype integer that identifies the definition of the sprite to add
     * @param position where the sprite has to be placed.
     */
    public VGDLSprite addSprite(int itype, Vector2d position)
    {
        return this.addSprite((SpriteContent) classConst[itype], position, itype, false);
    }

    /**
     * Adds a sprite given a content and position.
     * @param itype integer that identifies the definition of the sprite to add
     * @param position where the sprite has to be placed.
     * @param force if true, ignores the singleton restrictions and creates it anyway.
     */
    public VGDLSprite addSprite(int itype, Vector2d position, boolean force)
    {
        return this.addSprite((SpriteContent) classConst[itype], position, itype, force);
    }

    /**
     * Adds a sprite given a content and position. It checks for possible singletons.
     * @param content definition of the sprite to add
     * @param position where the sprite has to be placed.
     * @param itype integer identifier of this type of sprite.
     * @param force If true, forces the creation ignoring singleton restrictions
     */
    public VGDLSprite addSprite(SpriteContent content, Vector2d position, int itype, boolean force)
    {
        if(num_sprites > MAX_SPRITES)
        {
            System.out.println("Sprite limit reached");
            return null;
        }

        //Check for singleton Sprites
        boolean anyother = false;
        if(!force) {

            for (Integer typeInt : content.itypes) {
                //If this type is a singleton and we have one already
                if (singletons[typeInt] && getNumSprites(typeInt) > 0) {
                    //that's it, no more creations of this type.
                    anyother = true;
                    break;
                }
            }
        }

        //Only create the sprite if there is not any other sprite that blocks it.
        if(!anyother)
        {
            VGDLSprite newSprite = VGDLFactory.GetInstance().createSprite(
                    content , position, new Dimension(block_size, block_size));

            //Assign its types and add it to the collection of sprites.
            newSprite.itypes = (ArrayList<Integer>) content.itypes.clone();
            this.addSprite(newSprite, itype);
            return newSprite;
        }

        return null;
    }


    public void _updateCollisionDict(VGDLSprite sprite) {}

    /**
     * Reverses the direction of a given sprite.
     * @param sprite sprite to reverse.
     */
    public void reverseDirection(VGDLSprite sprite)
    {
        sprite.orientation = new Vector2d(-sprite.orientation.x, -sprite.orientation.y);
    }

    /**
     * Kills a given sprite, adding it to the list of sprites killed at this step.
     * @param sprite the sprite to kill.
     */
    public void killSprite(VGDLSprite sprite)
    {
        kill_list.add(sprite);
    }

    /**
     * Gets an iterator for the collection of sprites for a particular sprite type.
     * @param spriteItype type of the sprite to retrieve.
     * @return sprite collection of the specified type.
     */
    public Iterator<VGDLSprite> getSpriteGroup(int spriteItype)
    {
        return spriteGroups[spriteItype].getSpriteIterator();
    }

    /**
     * Gets an iterator for the collection of sprites for a particular sprite type, AND all subtypes.
     * @param spriteItype type of the sprite to retrieve.
     * @return sprite collection of the specified type and subtypes.
     */
    public Iterator<VGDLSprite> getSubSpritesGroup(int spriteItype)
    {
        //Create a sprite group for all the sprites
        SpriteGroup allSprites = new SpriteGroup(spriteItype);
        //Get all the subtypes
        ArrayList<Integer> allTypes = iSubTypes[spriteItype];

        //Add sprites of this type, and all subtypes.
        allSprites.addAllSprites(this.getSprites(spriteItype).values());
        for(Integer itype : allTypes)
        {
            allSprites.addAllSprites(this.getSprites(itype).values());
        }

        //Return the iterator.
        return allSprites.getSpriteIterator();
    }

    /**
     * Gets the collection of sprites for a particular sprite type.
     * @param spriteItype type of the sprite to retrieve.
     * @return sprite collection of the specified type.
     */
    public ConcurrentHashMap<Integer, VGDLSprite> getSprites(int spriteItype)
    {
        return spriteGroups[spriteItype].getSprites();
    }

    /**
     * Gets the array of collisions defined for two types of sprites.
     * @param spriteItype1 type of the first sprite.
     * @param spriteItype2 type of the second sprite.
     * @return the collection of the effects defined between the two sprite types.
     */
    public ArrayList<Effect> getCollisionEffects(int spriteItype1, int spriteItype2)
    {
        return collisionEffects[spriteItype1][spriteItype2];
    }

    /**
     * Returns all paired effects defined in the game.
     * @return all paired effects defined in the game.
     */
    public ArrayList<Pair<Integer,Integer>> getDefinedEffects()
    {
        return definedEffects;
    }

    /**
     * Returns the list of sprite type with at least one EOS effect defined.
     * @return the list of sprite type with at least one EOS effect defined.
     */
    public ArrayList<Integer> getDefinedEosEffects()
    {
        return definedEOSEffects;
    }

    /**
     * Returns all EOS effects defined in the game.
     * @return all EOS effects defined in the game.
     */
    public ArrayList<Effect> getEosEffects(int obj1)
    {
        return eosEffects[obj1];
    }

    /**
     * Adds a time effect to the game.
     */
    public void addTimeEffect(TimeEffect ef)
    {
        timeEffects.add(ef);
    }

    /**
     * Returns the char mapping of this array, that relates characters in the level with
     * sprite names that it references.
     * @return the char mapping of this array. For each character, there is a list of N sprite names.
     */
    public HashMap<Character,ArrayList<String>> getCharMapping()
    {
        return charMapping;
    }
    
    /**
     * Set the char mapping that is used to parse loaded levels
     * @param charMapping	new character mapping
     */
    public void setCharMapping(HashMap<Character, ArrayList<String>> charMapping){
    	this.charMapping = charMapping;
    }

    /**
     * Gets the array of termination conditions for this game.
     * @return the array of termination conditions.
     */
    public ArrayList<Termination> getTerminations()
    {
        return terminations;
    }

    /**
     * Gets the maximum amount of resources of type resourceId that are allowed by entities in the game.
     * @param resourceId the id of the resource to query for.
     * @return maximum amount of resources of type resourceId.
     */
    public int getResourceLimit(int resourceId)
    {
        return resources_limits[resourceId];
    }

    /**
     * Gets the color of the resource of type resourceId
     * @param resourceId id of the resource to query for.
     * @return Color assigned to this resource.
     */
    public Color getResourceColor(int resourceId)
    {
        return resources_colors[resourceId];
    }

    /**
     * Gets the dimensions of the screen.
     * @return the dimensions of the screen.
     */
    public Dimension getScreenSize() {return screenSize;}

    /**
     * Defines this game as stochastic (or not) depending on the parameter passed.
     * @param stoch true if the game is stochastic.
     */
    public void setStochastic(boolean stoch) {is_stochastic = stoch;}

    /**
     * Returns the avatar of the game.
     * @return the avatar of the game.
     */
    public MovingAvatar getAvatar() {return avatar;}

    /**
     * Sets the avatar of the game.
     * @param newAvatar the avatar of the game.
     */
    public void setAvatar(MovingAvatar newAvatar) {avatar = newAvatar;}

    /**
     * Sets the last action executed by the avatar. It could be NIL in case of time overspent.
     * @param action the action to set.
     */
    public void setAvatarLastAction(Types.ACTIONS action)
    {
        this.avatarLastAction = action;
    }

    /**
     * Indicates if the game is over, or if it is still being played.
     * @return true if the game is over, false if it is still being played.
     */
    public boolean isGameOver()
    {
        return this.winner != Types.WINNER.NO_WINNER;
    }

    /**
     * Retuns the observation of this state.
     * @return the observation.
     */
    public StateObservation getObservation()
    {
        return new StateObservation(fwdModel);
    }

    /**
     * Returns the sampleRandom object
     * @return the sampleRandom generator.
     */
    public Random getRandomGenerator()
    {
        return random;
    }


    /**
     * Returns the current game tick of this game.
     * @return the current game tick of this game.
     */
    public int getGameTick() {return gameTick;}

    /**
     * Returns the winner of this game. A value from Types.WINNER.
     * @return the winner of this game.
     */
    public Types.WINNER getWinner() {return winner;}


    /**
     * Gets the order in which the sprites are drawn.
     * @return the order of the sprites.
     */
    public int[] getSpriteOrder() {return spriteOrder;}


    /**
     * Indicates how many pixels form a block in the game.
     * @return how many pixels form a block in the game.
     */
    public int getBlockSize()
    {
        return block_size;
    }

    public abstract void buildStringLevel(String[] levelString);
    
    /**
     * Builds a level, receiving a file name.
     * @param gamelvl file name containing the level.
     */
    public void buildLevel(String gamelvl){}


    public ArrayList<Node> getPath(Vector2d start, Vector2d end)
    {
        Vector2d pathStart = new Vector2d(start);
        Vector2d pathEnd = new Vector2d(end);

        pathStart.mul(1.0 / (double) block_size);
        pathEnd.mul(1.0/(double)block_size);

        return pathf.getPath(pathStart, pathEnd);
    }
    
    /**
     * Class for helping collision detection.
     */
    protected class Bucket
    {
        ArrayList<VGDLSprite> allSprites;
        TreeMap<Integer, ArrayList<VGDLSprite>> spriteLists;
        int totalNumSprites;

        public Bucket()
        {
            allSprites = new ArrayList<VGDLSprite>();
            spriteLists = new TreeMap<Integer, ArrayList<VGDLSprite>>();
            totalNumSprites = 0;
        }

        public void clear()
        {
            allSprites.clear();
            spriteLists.clear();
            totalNumSprites = 0;
        }

        public void add(VGDLSprite sp)
        {
            int bucket = sp.bucket;
            ArrayList<VGDLSprite> sprites = spriteLists.get(bucket);
            if(sprites == null)
            {
                sprites = new ArrayList<VGDLSprite>();
                spriteLists.put(bucket, sprites);
            }
            sprites.add(sp);
            allSprites.add(sp);
            totalNumSprites++;
        }

        public int size()
        {
            return totalNumSprites;
        }

        public int size(int bucket)
        {
            ArrayList<VGDLSprite> sprites = spriteLists.get(bucket);
            if(sprites == null)
                return 0;
            return sprites.size();
        }

        public ArrayList<VGDLSprite> getAllSprites()
        {
            return allSprites;
        }

        public TreeMap<Integer, ArrayList<VGDLSprite>> getSpriteList()
        {
            return spriteLists;
        }

    }

}
