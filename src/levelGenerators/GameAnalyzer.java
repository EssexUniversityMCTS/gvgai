package levelGenerators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import core.game.GameDescription;
import core.game.GameDescription.InteractionData;
import core.game.GameDescription.SpriteData;
import core.game.GameDescription.TerminationData;

public class GameAnalyzer {
	public final ArrayList<String> spawnerTypes = new ArrayList<String>(Arrays.asList(new String[]{"SpawnPoint", "Bomber", "RandomBomber", "Spreader", "ShootAvatar", "FlakAvatar"}));
	public final ArrayList<String> spawnInteractions = new ArrayList<String>(Arrays.asList(new String[]{"TransformTo", "SpawnIfHasMore", "SpawnIfHasLess"}));
	public final ArrayList<String> solidInteractions = new ArrayList<String>(Arrays.asList(new String[]{"StepBack", "UndoAll"}));
	public final ArrayList<String> deathInteractions = new ArrayList<String>(Arrays.asList(new String[]{"KillSprite", "KillIfHasMore", "KillIfHasLess", "KillIfFromAbove", "KillIfOtherHasMore"}));
	public final ArrayList<String> horzAvatar = new ArrayList<String>(Arrays.asList(new String[]{"FlakAvatar", "HorizontalAvatar"}));
	public final String resource = "Resource";
	public final String spriteCounter = "SpriteCounter";
	public final String multiCounter = "MultiSpriteCounter";
	
	private HashMap<String, Integer> minRequiredNumber;
	private HashMap<String, Integer> priorityValue;
	private double minScoreUnit;
	private double maxScoreUnit;
	
	private ArrayList<String> solidSprites;
	private ArrayList<String> avatarSprites;
	private ArrayList<String> harmfulSprites;
	private ArrayList<String> collectableSprites;
	private ArrayList<String> goalSprites;
	private ArrayList<String> otherSprites;
	
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
	
	private void calculatePriorityValues(GameDescription game){
		ArrayList<SpriteData> allSprites = game.getAllSpriteData();
		
		for(SpriteData s:allSprites){
			ArrayList<InteractionData> interactions = getAllInteractions(s.name, InteractionType.ALL, game);
			priorityValue.put(s.name, interactions.size());
		}
	}
	
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
	
	private void findAvatarObjects(GameDescription game){
		ArrayList<SpriteData> avatars = game.getAvatar();
		
		for(SpriteData sprite:avatars){
			if(!avatarSprites.contains(sprite.name)){
				avatarSprites.add(sprite.name);
			}
		}
	}
	
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
	
	private void findResourceAndOtherSprites(GameDescription game){
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
		findResourceAndOtherSprites(game);
		calculateMinMaxScoreUnit(game);
	}
	
	public int getMinRequiredNumber(String stype){
		return minRequiredNumber.get(stype);
	}
	
	public int getPriorityNumber(String stype){
		if(!priorityValue.containsKey(stype)){
			return 0;
		}
		
		return priorityValue.get(stype);
	}
	
	public ArrayList<String> getSolidSprites(){
		return solidSprites;
	}
	
	public ArrayList<String> getAvatarSprites(){
		return avatarSprites;
	}
	
	public ArrayList<String> getHarmfulSprites(){
		return harmfulSprites;
	}
	
	public ArrayList<String> getCollectableSprites(){
		return collectableSprites;
	}
	
	public ArrayList<String> getGoalSprites(){
		return goalSprites;
	}
	
	public ArrayList<String> getOtherSprites(){
		return otherSprites;
	}
	
	public double getMaxScoreUnit(){
		return maxScoreUnit;
	}
	
	public double getMinScoreUnit(){
		return minScoreUnit;
	}
	
	private enum InteractionType{
		ALL,
		FIRST,
		SECOND
	}
}
