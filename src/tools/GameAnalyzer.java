package tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import core.game.GameDescription;
import core.game.GameDescription.InteractionData;
import core.game.GameDescription.SpriteData;
import core.game.GameDescription.TerminationData;

public class GameAnalyzer {
	/**
	 * List of all different types of spawners
	 */
	public final ArrayList<String> spawnerTypes = new ArrayList<String>(Arrays.asList(new String[]{"SpawnPoint", "Bomber", "RandomBomber", "Spreader", "ShootAvatar", "FlakAvatar"}));
	/**
	 * List of all different interactions cause spawning
	 */
	public final ArrayList<String> spawnInteractions = new ArrayList<String>(Arrays.asList(new String[]{"TransformTo", "SpawnIfHasMore", "SpawnIfHasLess"}));
	/**
	 * List of all different interactions cause an object to be solid
	 */
	public final ArrayList<String> solidInteractions = new ArrayList<String>(Arrays.asList(new String[]{"StepBack", "UndoAll"}));
	/**
	 * List of all different interactions cause an object to die
	 */
	public final ArrayList<String> deathInteractions = new ArrayList<String>(Arrays.asList(new String[]{"KillSprite", "KillIfHasMore", "KillIfHasLess", "KillIfFromAbove", "KillIfOtherHasMore"}));
	/**
	 * List of all horizontal moving avatar
	 */
	public final ArrayList<String> horzAvatar = new ArrayList<String>(Arrays.asList(new String[]{"FlakAvatar", "HorizontalAvatar"}));
	/**
	 * Resource string
	 */
	public final String resource = "Resource";
	/**
	 * Termination Condition that counts number of sprite
	 */
	public final String spriteCounter = "SpriteCounter";
	/**
	 * Termination Condition that counts number of two differnt sprites
	 */
	public final String multiCounter = "MultiSpriteCounter";
	
	/**
	 * list of 1 and 0 for all game sprites
	 */
	private HashMap<String, Integer> minRequiredNumber;
	/**
	 * list of priority values for all game sprites
	 */
	private HashMap<String, Integer> priorityValue;
	/**
	 * Max scoreChange found in all interactions
	 */
	private double minScoreUnit;
	/**
	 * Min scoreChange found in all interactions
	 */
	private double maxScoreUnit;
	
	/**
	 * List of all sprites that block the avatar
	 */
	private ArrayList<String> solidSprites;
	/**
	 * List of all sprites that are defined as avatar
	 */
	private ArrayList<String> avatarSprites;
	/**
	 * List of all sprites that kill the avatar
	 */
	private ArrayList<String> harmfulSprites;
	/**
	 * List of all sprites that are collected by the avatar
	 */
	private ArrayList<String> collectableSprites;
	/**
	 * List of all sprites defined in the Termination Set
	 */
	private ArrayList<String> goalSprites;
	/**
	 * All other sprites not defined
	 */
	private ArrayList<String> otherSprites;
	
