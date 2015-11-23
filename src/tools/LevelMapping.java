package tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import core.game.GameDescription;
import core.game.GameDescription.SpriteData;

/**
 * Helps to construct a HashMap<Character, Array<String>> easily
 * @author Ahmed A Khalifa
 *
 */
public class LevelMapping {
	/**
	 * hashmap where each code has a sprite name
	 */
	private HashMap<Long, String> allCodeSprites;
	/**
	 * hashmap where each sprite name has a code
	 */
	private HashMap<String, Long> allSpriteCodes;
	/**
	 * current level mapping used 
	 */
	private HashMap<Long, Character> charMapping;
	/**
	 * index variable used to specify codes
	 */
	private long index = 1;
	
	/**
	 * construct level mapping object for the current game
	 * @param game	current game description object
	 */
	public LevelMapping(GameDescription game){
		ArrayList<SpriteData> allSprites = game.getAllSpriteData();
		
		allSpriteCodes = new HashMap<String, Long>();
		allCodeSprites = new HashMap<Long, String>();
		
		for(SpriteData sprite:allSprites){
			allSpriteCodes.put(sprite.name, index);
			allCodeSprites.put(index, sprite.name);
			index *= 2;
		}
		
		charMapping = new HashMap<Long, Character>();
	}
	
	/**
	 * Initialize the level mapping object for the current game using a starting level mapping
	 * @param game			current game description object
	 * @param charMapping	starting level mapping
	 */
	public LevelMapping(GameDescription game, HashMap<Character, ArrayList<String>> charMapping){
		this(game);
		
		for(Entry<Character, ArrayList<String>> c:charMapping.entrySet()){
			addCharacterMapping(c.getKey(), c.getValue());
		}
	}
	
	/**
	 * get the hashvalue for a list of sprites
	 * @param data	list of sprites to get its hashvalue
	 * @return		hashvalue corresponding to list of sprites
	 */
	private long hashValueFunction(ArrayList<String> data){
		long result = 0;
		
		for(String sprite:data){
			result += allSpriteCodes.get(sprite);
		}
		
		return result;
	}
	
	/**
	 * get list of sprite names from its hashvalue
	 * @param data	hashvalue required to be decrypted
	 * @return		arraylist for the sprite names from the decryption process
	 */
	private ArrayList<String> reverseHash(long data){
		ArrayList<String> result = new ArrayList<String>();
		
		for(long mask:allCodeSprites.keySet()){
			if((data & mask) != 0){
				result.add(allCodeSprites.get(mask));
			}
		}
		
		return result;
	}
	
	/**
	 * clear all recorded mapping
	 */
	public void clearLevelMapping(){
		charMapping.clear();
	}
	
	/**
	 * add a character for a list of sprite names
	 * @param c		character assigned to the list of sprite names
	 * @param list	list of sprite names to be inserted in the level mapping
	 */
	public void addCharacterMapping(Character c, ArrayList<String> list){
		long code = hashValueFunction(list);
		if(!charMapping.containsKey(code)){
			charMapping.put(code, c);
		}
	}
	
	/**
	 * get character corresponding to a certain arraylist of strings
	 * @param data	list of sprite names
	 * @return		character corresponding to the list
	 */
	public Character getCharacter(ArrayList<String> data){
		if(data.size() == 0){
			return ' ';
		}
		
		long hashValue = hashValueFunction(data);
		if(!charMapping.containsKey(hashValue)){
			return null;
		}
		
		return charMapping.get(hashValue);
	}
	
	/**
	 * get arraylist of sprite names that correspond to a certain character
	 * @param data	character required to be checked
	 * @return		list of sprite names corresponding to input character
	 */
	public ArrayList<String> getArrayList(Character data){
		return getCharMapping().get(data);
	}
	
	/**
	 * get a hashmap of characters and the corresponding sprite names
	 * @return	hashmap of characters and the corresponding sprite names
	 */
	public HashMap<Character, ArrayList<String>> getCharMapping(){
		HashMap<Character, ArrayList<String>> result = new HashMap<Character, ArrayList<String>>();
		
		for(Entry<Long, Character> entry:charMapping.entrySet()){
			result.put(entry.getValue(), reverseHash(entry.getKey()));
		}
		
		return result;
	}
}
