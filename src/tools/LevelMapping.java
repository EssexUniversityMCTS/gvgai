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
	 * 
	 */
	private HashMap<Long, String> allCodeSprites;
	/**
	 * 
	 */
	private HashMap<String, Long> allSpriteCodes;
	/**
	 * 
	 */
	private HashMap<Long, Character> charMapping;
	/**
	 * 
	 */
	private long index = 1;
	
	/**
	 * 
	 * @param game
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
	 * 
	 * @param game
	 * @param charMapping
	 */
	public LevelMapping(GameDescription game, HashMap<Character, ArrayList<String>> charMapping){
		this(game);
		
		for(Entry<Character, ArrayList<String>> c:charMapping.entrySet()){
			addCharacterMapping(c.getKey(), c.getValue());
		}
	}
	
	/**
	 * 
	 * @param data
	 * @return
	 */
	private long hashValueFunction(ArrayList<String> data){
		long result = 0;
		
		for(String sprite:data){
			result += allSpriteCodes.get(sprite);
		}
		
		return result;
	}
	
	/**
	 * 
	 * @param data
	 * @return
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
	 * 
	 */
	public void clearLevelMapping(){
		charMapping.clear();
	}
	
	/**
	 * 
	 * @param c
	 * @param list
	 */
	public void addCharacterMapping(Character c, ArrayList<String> list){
		charMapping.put(hashValueFunction(list), c);
	}
	
	/**
	 * 
	 * @param data
	 * @return
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
	 * 
	 * @param data
	 * @return
	 */
	public ArrayList<String> getArrayList(Character data){
		return getCharMapping().get(data);
	}
	
	/**
	 * 
	 * @return
	 */
	public HashMap<Character, ArrayList<String>> getCharMapping(){
		HashMap<Character, ArrayList<String>> result = new HashMap<Character, ArrayList<String>>();
		
		for(Entry<Long, Character> entry:charMapping.entrySet()){
			result.put(entry.getValue(), reverseHash(entry.getKey()));
		}
		
		return result;
	}
}