	/**
	 * Checks if the object is created by other object
	 * @param stype 		current sprite name to test
	 * @param game			game description object that describe the current game
	 * @param allSprites	list of all sprites to test
	 * @return				return true if object is created by other object
	 */
	private boolean checkIsCreate(String stype, GameDescription game, ArrayList<SpriteData> allSprites){
		for(SpriteData sprite:allSprites){
			if(spawnerTypes.contains(sprite.type) && sprite.sprites.contains(stype)){
				return true;
			}
			
			for(SpriteData sprite2:allSprites){
				ArrayList<InteractionData> data = game.getInteraction(sprite.name, sprite2.name);
				for (InteractionData d:data){
					if(spawnInteractions.contains(d.type) && d.sprites.contains(stype)){
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Calculate the minimum number for each sprite
	 * @param game	the current game description object
	 */
	private void calculateMinRequiredNumber(GameDescription game){
		ArrayList<SpriteData> allSprites = game.getAllSpriteData();
		
		for(SpriteData sprite:allSprites){
			if(!sprite.type.equals(resource) && checkIsCreate(sprite.name, game, allSprites)){
				minRequiredNumber.put(sprite.name, 0);
			}
			else{
				if(getAllInteractions(sprite.name, InteractionType.ALL, game).size() > 0 || 
						spawnerTypes.contains(sprite.type)){
					minRequiredNumber.put(sprite.name, 1);
				}
			}
			
			if(!minRequiredNumber.containsKey(sprite.name)){
				minRequiredNumber.put(sprite.name, 0);
			}
		}
	}
	
	/**
	 * calculate the priority values for all game sprites and save it in hashmap
	 * @param game	game description object for the current game
	 */
	private void calculatePriorityValues(GameDescription game){
		ArrayList<SpriteData> allSprites = game.getAllSpriteData();
		
		for(SpriteData s:allSprites){
			ArrayList<InteractionData> interactions = getAllInteractions(s.name, InteractionType.ALL, game);
			priorityValue.put(s.name, interactions.size());
		}
	}
	
	/**
	 * search for all solid sprites that blocks the avatar from moving
	 * @param game	game description object for the current game
	 */
	private void findSolidObjects(GameDescription game){
		ArrayList<SpriteData> avatars = game.getAvatar();
		ArrayList<SpriteData> staticSprites = game.getStatic();
		
		for(SpriteData sprite:avatars){
			for(SpriteData solid:staticSprites){
				boolean isSolid = true;
				ArrayList<InteractionData> interactions = game.getInteraction(sprite.name, solid.name);
				ArrayList<InteractionData> secondaryInteraction = getAllInteractions(solid.name, InteractionType.FIRST, game);
				for(InteractionData sI:secondaryInteraction){
					if(!solidInteractions.contains(sI.type)){
						isSolid = false;
						break;
					}
				}
				
				for(InteractionData i:interactions){
					if(!solidInteractions.contains(i.type)){
						isSolid = false;
						break;
					}
				}
				
				if(isSolid && interactions.size() > 0 && !solidSprites.contains(solid.name)){
					solidSprites.add(solid.name);
				}
			}
		}
	}
	
	/**
	 * get a list of all interactions for a certain game sprite
	 * @param stype	sprite required to be checked
	 * @param type	type of checked interaction (if the sprite is always on left or right or don't care)
	 * @param game	game description object for the current game
	 * @return		list of all interactions for the listed sprite name
	 */
	private ArrayList<InteractionData> getAllInteractions(String stype, InteractionType type, GameDescription game){
		ArrayList<SpriteData> allSprites = game.getAllSpriteData();
		ArrayList<InteractionData> data = new ArrayList<InteractionData>();
		
		for (SpriteData sd:allSprites){
			if(type == InteractionType.FIRST || type == InteractionType.ALL){
				data.addAll(game.getInteraction(stype, sd.name));
			}
			if(type == InteractionType.SECOND || type == InteractionType.ALL){
				data.addAll(game.getInteraction(sd.name, stype));
			}
		}
		
		return data;
	}
	
	/**
	 * get a list of all avatar sprites in the game
	 * @param game	game description object for the current game
	 */
	private void findAvatarObjects(GameDescription game){
		ArrayList<SpriteData> avatars = game.getAvatar();
		
		for(SpriteData sprite:avatars){
			if(!avatarSprites.contains(sprite.name)){
				avatarSprites.add(sprite.name);
			}
		}
	}
	
	/**
	 * find all sprites listed in the termination set
	 * @param game	game description object for the current game
	 */
	private void findGoalSprites(GameDescription game){
		ArrayList<TerminationData> terminations = game.getTerminationConditions();
		
		for(TerminationData td:terminations){
			for(String sprite:td.sprites){
				if(!goalSprites.contains(sprite)){
					goalSprites.add(sprite);
				}
			}
		}
	}
	
	/**
	 * find all sprites that can kill the avatar
	 * @param game	game description object for the current game
	 */
	private void findHarmfulSprites(GameDescription game){
		ArrayList<String> avatars = getAvatarSprites();
		ArrayList<SpriteData> allSprites = game.getAllSpriteData();
		
		for(String a:avatars){
			for(SpriteData s:allSprites){
				ArrayList<InteractionData> interactions = game.getInteraction(a, s.name);
				for(InteractionData i:interactions){
					if(deathInteractions.contains(i.type)){
						if(!harmfulSprites.contains(s.name)){
							harmfulSprites.add(s.name);
						}
					}
				}
			}
		}
		
		for(SpriteData s:allSprites){
			if(spawnerTypes.contains(s.type)){
				for(String stype:s.sprites){
					if(harmfulSprites.contains(stype) && 
							!harmfulSprites.contains(s.name)){
						harmfulSprites.add(s.name);
					}
				}
			}
		}
	}
	
	/**
	 * find all sprites that can be collected by the avatar
	 * @param game	game description object of the current game
	 */
	private void findCollectableSprites(GameDescription game){
		ArrayList<String> avatars = getAvatarSprites();
		ArrayList<SpriteData> allSprites = game.getAllSpriteData();
		
		for(String a:avatars){
			for(SpriteData s:allSprites){
				ArrayList<InteractionData> interactions = game.getInteraction(s.name, a);
				for(InteractionData i:interactions){
					if(deathInteractions.contains(i.type)){
						if(!harmfulSprites.contains(s.name) && !collectableSprites.contains(s.name)){
							collectableSprites.add(s.name);
						}
					}
				}
			}
		}
	}
	
	/**
	 * analyze the game description object and list all other sprites
	 * @param game	game description object for the current game
	 */
	private void findOtherSprites(GameDescription game){
		ArrayList<SpriteData> allSprites = game.getAllSpriteData();
		ArrayList<String> combinedLists = new ArrayList<String>();
		combinedLists.addAll(avatarSprites);
		combinedLists.addAll(harmfulSprites);
		combinedLists.addAll(solidSprites);
		combinedLists.addAll(collectableSprites);
		combinedLists.addAll(goalSprites);
		
		for(SpriteData s:allSprites){
			if(!combinedLists.contains(s.name)){
				if(!otherSprites.contains(s.name)){
					otherSprites.add(s.name);
				}
			}
		}
	}
	
	/**
	 * calculate the min and max score change in the instruction set
	 * @param game	game description object for the current game
	 */
	private void calculateMinMaxScoreUnit(GameDescription game){
		maxScoreUnit = 0;
		minScoreUnit = Integer.MAX_VALUE;
		ArrayList<SpriteData> allSprites = game.getAllSpriteData();
		
		for(SpriteData s1:allSprites){
			for(SpriteData s2:allSprites){
				ArrayList<InteractionData> interactions = game.getInteraction(s1.name, s2.name);
				for(InteractionData i:interactions){
					if(i.scoreChange > 0){
						if(i.scoreChange > maxScoreUnit){
							maxScoreUnit = i.scoreChange;
						}
						if(i.scoreChange < minScoreUnit){
							minScoreUnit = i.scoreChange;
						}
					}
				}
			}
		}
	}
	
	/**
	 * Remove all sprites that are not appearing in the level mapping
	 * @param game	game description object for the current game
	 */
	private void removeUselessObjects(GameDescription game){
		HashMap<Character, ArrayList<String>> levelMapping = game.getLevelMapping();
		ArrayList<String> allowedObjs = new ArrayList<String>();
		for(ArrayList<String> data:levelMapping.values()){
			allowedObjs.addAll(data);
		}
		
		ArrayList<String> removeObjs = new ArrayList<String>();
		for(String s:avatarSprites){
			if(!allowedObjs.contains(s)){
				removeObjs.add(s);
			}
		}
		avatarSprites.removeAll(removeObjs);
		if(avatarSprites.size() <= 0){
			avatarSprites.add("avatar");
		}
		
		if(solidSprites.size() > 0){
			removeObjs.clear();
			for(String s:solidSprites){
				if(!allowedObjs.contains(s)){
					removeObjs.add(s);
				}
			}
			solidSprites.removeAll(removeObjs);
			if(solidSprites.size() <= 0){
				solidSprites.add("wall");
			}
		}
		
		removeObjs.clear();
		for(String s:goalSprites){
			if(!allowedObjs.contains(s)){
				removeObjs.add(s);
			}
		}
		goalSprites.removeAll(removeObjs);
		
		removeObjs.clear();
		for(String s:harmfulSprites){
			if(!allowedObjs.contains(s)){
				removeObjs.add(s);
			}
		}
		harmfulSprites.removeAll(removeObjs);
		
		removeObjs.clear();
		for(String s:collectableSprites){
			if(!allowedObjs.contains(s)){
				removeObjs.add(s);
			}
		}
		collectableSprites.removeAll(removeObjs);
		
		removeObjs.clear();
		for(String s:otherSprites){
			if(!allowedObjs.contains(s)){
				removeObjs.add(s);
			}
		}
		otherSprites.removeAll(removeObjs);
	}
	
	/**
	 * Initialize GameAnalyzer
	 * @param game	game description object for the current game
	 */
	public GameAnalyzer(GameDescription game){
		minRequiredNumber = new HashMap<String, Integer>();
		priorityValue = new HashMap<String, Integer>();
		
		solidSprites = new ArrayList<String>();
		avatarSprites = new ArrayList<String>();
		harmfulSprites = new ArrayList<String>();
		collectableSprites = new ArrayList<String>();
		goalSprites = new ArrayList<String>();
		otherSprites = new ArrayList<String>();
		
		calculateMinRequiredNumber(game);
		calculatePriorityValues(game);
		
		findSolidObjects(game);
		findAvatarObjects(game);
		findGoalSprites(game);
		findHarmfulSprites(game);
		findCollectableSprites(game);
		findOtherSprites(game);

		removeUselessObjects(game);
		calculateMinMaxScoreUnit(game);
	}
	
	/**
	 * Checks if the object is spawned by other object
	 * @param stype	sprite name to be checked
	 * @return		return 1 if not spawned by other object and 0 otherwise	
	 */
	public int checkIfSpawned(String stype){
		return minRequiredNumber.get(stype);
	}
	
	/**
	 * Get the priority value for a specific sprite
	 * @param stype	sprite name to be checked
	 * @return		number of occurence of the sprite in the InteractionSet
	 */
	public int getPriorityNumber(String stype){
		if(!priorityValue.containsKey(stype)){
			return 0;
		}
		
		return priorityValue.get(stype);
	}
	
	/**
	 * Get array of solid sprite names
	 * @return	array contains all solid sprites
	 */
	public ArrayList<String> getSolidSprites(){
		return solidSprites;
	}
	
	/**
	 * get array of avatar sprite names
	 * @return	array of all sprite names marked as avatar
	 */
	public ArrayList<String> getAvatarSprites(){
		return avatarSprites;
	}
	
	/**
	 * get array of all sprites that can kill the avatar
	 * @return	array of all harmful sprite names
	 */
	public ArrayList<String> getHarmfulSprites(){
		return harmfulSprites;
	}
	
	/**
	 * get array for all objects that can be collected using player
	 * @return	array list contains collectible sprites
	 */
	public ArrayList<String> getCollectableSprites(){
		return collectableSprites;
	}
	
	/**
	 * get an array contains all sprites found in the termination set
	 * @return	array list contains all goal sprites
	 */
	public ArrayList<String> getGoalSprites(){
		return goalSprites;
	}
	
	/**
	 * get an array list of all other sprites that are not listed in the previous lists
	 * @return	array list of all other sprites
	 */
	public ArrayList<String> getOtherSprites(){
		return otherSprites;
	}
	
	/**
	 * get maximum +ve score change listed in the instruction set
	 * @return	maximum +ve score change value
	 */
	public double getMaxScoreUnit(){
		return maxScoreUnit;
	}
	
	/**
	 * get minimum +ve score change listed in the instruction set
	 * @return minimum +ve score change value
	 */
	public double getMinScoreUnit(){
		return minScoreUnit;
	}
	
	/**
	 * Internal Enum for getAllInteractions function
	 */
	private enum InteractionType{
		ALL,
		FIRST,
		SECOND
	}
}
