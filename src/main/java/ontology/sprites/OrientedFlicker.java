package ontology.sprites;

import core.VGDLSprite;
import core.content.SpriteContent;
import tools.Vector2d;

import java.awt.*;

/**
 * Created with IntelliJ IDEA. User: Diego Date: 21/10/13 Time: 16:24 This is a Java port from Tom Schaul's VGDL -
 * https://github.com/schaul/py-vgdl
 */
public class OrientedFlicker extends Flicker {

  public OrientedFlicker() {
  }

  public OrientedFlicker(Vector2d position, Dimension size, SpriteContent cnt) {
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
    draw_arrow = true;
    speed = 0;
    is_oriented = true;
  }

  @Override
  public VGDLSprite copy() {
    OrientedFlicker newSprite = new OrientedFlicker();
    copyTo(newSprite);
    return newSprite;
  }

  @Override
  public void copyTo(VGDLSprite target) {
    OrientedFlicker targetSprite = (OrientedFlicker) target;
    super.copyTo(targetSprite);
  }
}
