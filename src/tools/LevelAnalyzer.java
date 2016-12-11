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
     * Get an array of sprites that their percentage is between lowThreshold and highThreshold
     * @param list		the list required to be searched
     * @param lowThreshold	the minimum percentage that the searched sprite cover the map
     * @param highThreshold	the maximum percentage that the searched sprite cover the map
     * @param inMap		boolean to define if the sprite has to be in the defined level
     * @return			array of the found sprites that satisfy the constraints
     */
    private SpriteData[] getSpriteData (ArrayList<SpriteData> list, double lowThreshold, double highThreshold, boolean inMap){
	ArrayList<SpriteData> temp = new ArrayList<SpriteData>();
	
	for(int i=0; i<list.size(); i++){
	    SpriteData s = list.get(i);
	    double percentage = numberOfSprites.get(s.name) / (1.0 * getLength() * getWidth());
	    if(percentage < lowThreshold || percentage > highThreshold){
		continue;
	    }
	    
	    if(inMap){
		if(usefulSprites.contains(s)){
		    temp.add(s);
		}
	    }
	    else{
		temp.add(s);
	    }
	}
	
	return convertToArray(temp);
    }
    
    /**
     * Get avatar sprite data
     * @param inMap	if he is defined in the map or not
     * @return		array of all avatars that specify the input parameters
     */
    public SpriteData[] getAvatars(boolean inMap){
	return getSpriteData(avatarSprites, 0, 1, inMap);
    }
    
    /**
     * get all npcs that are in between lowThreshold and highThreshold percentages
     * @param lowThreshold	the lowest percentage
     * @param highThreshold	the maximum percentage
     * @param inMap		the sprite should be in the map
     * @return			array of all npcs that specify the input parameters
     */
    public SpriteData[] getNPCs(double lowThreshold, double highThreshold, boolean inMap){
	return getSpriteData(npcSprites, lowThreshold, highThreshold, inMap);
    }
    
    /**
     * get all immovables that are in between lowThreshold and highThreshold percentages
     * @param lowThreshold	the lowest percentage
     * @param highThreshold	the maximum percentage
     * @param inMap		the sprite should be in the map
     * @return			array of all immovables that specify the input parameters
     */
    public SpriteData[] getImmovables(double lowThreshold, double highThreshold, boolean inMap){
	return getSpriteData(immovableSprites, lowThreshold, highThreshold, inMap);
    }
    
    /**
     * get all movables that are in between lowThreshold and highThreshold percentages
     * @param lowThreshold	the lowest percentage
     * @param highThreshold	the maximum percentage
     * @param inMap		the sprite should be in the map
     * @return			array of all movables that specify the input parameters
     */
    public SpriteData[] getMovables(double lowThreshold, double highThreshold, boolean inMap){
	return getSpriteData(movableSprites, lowThreshold, highThreshold, inMap);
    }
    
    /**
     * get all portals that are in between lowThreshold and highThreshold percentages
     * @param lowThreshold	the lowest percentage
     * @param highThreshold	the maximum percentage
     * @param inMap		the sprite should be in the map
     * @return			array of all portals that specify the input parameters
     */
    public SpriteData[] getPortals(double lowThreshold, double highThreshold, boolean inMap){
	return getSpriteData(portalsSprites, lowThreshold, highThreshold, inMap);
    }
    
    /**
     * get all resources that are in between lowThreshold and highThreshold percentages
     * @param lowThreshold	the lowest percentage
     * @param highThreshold	the maximum percentage
     * @param inMap		the sprite should be in the map
     * @return			array of all resources that specify the input parameters
     */
    public SpriteData[] getResources(double lowThreshold, double highThreshold, boolean inMap){
	return getSpriteData(resourceSprites, lowThreshold, highThreshold, inMap);
    }
    
    /**
     * get all spawners that are in between lowThreshold and highThreshold percentages
     * @param lowThreshold	the lowest percentage
     * @param highThreshold	the maximum percentage
     * @param inMap		the sprite should be in the map
     * @return			array of all spawners that specify the input parameters
     */
    public SpriteData[] getSpawners(double lowThreshold, double highThreshold, boolean inMap){
	return getSpriteData(spawnerSprites, lowThreshold, highThreshold, inMap);
    }
    
    /**
     * get all border objects that are in between lowThreshold and highThreshold percentages
     * @param lowThreshold	the lowest percentage
     * @param highThreshold	the maximum percentage
     * @return			array of all border sprites that specify the input parameters
     */
    public SpriteData[] getBorderObjects(double lowThreshold, double highThreshold){
	return getSpriteData(borderSprites, lowThreshold, highThreshold, true);
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
