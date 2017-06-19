package tools;

import java.util.ArrayList;
import java.util.HashMap;

import core.game.GameDescription.SpriteData;
import core.game.SLDescription;

/**
 * This class analyze the game sprites with a specific level
 * @author AhmedKhalifa
 */
public class LevelAnalyzer {
	/**
	 * the current analyzed level
	 */
	private String[][] level;

	/**
	 * the number of sprites in the map
	 */
	private HashMap<String, Integer> numberOfSprites;
	/**
	 * Hashmap between all sprites that are on the same map location
	 */
	private HashMap<String, ArrayList<SpriteData>> sameTileSprites;

	/**
	 * different arrays for analyzed sprites
	 */
	private ArrayList<SpriteData> usefulSprites;
	private ArrayList<SpriteData> borderSprites;

	/**
	 * arrays for different type of sprites
	 */
	private ArrayList<SpriteData> avatarSprites;
	private ArrayList<SpriteData> npcSprites;
	private ArrayList<SpriteData> immovableSprites;
	private ArrayList<SpriteData> movableSprites;
	private ArrayList<SpriteData> portalsSprites;
	private ArrayList<SpriteData> resourceSprites;
	private ArrayList<SpriteData> spawnerSprites;

	/**
	 * Constructor for the level analyzer where it analyze SLDescription object
	 * @param description	the current game & level description
	 */
	public LevelAnalyzer(SLDescription description){
		SpriteData[] gameSprites = description.getGameSprites();
		level = description.getCurrentLevel();

		usefulSprites = new ArrayList<SpriteData>();
		borderSprites = new ArrayList<SpriteData>();
		spawnerSprites = new ArrayList<SpriteData>();

		avatarSprites = new ArrayList<SpriteData>();
		npcSprites = new ArrayList<SpriteData>();
		immovableSprites = new ArrayList<SpriteData>();
		movableSprites = new ArrayList<SpriteData>();
		portalsSprites = new ArrayList<SpriteData>();
		resourceSprites = new ArrayList<SpriteData>();

		numberOfSprites = new HashMap<String, Integer>();
		sameTileSprites = new HashMap<String, ArrayList<SpriteData>>();

		for(int i=0; i<gameSprites.length; i++){
			numberOfSprites.put(gameSprites[i].name, 0);

			if(gameSprites[i].isStatic){
				immovableSprites.add(gameSprites[i]);
				if(gameSprites[i].type.equalsIgnoreCase("SpawnPoint")){
					spawnerSprites.add(gameSprites[i]);
				}
			}
			else if(gameSprites[i].isAvatar){
				avatarSprites.add(gameSprites[i]);
			}
			else if(gameSprites[i].isNPC){
				npcSprites.add(gameSprites[i]);
			}
			else if(gameSprites[i].isPortal){
				portalsSprites.add(gameSprites[i]);
			}
			else if(gameSprites[i].isResource){
				resourceSprites.add(gameSprites[i]);
			}
			else{
				movableSprites.add(gameSprites[i]);
			}
		}

		for(int y=0; y<level.length; y++){
			for(int x=0; x<level[y].length; x++){
				String[] parts = level[y][x].split(",");
				if(parts != null){
					for(int i=0; i<parts.length; i++){
						SpriteData s = getSpriteData(gameSprites, parts[i].trim());
						if(s == null){
							continue;
						}

						if(!borderSprites.contains(s) && (x==0 || y==0 || x==getWidth()-1 || y==getLength()-1)){
							borderSprites.add(s);
						}
						if(!usefulSprites.contains(s)){
							usefulSprites.add(s);
						}
						numberOfSprites.put(s.name, numberOfSprites.get(s.name) + 1);
					}
				}
			}
		}

		for(int i=0; i<usefulSprites.size(); i++){
			sameTileSprites.put(usefulSprites.get(i).name, new ArrayList<SpriteData>());
		}

		for(int y=0; y<level.length; y++){
			for(int x=0; x<level[y].length; x++){
				String[] parts = level[y][x].split(",");
				if(parts != null){
					for(int i=0; i<parts.length; i++){
						SpriteData s1 = getSpriteData(gameSprites, parts[i].trim());
						if (s1 != null) {
							for (int j = 0; j < parts.length; j++) {
								SpriteData s2 = getSpriteData(gameSprites, parts[j].trim());
								if (s1.name.equals(s2.name)) {
									continue;
								}
								sameTileSprites.get(s1.name).add(s2);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * get spritedata using its name
	 * @param gameSprites	list of all spritedata in the game
	 * @param spriteName	the current spritename used in searching
	 * @return			the current spritedata corresponding to the
	 * 				spritename or null if doesn't exist
	 */
	private SpriteData getSpriteData(SpriteData[] gameSprites, String spriteName){
		for(int i=0; i<gameSprites.length; i++){
			if(gameSprites[i].name.equals(spriteName)){
				return gameSprites[i];
			}
		}
		return null;
	}

	/**
	 * convert an arraylist of spritedata to a normal array
	 * @param list	the input arraylist
	 * @return		the output array equivalent to the list
	 */
	private SpriteData[] convertToArray(ArrayList<SpriteData> list){
		SpriteData[] array = new SpriteData[list.size()];
		for(int i=0; i<array.length; i++){
			array[i] = list.get(i);
		}
		return array;
	}

	/**
	 * get the level length (y-axis)
	 * @return	level length (y-axis)
	 */
	public int getLength(){
		return level.length;
	}

	/**
	 * get the level width (x-axis)
	 * @return	level width (x-axis)
	 */
	public int getWidth(){
		return level[0].length;
	}

	/**
	 * get the level area
	 * @return	level area
	 */
	public int getArea(){
		return getLength() * getWidth();
	}

	/**
	 * get the level perimeter
	 * @return	level perimeter
	 */
	public int getPerimeter(){
		return 2 * (getLength() + getWidth());
	}

	/**
	 * Get an array of sprites that their percentage is between lowThreshold and highThreshold
	 * @param list		the list required to be searched
	 * @param lowThreshold	the minimum percentage that the searched sprite cover the map
	 * @param highThreshold	the maximum percentage that the searched sprite cover the map
	 * @param inMap		boolean to define if the sprite has to be in the defined level
	 * @return			array of the found sprites that satisfy the constraints
	 */
	private SpriteData[] getSpriteData (ArrayList<SpriteData> list, int lowThreshold, int highThreshold){
		ArrayList<SpriteData> temp = new ArrayList<SpriteData>();

		for(int i=0; i<list.size(); i++){
			SpriteData s = list.get(i);
			if(numberOfSprites.get(s.name) < lowThreshold || numberOfSprites.get(s.name) > highThreshold){
				continue;
			}

			temp.add(s);
		}

		return convertToArray(temp);
	}

	/**
	 * Get avatar sprite data
	 * @param inMap	if he is defined in the map or not
	 * @return		array of all avatars that specify the input parameters
	 */
	public SpriteData[] getAvatars(boolean inMap){

		return inMap?getSpriteData(avatarSprites, 1, this.getArea()):getSpriteData(avatarSprites, 0, this.getArea());
	}

	/**
	 * get all npcs that are in between lowThreshold and highThreshold percentages
	 * @param lowThreshold	the lowest percentage
	 * @param highThreshold	the maximum percentage
	 * @return			array of all npcs that specify the input parameters
	 */
	public SpriteData[] getNPCs(double lowThreshold, double highThreshold){
		return getSpriteData(npcSprites, (int)(lowThreshold * this.getArea()), (int)(highThreshold * this.getArea()));
	}

	/**
	 * get all npcs that are in between lowThreshold and highThreshold percentages
	 * @param lowThreshold	the lowest number of sprites
	 * @param highThreshold	the maximum number of sprites
	 * @return			array of all npcs that specify the input parameters
	 */
	public SpriteData[] getNPCs(int lowThreshold, int highThreshold){
		return getSpriteData(npcSprites, lowThreshold, highThreshold);
	}

	/**
	 * get all npcs that either in map or not
	 * @param inMap	if npc is defined in the map or not
	 * @return		array of all npcs that specify the input parameters
	 */
	public SpriteData[] getNPCs(boolean inMap){
		return inMap?getSpriteData(npcSprites, 1, this.getArea()):getSpriteData(npcSprites, 0, this.getArea());
	}

	/**
	 * get all immovables that are in between lowThreshold and highThreshold percentages
	 * @param lowThreshold	the lowest percentage
	 * @param highThreshold	the maximum percentage
	 * @return			array of all immovables that specify the input parameters
	 */
	public SpriteData[] getImmovables(double lowThreshold, double highThreshold){
		return getSpriteData(immovableSprites, (int)(lowThreshold * this.getArea()), (int)(highThreshold * this.getArea()));
	}

	/**
	 * get all immovables that are in between lowThreshold and highThreshold percentages
	 * @param lowThreshold	the lowest number
	 * @param highThreshold	the maximum number
	 * @return			array of all immovables that specify the input parameters
	 */
	public SpriteData[] getImmovables(int lowThreshold, int highThreshold){
		return getSpriteData(immovableSprites, lowThreshold, highThreshold);
	}

	/**
	 * get all immovables that either in the map or not
	 * @param inMap	the sprite should be in the map
	 * @return		array of all immovables that specify the input parameters
	 */
	public SpriteData[] getImmovables(boolean inMap){
		return inMap?getSpriteData(immovableSprites, 1, this.getArea()):getSpriteData(immovableSprites, 0, this.getArea());
	}

	/**
	 * get all movables that are in between lowThreshold and highThreshold percentages
	 * @param lowThreshold	the lowest percentage
	 * @param highThreshold	the maximum percentage
	 * @return			array of all movables that specify the input parameters
	 */
	public SpriteData[] getMovables(double lowThreshold, double highThreshold){
		return getSpriteData(movableSprites, (int)(lowThreshold * this.getArea()), (int)(highThreshold * this.getArea()));
	}

	/**
	 * get all movables that are in between lowThreshold and highThreshold percentages
	 * @param lowThreshold	the lowest number
	 * @param highThreshold	the maximum number
	 * @return			array of all movables that specify the input parameters
	 */
	public SpriteData[] getMovables(int lowThreshold, int highThreshold){
		return getSpriteData(movableSprites, lowThreshold, highThreshold);
	}

	/**
	 * get all movables that are either in the map or not
	 * @param inMap	the sprite should be in the map
	 * @return		array of all movables that specify the input parameters
	 */
	public SpriteData[] getMovables(boolean inMap){
		return inMap?getSpriteData(movableSprites, 1, this.getArea()):getSpriteData(movableSprites, 0, this.getArea());
	}

	/**
	 * get all portals that are in between lowThreshold and highThreshold percentages
	 * @param lowThreshold	the lowest percentage
	 * @param highThreshold	the maximum percentage
	 * @return			array of all portals that specify the input parameters
	 */
	public SpriteData[] getPortals(double lowThreshold, double highThreshold){
		return getSpriteData(portalsSprites, (int)(lowThreshold * this.getArea()), (int)(highThreshold * this.getArea()));
	}

	/**
	 * get all portals that are in between lowThreshold and highThreshold percentages
	 * @param lowThreshold	the lowest number
	 * @param highThreshold	the maximum number
	 * @return			array of all portals that specify the input parameters
	 */
	public SpriteData[] getPortals(int lowThreshold, int highThreshold){
		return getSpriteData(portalsSprites, lowThreshold, highThreshold);
	}

	/**
	 * get all portals that are in between lowThreshold and highThreshold percentages
	 * @param inMap		the sprite should be in the map
	 * @return			array of all portals that specify the input parameters
	 */
	public SpriteData[] getPortals(boolean inMap){
		return inMap?getSpriteData(portalsSprites, 1, this.getArea()):getSpriteData(portalsSprites, 0, this.getArea());
	}

	/**
	 * get all resources that are in between lowThreshold and highThreshold percentages
	 * @param lowThreshold	the lowest percentage
	 * @param highThreshold	the maximum percentage
	 * @return			array of all resources that specify the input parameters
	 */
	public SpriteData[] getResources(double lowThreshold, double highThreshold){
		return getSpriteData(resourceSprites, (int)(lowThreshold * this.getArea()), (int)(highThreshold * this.getArea()));
	}

	/**
	 * get all resources that are in between lowThreshold and highThreshold percentages
	 * @param lowThreshold	the lowest number
	 * @param highThreshold	the maximum number
	 * @return			array of all resources that specify the input parameters
	 */
	public SpriteData[] getResources(int lowThreshold, int highThreshold){
		return getSpriteData(resourceSprites, lowThreshold, highThreshold);
	}

	/**
	 * get all resources that are in between lowThreshold and highThreshold percentages
	 * @param inMap		the sprite should be in the map
	 * @return			array of all resources that specify the input parameters
	 */
	public SpriteData[] getResources(boolean inMap){
		return inMap?getSpriteData(resourceSprites, 1, this.getArea()):getSpriteData(resourceSprites, 0, this.getArea());
	}

	/**
	 * get all spawners that are in between lowThreshold and highThreshold percentages
	 * @param lowThreshold	the lowest percentage
	 * @param highThreshold	the maximum percentage
	 * @return			array of all spawners that specify the input parameters
	 */
	public SpriteData[] getSpawners(double lowThreshold, double highThreshold){
		return getSpriteData(spawnerSprites, (int)(lowThreshold * this.getArea()), (int)(highThreshold * this.getArea()));
	}

	/**
	 * get all spawners that are in between lowThreshold and highThreshold percentages
	 * @param lowThreshold	the lowest number
	 * @param highThreshold	the maximum number
	 * @return			array of all spawners that specify the input parameters
	 */
	public SpriteData[] getSpawners(int lowThreshold, int highThreshold){
		return getSpriteData(spawnerSprites, lowThreshold, highThreshold);
	}

	/**
	 * get all spawners that are in between lowThreshold and highThreshold percentages
	 * @param inMap		the sprite should be in the map
	 * @return			array of all spawners that specify the input parameters
	 */
	public SpriteData[] getSpawners(boolean inMap){
		return inMap?getSpriteData(spawnerSprites, 1, this.getArea()):getSpriteData(spawnerSprites, 0, this.getArea());
	}

	/**
	 * get all border objects that are in between lowThreshold and highThreshold percentages
	 * @param lowThreshold	the lowest percentage
	 * @param highThreshold	the maximum percentage
	 * @return			array of all border sprites that specify the input parameters
	 */
	public SpriteData[] getBorderObjects(double lowThreshold, double highThreshold){
		return getSpriteData(borderSprites, (int)(lowThreshold*this.getArea()), (int)(highThreshold*this.getArea()));
	}

	/**
	 * get all border objects that are in between lowThreshold and highThreshold percentages
	 * @param lowThreshold	the lowest number
	 * @param highThreshold	the maximum number
	 * @return			array of all border sprites that specify the input parameters
	 */
	public SpriteData[] getBorderObjects(int lowThreshold, int highThreshold){
		return getSpriteData(borderSprites, lowThreshold, highThreshold);
	}

	/**
	 * get the number of times a certain sprite appear in the map
	 * @param spriteName	the name of the sprite
	 * @return			number of times this spritename appear in the level
	 */
	public double getNumberOfObjects(String spriteName){
		if(numberOfSprites.containsKey(spriteName)){
			return numberOfSprites.get(spriteName);
		}

		return 0;
	}

	/**
	 * get all the sprites that are on the same tile with a certain sprite
	 * @param spriteName	the name of the sprite
	 * @return			list of all sprites that share the same tile with the specified spritename
	 */
	public SpriteData[] getSpritesOnSameTile(String spriteName){
		return convertToArray(sameTileSprites.get(spriteName));
	}
}
