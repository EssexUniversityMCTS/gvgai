package core.vgdl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Created by Diego on 18/03/14.
 * This class encapsulates a SpriteGroup: a collection of VGDLSprite objects
 * identified with an unique id. All sprites in the SpriteGroup are of the same
 * type.
 */
public class SpriteGroup
{
    /**
     * Type of sprite this class holds a collection of.
     */
    private int itype;

    /**
     * Collection of sprites. They are maintained in a ConcurrentHashMap, where the key is the
     * unique identifier for the given sprite (in the whole game).
     */
    //private ConcurrentHashMap<Integer, VGDLSprite> sprites;
    private ArrayList<VGDLSprite> sprites;

    /**
     * Creates a new SpriteGroup, specifying the type of sprites this will hold.
     * @param itype type of sprite for the SpriteGroup.
     */
    public SpriteGroup(int itype)
    {
        this.itype = itype;
        sprites = new ArrayList<VGDLSprite>(100);
    }

    /**
     * Adds an sprite to the collection.
     * @param spriteId Unique ID of the sprite to add
     * @param sprite Sprite to add.
     */
    public void addSprite(int spriteId, VGDLSprite sprite)
    {
        sprites.add(sprite);
    }


    /**
     * Adds a collection of sprites to this collection.
     * @param spritesToAdd Sprites to add.
     */
    public void addAllSprites(Collection<VGDLSprite> spritesToAdd)
    {
        for(VGDLSprite sp : spritesToAdd)
            sprites.add(sp);
    }


    /**
     * Adds a collection of sprites to this collection. Each sprite added is a copy
     * of the original sprite in the array received.
     * @param spritesToCopy Sprites to copy.
     */
    public void copyAllSprites(Collection<VGDLSprite> spritesToCopy)
    {
        for(VGDLSprite sp : spritesToCopy)
            sprites.add(sp.copy());
    }

     /**
     * Gets the collection of sprites, as a ConcurrentHashMap [KEY => VALUE].
     * @return the TreeMap with the Sprites.
     */
    public ArrayList<VGDLSprite> getSprites()
    {
        return sprites;
    }

    /**
     * Gets the set of KEYs in an array.  It will return null if
     * the collection of sprites is empty.
     * @return the list of the sprite keys in this collection in an array.
     */
//    public Integer[] getKeys()
//    {
//        int nSprites = sprites.size();
//        if(nSprites == 0)
//            return null;
//        Integer[] keys = new Integer[nSprites];
//        return sprites.keySet().toArray(keys);
//    }

    /**
     * Gets an ordered iterator through all sprites. It will return null if
     * the collection of sprites is empty.
     * @return the list of the sprites in this collection in an iterator.
     */
    public Iterator<VGDLSprite> getSpriteIterator()
    {
        if(numSprites() == 0)
            return null;
        return sprites.iterator();
    }

    /**
     * Removes an sprite.
     * @param sprite the sprite to remove.
     */
    public void removeSprite(VGDLSprite sprite)
    {
        sprites.remove(sprite);
    }

    /**
     * Gets the type of this SpriteGroup.
     * @return the type of this sprite group
     */
    public int getItype()
    {
        return itype;
    }

    /**
     * Retrieves a sprite given its unique ID. It'll return null if the
     * sprite is not in the collection.
     * @param spriteId ID of the sprite to retrieve.
     * @return the desired sprite.
     */
    public VGDLSprite getSprite(int spriteId)
    {
        return sprites.get(spriteId);
    }

    /**
     * Retrieves a sprite given its position in the sprite list.
     * Returns null sprite is not in the collection.
     * @param idx index of the sprite.
     * @return the desired sprite.
     */
    public VGDLSprite getSpriteByIdx(int idx) {
        if(numSprites() == 0)
            return null;

        return sprites.get(idx);
    }

    /**
     * Clears the collection of sprites.
     */
    public void clear()
    {
        sprites.clear();
    }

    /**
     * Gets the number of sprites in the collection.
     * @return number of sprites in this collection.
     */
    public int numSprites()
    {
        return sprites.size();
    }

    /**
     * Returns the number of disabled sprites
     * @return the number of disabled sprites
     */
    public int numDisabledSprites() {
        int acum = 0;
        for (VGDLSprite sp: sprites) {
            if (sp.is_disabled()) acum++;
        }
        return acum;
    }

    /**
     * Gets the first sprite of this collection, or null if it is empty
     * @return the first sprite in this collection.
     */
    public VGDLSprite getFirstSprite()
    {
        if(numSprites() == 0)
            return null;
        return sprites.get(0); //sprites.values().toArray()[0]; //  firstEntry().getValue();
    }
}
