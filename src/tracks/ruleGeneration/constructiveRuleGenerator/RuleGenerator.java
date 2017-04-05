package tracks.ruleGeneration.constructiveRuleGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import core.game.GameDescription.SpriteData;
import core.game.SLDescription;
import core.generator.AbstractRuleGenerator;
import tools.ElapsedCpuTimer;
import tools.LevelAnalyzer;

/**
 * This is a constructive rule generator it depends 
 * @author AhmedKhalifa
 */
public class RuleGenerator extends AbstractRuleGenerator{
	/**
	 * a Level Analyzer object used to analyze the game sprites
	 */
	private LevelAnalyzer la;

	/**
	 * array of different interactions that movable objects (contains also npcs) can do when hitting the walls
	 */
	private String[] movableWallInteraction = new String[]{"stepBack", "flipDirection", "reverseDirection",
			"turnAround", "wrapAround"};

	/**
	 * percentages used to decide
	 */
	private double wallPercentageProb = 0.5;
	private double spikeProb = 0.5;
	private double doubleNPCsProb = 0.5;
	private double harmfulMovableProb = 0.5;
	private double firewallProb = 0.1;
	private double scoreSpikeProb = 0.1;
	private double randomNPCProb = 0.5;
	private double spawnedProb = 0.5;
	private double bomberProb = 0.5;

	/**
	 * a list of suggested interactions for the generated game
	 */
	private ArrayList<String> interactions;
	/**
	 * a list of suggested temination conditions for the generated game
	 */
	private ArrayList<String> terminations;

	/**
	 * the sprite that the generator think is a wall sprite
	 */
	private SpriteData wall;
	/**
	 * array of all door sprites
	 */
	private ArrayList<SpriteData> exit;
	/**
	 * array of all collectible sprites
	 */
	private ArrayList<String> collectible;
	/**
	 * a certain unmovable object that is used as a collectible object
	 */
	private SpriteData score;
	/**
	 * a certain unmovable object that is used as a spike object
	 */
	private SpriteData spike;

	/**
	 * random object used in generating different games
	 */
	private Random random;

	/**
	 * Array of all different types of harmful objects (can kill the player)
	 */
	private ArrayList<String> harmfulObjects;
	/**
	 * Array of all different types of fleeing NPCs
	 */
	private ArrayList<String> fleeingNPCs;

	/**
	 * Constructor that initialize the constructive algorithm
	 * @param sl	SLDescription object contains information about the
	 * 			current game and level
	 * @param time	the amount of time allowed for initialization
	 */
	public RuleGenerator(SLDescription sl, ElapsedCpuTimer time){
		//Initialize everything
		la = new LevelAnalyzer(sl);

		interactions = new ArrayList<String>();
		terminations = new ArrayList<String>();

		random = new Random();
		harmfulObjects = new ArrayList<String>();
		fleeingNPCs = new ArrayList<String>();
		collectible = new ArrayList<String>();

		//Identify the wall object
		wall = null;
		SpriteData[] temp = la.getBorderObjects((1.0 * la.getPerimeter()) / la.getArea(), this.wallPercentageProb);
		if(temp.length > 0){
			wall = temp[0];
			for(int i=0; i<temp.length; i++){
				if(la.getNumberOfObjects(temp[i].name) < la.getNumberOfObjects(wall.name)){
					wall = temp[i];
				}
			}
		}

		//identify the exit sprite
		exit = new ArrayList<SpriteData>();
		temp = la.getPortals(true);
		for(int i=0; i<temp.length; i++){
			if(!temp[i].type.equalsIgnoreCase("portal")){
				exit.add(temp[i]);
			}
		}

		//identify the score and spike sprites
		ArrayList<SpriteData> tempList = new ArrayList<SpriteData>();
		score = null;
		spike = null;
		temp = la.getImmovables(1, (int)(scoreSpikeProb * la.getArea()));
		if (temp.length > 0) {
			if (wall == null) {
				score = temp[random.nextInt(temp.length)];
				spike = temp[random.nextInt(temp.length)];
			}
			else {
				tempList = new ArrayList<SpriteData>();
				SpriteData[] relatedSprites = la.getSpritesOnSameTile(wall.name);
				for (int i = 0; i < temp.length; i++) {
					for (int j = 0; j < relatedSprites.length; j++) {
						if (!temp[i].name.equals(relatedSprites[j].name)) {
							tempList.add(temp[i]);
						}
					}
					if(relatedSprites.length == 0){
						tempList.add(temp[i]);
					}
				}

				score = tempList.get(random.nextInt(tempList.size()));
				spike = tempList.get(random.nextInt(tempList.size()));
			}
		}
	}

