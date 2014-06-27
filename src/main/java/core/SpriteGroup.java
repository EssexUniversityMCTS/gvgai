package core;

import java.util.Iterator;
import java.util.TreeMap;

/**
 * Created by Diego on 18/03/14. This class encapsulates a SpriteGroup: a collection of VGDLSprite objects identified
 * with an unique id. All sprites in the SpriteGroup are of the same type.
 */
public class SpriteGroup {
  /**
   * Type of sprite this class holds a collection of.
   */
  private int itype;

  /**
   * Collection of sprites. They are maintained in a TreeMap, where the key is the unique identifier for the given
   * sprite (in the whole game).
   */
  private TreeMap<Integer, VGDLSprite> sprites;

  /**
   * Creates a new SpriteGroup, specifying the type of sprites this will hold.
   * 
   * @param itype type of sprite for the SpriteGroup.
   */
  public SpriteGroup(int itype) {
    this.itype = itype;
    sprites = new TreeMap<>();
  }

  /**
   * Adds an sprite to the collection.
   * 
   * @param spriteId Unique ID of the sprite to add
   * @param sprite Sprite to add.
   */
  public void addSprite(int spriteId, VGDLSprite sprite) {
    sprites.put(spriteId, sprite);
  }

  /**
   * Adds a collection of sprites to this collection.
   * 
   * @param spritesToAdd Sprites to add.
   */
  public void addAllSprites(Iterable<VGDLSprite> spritesToAdd) {
    for (VGDLSprite sp : spritesToAdd)
      sprites.put(sp.spriteID, sp);
  }

  /**
   * Gets the collection of sprites, as a TreeMap [KEY => VALUE].
   * 
   * @return the TreeMap with the Sprites.
   */
  public TreeMap<Integer, VGDLSprite> getSprites() {
    return sprites;
  }

  /**
   * Gets the set of KEYs in an array. It will return null if the collection of sprites is empty.
   * 
   * @return the list of the sprite keys in this collection in an array.
   */
  public Integer[] getKeys() {
    int nSprites = sprites.size();
    if (0 == nSprites)
      return null;
    Integer[] keys = new Integer[nSprites];
    return sprites.keySet().toArray(keys);
  }

  /**
   * Gets an ordered iterator through all sprites. It will return null if the collection of sprites is empty.
   * 
   * @return the list of the sprites in this collection in an iterator.
   */
  public Iterator<VGDLSprite> getSpriteIterator() {
    if (0 == numSprites())
      return null;
    return sprites.values().iterator();
  }

  /**
   * Removes an sprite indicated with its ID.
   * 
   * @param spriteId the id of the sprite to remove.
   */
  public void removeSprite(int spriteId) {
    sprites.remove(spriteId);
  }

  /**
   * Gets the type of this SpriteGroup.
   * 
   * @return the type of this sprite group
   */
  public int getItype() {
    return itype;
  }

  /**
   * Retrieves a sprite given its unique ID. It'll return null if the sprite is not in the collection.
   * 
   * @param spriteId ID of the sprite to retrieve.
   * @return the desired sprite.
   */
  public VGDLSprite getSprite(int spriteId) {
    return sprites.get(spriteId);
  }

  /**
   * Clears the collection of sprites.
   */
  public void clear() {
    sprites.clear();
  }

  /**
   * Gets the number of sprites in the collection.
   * 
   * @return number of sprites in this collection.
   */
  public int numSprites() {
    return sprites.size();
  }

  /**
   * Gets the first sprite of this collection, or null if it is empty
   * 
   * @return the first sprite in this collection.
   */
  public VGDLSprite getFirstSprite() {
    if (0 == numSprites())
      return null;
    return sprites.firstEntry().getValue();
  }
}
