package core;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 27/10/13
 * Time: 12:22
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class VGDLRegistry
{
    /**
     * Singleton instance of this class.
     */
    private static VGDLRegistry registry;

    /**
     * Maps strings to int. Strings are VGDL identifiers for SPRITE TYPES,
     * while integers are their unique int identifier counterparts.
     */
    private TreeMap<String, Integer> sprite_mapping;

    /**
     * Private constructor.
     */
    private VGDLRegistry(){}

    /**
     * Initializes the registry of sprites for games.
     */
    public void init()
    {
        sprite_mapping = new TreeMap<String, Integer>();
    }

    /**
     * Returns the unique instance of this class.
     * @return the unique instance of this class.
     */
    public static VGDLRegistry GetInstance()
    {
        if(registry == null)
        {
            registry = new VGDLRegistry();
            registry.init();
        }
        return registry;
    }

    /**
     * Register a new sprite string.
     * @param key key in the hashmap.
     * @return Returns its new index, or a new one if it was already registered.
     */
    public int registerSprite(String key)
    {
        int index = getRegisteredSpriteValue(key);
        if(index != -1)
            return index;

        //otherwise, insert.
        int numElements = sprite_mapping.size();
        sprite_mapping.put(key, numElements);
        return numElements;
    }

    /**
     * Returns the index (value in map) of a given key, for sprites.
     * @param key key to check
     * @return the value in map, -1 if it does not exist.
     */
    public int getRegisteredSpriteValue(String key)
    {
        if(sprite_mapping.containsKey(key))
            return sprite_mapping.get(key);
        return -1;
    }


    /**
     * Returns an array of indexes (value in map) of a set of keys, for sprites.
     * @param keys list of keys, separated by commas.
     * @return array with values in the map, -1 if it does not exist.
     */
    public int[] explode(String keys)
    {
        if(keys == null)
            return new int[]{-1};

        String[] keysArray = keys.split(",");
        int[] intKeys = new int[keysArray.length];
        for(int i = 0; i < keysArray.length; ++i)
            intKeys[i] = getRegisteredSpriteValue(keysArray[i]);

        return intKeys;
    }

    /**
     * Returns the String associated with the first (and in theory, unique) sprite value passed.
     * This method is for <b>debug purposes only</b>, should not be used for game execution.
     * @param value value whose key is returned.
     * @return the String associated with the value passed.
     */
    public String getRegisteredSpriteKey(int value)
    {
        //This method should not be used.
        //System.out.println("This method is deprecated, should not be used (other than debug).");
        if(sprite_mapping.containsValue(value))
        {
            Set<Map.Entry<String, Integer>> entries = sprite_mapping.entrySet();
            for(Map.Entry<String, Integer> entry : entries)
            {
                if(entry.getValue() == value)
                    return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Returns the -number of elements in the sprite_mapping array.
     * @return number of elements in the sprite_mapping array.
     */
    public int numSpriteTypes()
    {
        return sprite_mapping.size();
    }

}