	/**
	 * Check if this spritename is the avatar
	 * @param spriteName	the input sprite name
	 * @return			true if its the avatar or false otherwise
	 */
	private boolean isAvatar(String spriteName){
		SpriteData[] avatar = la.getAvatars(false);
		for(int i=0; i<avatar.length; i++){
			if(avatar[i].equals(spriteName)){
				return true;
			}
		}
		return false;
	}

	/**
	 * get the interactions of everything with wall sprites
	 */
	private void getWallInteractions(){
		String wallName = "EOS";
		if (wall != null) {
			wallName = wall.name;
		}
		//Is walls acts like fire (harmful for everyone)
		boolean isFireWall = this.random.nextDouble() < firewallProb &&
				wall != null && fleeingNPCs.size() == 0;

		//Avatar interaction with wall or EOS
		String action = "stepBack";
		if(isFireWall){
			action = "killSprite";
		}
		SpriteData[] temp = la.getAvatars(false);
		for (int i = 0; i < temp.length; i++) {
			interactions.add(temp[i].name + " " + wallName + " > " + action);
		}

		//Get the interaction between all movable objects (including npcs) with wall or EOS
		action = movableWallInteraction[random.nextInt(movableWallInteraction.length)];
		if(isFireWall){
			action = "killSprite";
		}
		temp = la.getMovables(false);
		for (int i = 0; i < temp.length; i++) {
			interactions.add(temp[i].name + " " + wallName + " > " + action);
		}
		action = movableWallInteraction[random.nextInt(movableWallInteraction.length)];
		if(isFireWall){
			action = "killSprite";
		}
		temp = la.getNPCs(false);
		for (int i = 0; i < temp.length; i++) {
			interactions.add(temp[i].name + " " + wallName + " > " + action);
		}
	}

	/**
	 * get the interactions of all sprites with resource sprites
	 */
	private void getResourceInteractions(){
		SpriteData[] avatar = la.getAvatars(false);
		SpriteData[] resources = la.getResources(true);

		//make the avatar collect the resources
		for(int i=0; i<avatar.length; i++){
			for(int j=0; j<resources.length; j++){
				interactions.add(resources[j].name + " " + avatar[i].name + " > collectResource");
			}
		}
	}

	/**
	 * get the interactions of all sprites with spawner sprites
	 */
	private void getSpawnerInteractions(){
		SpriteData[] avatar = la.getAvatars(false);
		SpriteData[] spawners = la.getSpawners(true);

		//make the spawned object harmful to the avatar with a chance to be useful
		if(random.nextDouble() < spawnedProb){
			for (int i = 0; i < avatar.length; i++) {
				for (int j = 0; j < spawners.length; j++) {
					for (int k = 0; k < spawners[j].sprites.size(); k++) {
						harmfulObjects.add(spawners[j].sprites.get(k));
						interactions.add(avatar[i].name + " " + spawners[j].sprites.get(k) + " > killSprite");
					}
				}
			}
		}
		else{
			for (int i = 0; i < avatar.length; i++) {
				for (int j = 0; j < spawners.length; j++) {
					for (int k = 0; k < spawners[j].sprites.size(); k++) {
					    if(!harmfulObjects.contains(spawners[j].sprites.get(k))){
						collectible.add(spawners[j].sprites.get(k));
						interactions.add(spawners[j].sprites.get(k) + " " + avatar[i].name + " > killSprite scoreChange=1");
					    }
					}
				}
			}
		}
		
		for (int j = 0; j < spawners.length; j++) {
		    for (int k = 0; k < spawners[j].sprites.size(); k++) {
			if(harmfulObjects.contains(spawners[j].sprites.get(k))){
			    harmfulObjects.add(spawners[j].name);
			    break;
			}
		    }
		}
		
		for (int j = 0; j < spawners.length; j++) {
		    for (int k = 0; k < spawners[j].sprites.size(); k++) {
			if(collectible.contains(spawners[j].sprites.get(k))){
			    collectible.add(spawners[j].name);
			    break;
			}
		    }
		}
	}

