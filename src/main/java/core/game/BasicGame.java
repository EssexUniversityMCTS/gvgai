package core.game;

import core.VGDLFactory;
import core.VGDLRegistry;
import core.content.GameContent;
import tools.IO;
import tools.Vector2d;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA. User: Diego Date: 16/10/13 Time: 14:00 This is a Java port from Tom Schaul's VGDL -
 * https://github.com/schaul/py-vgdl
 */
public class BasicGame extends Game {

  /**
   * Allows definition of sprite size from the VGDL description. If indicated, super.block_size is set to this variable.
   * square_size should be divisible by all speeds in the game definition.
   */
  public int square_size;

  /**
   * Default constructor for a basic game.
   * 
   * @param content Contains parameters for the game.
   */
  public BasicGame(GameContent content) {

    // Add here whatever mappings are common for all BasicGames.
    charMapping.put('w', new ArrayList<>());
    charMapping.get('w').add("wall");

    charMapping.put('A', new ArrayList<>());
    charMapping.get('A').add("avatar");

    // Default values for frame rate and maximum number of sprites allowed.
    square_size = -1;
    frame_rate = 25;
    MAX_SPRITES = 10000;

    // Parse the arguments.
    parseParameters(content);
  }

  @Override
  /**
   * Builds a level from this game, reading it from file.
   * @param gamelvl filename of the level to load.
   */
  public void buildLevel(String gamelvl) {
    // Read the level description
    String[] desc_lines = new IO().readFile(gamelvl);

    // Dimensions of the level read from the file.
    size.width = desc_lines[0].length();
    size.height = desc_lines.length;

    block_size =
        -1 != square_size ? square_size : Math.max(2, (int) 800.0
            / Math.max(size.width, size.height));
    screenSize = new Dimension(size.width * block_size, size.height * block_size);

    // All sprites are created and placed here:
    for (int i = 0; i < size.height; ++i) {
      String line = desc_lines[i];

      // This might happen. We just concat ' ' until size.
      if (line.length() < size.width)
        line = completeLine(line, size.width - line.length(), " ");

      // For each character
      for (int j = 0; j < size.width; ++j) {
        Character c = line.charAt(j);

        // If this character is defined in the array of mappings.
        if (charMapping.containsKey(c)) {
          // Get its position and add it to the game.
          Vector2d position = new Vector2d(j * block_size, i * block_size);
          addSpritesIn(charMapping.get(c), position);
        }

      }
    }

    // Nobody has been killed... yet!
    kill_list = new ArrayList<>();

    // Generate the initial state observation.
    initForwardModel();
  }

  /**
   * Reads the parameters of a game type.
   * 
   * @param content list of parameter-value pairs.
   */
  protected void parseParameters(GameContent content) {
    VGDLFactory factory = VGDLFactory.GetInstance();
    Class refClass = VGDLFactory.registeredGames.get(content.referenceClass);
    // System.out.println("refClass" + refClass.toString());
    if (!getClass().equals(refClass)) {
      System.out.println("Error: Game subclass instance not the same as content.referenceClass"
          + ' ' + getClass() + ' ' + refClass);
      return;
    }

    factory.parseParameters(content, this);
  }

  /**
   * Adds all sprites that 'c' represents in the position indicated.
   * 
   * @param keys List of sprite types to add.
   * @param position position where all these sprites will be placed.
   */
  public void addSpritesIn(Iterable<String> keys, Vector2d position) {
    // We might have more than one sprite in the same position.
    for (String objectType : keys) {
      int itype = VGDLRegistry.GetInstance().getRegisteredSpriteValue(objectType);
      addSprite(itype, position);
    }
  }

  /**
   * Takes a line and concats filler as many times as specified.
   * 
   * @param base initial string.
   * @param occurrences how many times filler is appended
   * @param filler string to append occurrences times to base.
   * @return the resultant string.
   */
  private String completeLine(String base, int occurrences, String filler) {
    for (int i = 0; i < occurrences; ++i)
      base = base.concat(filler);
    return base;
  }

}
