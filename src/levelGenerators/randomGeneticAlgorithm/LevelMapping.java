package levelGenerators.randomGeneticAlgorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import core.game.GameDescription.SpriteData;

/**
 * Helps to construct a HashMap<Character, Array<String>> easily
 * @author Ahmed A Khalifa
 *
 */
public class LevelMapping {
	private HashMap<Long, String> allCodeSprites;
	private HashMap<String, Long> allSpriteCodes;
	private HashMap<Long, Character> charMapping;
	private long index = 1;
	
	public LevelMapping(){
		ArrayList<SpriteData> allSprites = SharedData.gameDescription.getAllSpriteData();
		
		allSpriteCodes = new HashMap<String, Long>();
		allCodeSprites = new HashMap<Long, String>();
		
		for(SpriteData sprite:allSprites){
			allSpriteCodes.put(sprite.name, index);
			allCodeSprites.put(index, sprite.name);
			index *= 2;
		}
		
		charMapping = new HashMap<Long, Character>();
	}
	
	public LevelMapping(HashMap<Character, ArrayList<String>> charMapping){
		this();
		
		for(Entry<Character, ArrayList<String>> c:charMapping.entrySet()){
			addCharacterMapping(c.getKey(), c.getValue());
		}
	}
	
	private long hashValueFunction(ArrayList<String> data){
		long result = 0;
		
		for(String sprite:data){
			result += allSpriteCodes.get(sprite);
		}
		
		return result;
	}
	
	private ArrayList<String> reverseHash(long data){
		ArrayList<String> result = new ArrayList<String>();
		
		for(long mask:allCodeSprites.keySet()){
			if((data & mask) != 0){
				result.add(allCodeSprites.get(mask));
			}
		}
		
		return result;
	}
	
	public void clearLevelMapping(){
		charMapping.clear();
	}
	
	public void addCharacterMapping(Character c, ArrayList<String> list){
		charMapping.put(hashValueFunction(list), c);
	}
	
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
	
	public ArrayList<String> getArrayList(Character data){
		return getCharMapping().get(data);
	}
	
	public HashMap<Character, ArrayList<String>> getCharMapping(){
		HashMap<Character, ArrayList<String>> result = new HashMap<Character, ArrayList<String>>();
		
		for(Entry<Long, Character> entry:charMapping.entrySet()){
			result.put(entry.getValue(), reverseHash(entry.getKey()));
		}
		
		return result;
	}
}