	/**
	 * get the interactions of all sprites with immovable sprites
	 */
	private void getImmovableInteractions(){
		SpriteData[] avatar = la.getAvatars(false);

		//If we have a score object make the avatar can collect it
		if(score != null){
			for(int i=0; i<avatar.length; i++){
				collectible.add(score.name);
				interactions.add(score.name + " " + avatar[i].name + " > killSprite scoreChange=1");
			}
		}

		//If we have a spike object make it kill the avatar with a change to be a super collectible sprite
		if (spike != null && !spike.name.equalsIgnoreCase(score.name)) {
			if (random.nextDouble() < spikeProb) {
				harmfulObjects.add(spike.name);
				for (int i = 0; i < avatar.length; i++) {
					interactions.add(avatar[i].name + " " + spike.name + " > killSprite");
				}
			}
			else {
				for (int i = 0; i < avatar.length; i++) {
					collectible.add(spike.name);
					interactions.add(spike.name + " " + avatar[i].name + " > killSprite scoreChange=2");
				}
			}
		}
	}

	/**
	 * get the interactions of all sprites with avatar sprites
	 */
	private void getAvatarInteractions(){
		SpriteData[] avatar = la.getAvatars(false);

		//Kill the avatar bullet kill any harmful objects
		for(int i=0; i<avatar.length; i++){
			for (int j = 0; j < harmfulObjects.size(); j++) {
				for (int k = 0; k < avatar[i].sprites.size(); k++) {
					interactions.add(harmfulObjects.get(j) + " " + avatar[i].sprites.get(k) + " > killSprite scoreChange=1");
					interactions.add(avatar[i].sprites.get(k) + " " + harmfulObjects.get(j) + " > killSprite");
				}
			}
		}
	}

	/**
	 * get the interactions of all sprites with portal sprites
	 */
	private void getPortalInteractions() {
		SpriteData[] avatar = la.getAvatars(false);
		SpriteData[] portals = la.getPortals(true);

		//make the exits die with collision of the player (going through them)
		for (int i = 0; i < avatar.length; i++) {
			for (int j = 0; j < exit.size(); j++) {
				interactions.add(exit.get(j).name + " " + avatar[i].name + " > killSprite");
			}
		}
		//If they are Portal type then u can teleport toward it
		for (int i = 0; i < portals.length; i++) {
			for (int j = 0; j < avatar.length; j++) {
				if (portals[i].type.equalsIgnoreCase("Portal")) {
					interactions.add(avatar[j].name + " " + portals[i].name + " > teleportToExit");
				}
			}
		}
	}

