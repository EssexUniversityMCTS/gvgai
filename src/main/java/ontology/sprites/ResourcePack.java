package ontology.sprites;

import core.VGDLSprite;
import core.content.SpriteContent;
import tools.Vector2d;

import java.awt.*;

/**
 * Created with IntelliJ IDEA. User: Diego Date: 24/10/13 Time: 10:22 This is a Java port from Tom Schaul's VGDL -
 * https://github.com/schaul/py-vgdl
 */
public class ResourcePack extends Resource {
  public ResourcePack() {
  }

  public ResourcePack(Vector2d position, Dimension size, SpriteContent cnt) {
    // Init the sprite
    init(position, size);

    // Specific class default parameter values.
    loadDefaults();

    // Parse the arguments.
    parseParameters(cnt);
  }

  @Override
  protected void loadDefaults() {
    super.loadDefaults();
    is_static = true;
  }

  @Override
  public VGDLSprite copy() {
    ResourcePack newSprite = new ResourcePack();
    copyTo(newSprite);
    return newSprite;
  }
}