	/**
	 * get the interactions of all sprites with npc sprites
	 */
	private void getNPCInteractions(){
		SpriteData[] avatar = la.getAvatars(false);
		SpriteData[] npc = la.getNPCs(false);

		for(int i=0; i<npc.length; i++){
			//If its fleeing object make it useful
			if (npc[i].type.equalsIgnoreCase("fleeing")) {
				for(int j=0; j<npc[i].sprites.size(); j++){
					fleeingNPCs.add(npc[i].sprites.get(j));
					interactions.add(npc[i].name + " " + npc[i].sprites.get(j) + " > killSprite scoreChange=1");
				}
			}
			else if (npc[i].type.equalsIgnoreCase("bomber") || npc[i].type.equalsIgnoreCase("randombomber")) {
				//make the bomber harmful for the player
				for(int j=0; j<avatar.length; j++){
					harmfulObjects.add(npc[i].name);
					interactions.add(avatar[j].name + " " + npc[i].name + " > killSprite");
				}
				//make the spawned object harmful
				if(this.random.nextDouble() < bomberProb){
					for (int j = 0; j < npc[i].sprites.size(); j++) {
						harmfulObjects.add(npc[i].sprites.get(j));
						interactions.add(avatar[j].name + " " + npc[i].sprites.get(j) + " > killSprite");
					}
				}
				//make the spawned object useful
				else{
					for (int j = 0; j < npc[i].sprites.size(); j++) {
						interactions.add(npc[i].sprites.get(j) + " " + avatar[j].name + " > killSprite scoreChange=1");
					}
				}
			}
			else if (npc[i].type.equalsIgnoreCase("chaser") || npc[i].type.equalsIgnoreCase("AlternateChaser")
					|| npc[i].type.equalsIgnoreCase("RandomAltChaser")) {
				//make chasers harmful for the avatar
				for(int j=0; j<npc[i].sprites.size(); j++){
					if(isAvatar(npc[i].sprites.get(j))){
						for(int k=0; k<avatar.length; k++){
							harmfulObjects.add(npc[i].name);
							interactions.add(avatar[k].name + " " + npc[i].name + " > killSprite");
						}
					}
					else{
						if(random.nextDouble() < doubleNPCsProb){
							interactions.add(npc[i].sprites.get(j) + " " + npc[i].name + " > killSprite");
						}
						else{
							interactions.add(npc[i].sprites.get(j) + " " + npc[i].name + " > transformTo stype=" + npc[i].name);
						}

					}
				}
			}
			else if (npc[i].type.equalsIgnoreCase("randomnpc")) {
				//random npc are harmful to the avatar
				if(this.random.nextDouble() < randomNPCProb){
					for (int j = 0; j < avatar.length; j++) {
						harmfulObjects.add(npc[i].name);
						interactions.add(avatar[j].name + " " + npc[i].name + " > killSprite");
					}
				}
				//random npc are userful to the avatar
				else{
					for (int j = 0; j < avatar.length; j++) {
						collectible.add(npc[i].name);
						interactions.add(npc[i].name + " " + avatar[j].name + " > killSprite scoreChange=1");
					}
				}
			}
		}
	}

	/**
	 * get the interactions of all sprites with movable sprites
	 */
	private void getMovableInteractions(){
		SpriteData[] movables = la.getMovables(false);
		SpriteData[] avatar = la.getAvatars(false);
		SpriteData[] spawners = la.getSpawners(false);

		for(int j=0; j<movables.length; j++){
			//Check if the movable object is not avatar or spawned child
			boolean found = false;
			for(int i=0; i<avatar.length; i++){
				if(avatar[i].sprites.contains(movables[j].name)){
					found = true;
				}
			}
			for(int i=0; i<spawners.length; i++){
				if(spawners[i].sprites.contains(movables[j].name)){
					found = true;
				}
			}
			if(!found){
				//Either make them harmful or useful
				if(random.nextDouble() < harmfulMovableProb){
					for(int i=0; i<avatar.length; i++){
						harmfulObjects.add(movables[j].name);
						interactions.add(avatar[i].name + " " + movables[j].name + " > killSprite");
					}
				}
				else{
					for(int i=0; i<avatar.length; i++){
						collectible.add(movables[j].name);
						interactions.add(movables[j].name + " " + avatar[i].name + " > killSprite scoreChange=1");
					}
				}
			}
		}
	}

	/**
	 * get the termination condition for the generated game
	 */
	private void getTerminations(){
		//If you have a door object make it the winning condition
		if(exit.size() > 0){
			SpriteData door = null;
			for(int i=0; i<exit.size(); i++){
				if(exit.get(i).type.equalsIgnoreCase("door")){
					door = exit.get(i);
					break;
				}
			}

			if(door != null){
				terminations.add("SpriteCounter stype=" + door.name + " limit=0 win=True");
			}
			//otherwise pick any other exit object
			else if(collectible.size() > 0){
				terminations.add("SpriteCounter stype=collectible limit=0 win=True");
			}
		}
		else {
			//If we have feeling NPCs use them as winning condition
			if (fleeingNPCs.size() > 0) {
				terminations.add("SpriteCounter stype=fleeing limit=0 win=True");
			}
			else if(harmfulObjects.size() > 0 && this.la.getAvatars(true)[0].sprites.size() > 0){
				terminations.add("SpriteCounter stype=harmful limit=0 win=True");
			}
			//Otherwise use timeout as winning condition
			else {
				terminations.add("Timeout limit=" + (500 + random.nextInt(5) * 100) + " win=True");
			}
		}

		//Add the losing condition which is the player dies
		if(harmfulObjects.size() > 0){
			SpriteData[] usefulAvatar = this.la.getAvatars(true);
			for(int i=0; i<usefulAvatar.length; i++){
				terminations.add("SpriteCounter stype=" + usefulAvatar[i].name + " limit=0 win=False");
			}
		}
	}

    
    /**
     * get the generated interaction rules and termination rules
     * @param sl	SLDescription object contain information about the game
     * 			sprites and the current level
     * @param time	the amount of time allowed for the rule generator
     * @return		two arrays the first contains the interaction rules
     * 			while the second contains the termination rules
     */
    @Override
    public String[][] generateRules(SLDescription sl, ElapsedCpuTimer time) {
	this.interactions.clear();
	this.terminations.clear();
	this.collectible.clear();
	this.harmfulObjects.clear();
	this.fleeingNPCs.clear();
	
	this.getResourceInteractions();
	this.getImmovableInteractions();
	this.getNPCInteractions();
	this.getSpawnerInteractions();
	this.getPortalInteractions();
	this.getMovableInteractions();
	this.getWallInteractions();
	this.getAvatarInteractions();
	
	this.getTerminations();
	
	return new String[][]{interactions.toArray(new String[interactions.size()]), terminations.toArray(new String[terminations.size()])};
    }
    
    @Override
    public HashMap<String, ArrayList<String>> getSpriteSetStructure() {
        HashMap<String, ArrayList<String>> struct = new HashMap<String, ArrayList<String>>();
        HashMap<String, Boolean> testing = new HashMap<String, Boolean>();
        
        if(fleeingNPCs.size() > 0){
            struct.put("fleeing", new ArrayList<String>());
        }
        for(int i=0; i<this.fleeingNPCs.size(); i++){
            if(!testing.containsKey(this.fleeingNPCs.get(i))){
        	testing.put(this.fleeingNPCs.get(i), true);
        	struct.get("fleeing").add(this.fleeingNPCs.get(i));
            }
        }
        
        if(harmfulObjects.size() > 0){
            struct.put("harmful", new ArrayList<String>());
        }
        for(int i=0; i<this.harmfulObjects.size(); i++){
            if(!testing.containsKey(this.harmfulObjects.get(i))){
        	testing.put(this.harmfulObjects.get(i), true);
        	struct.get("harmful").add(this.harmfulObjects.get(i));
            }
        }
        if(collectible.size() > 0){
            struct.put("collectible", new ArrayList<String>());
        }
        for(int i=0; i<this.collectible.size(); i++){
            if(!testing.containsKey(this.collectible.get(i))){
        	testing.put(this.collectible.get(i), true);
        	struct.get("collectible").add(this.collectible.get(i));
            }
        }
        
        return struct;
    }

}
