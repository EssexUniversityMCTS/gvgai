package ontology.avatar.oriented;

import core.VGDLSprite;
import core.content.SpriteContent;
import ontology.Types;
import tools.Vector2d;

import java.awt.*;

/**
 * Created with IntelliJ IDEA. User: Diego Date: 22/10/13 Time: 18:12 This is a Java port from Tom Schaul's VGDL -
 * https://github.com/schaul/py-vgdl
 */
public class MarioAvatar extends InertialAvatar {
  public boolean airsteering;

  public MarioAvatar() {
  }

  public MarioAvatar(Vector2d position, Dimension size, SpriteContent cnt) {
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
    physicstype_id = Types.PHYSICS_GRAVITY;
    draw_arrow = false;
    strength = 10;
    airsteering = false;

  }

  @Override
  public VGDLSprite copy() {
    MarioAvatar newSprite = new MarioAvatar();
    copyTo(newSprite);
    return newSprite;
  }

  @Override
  public void copyTo(VGDLSprite target) {
    MarioAvatar targetSprite = (MarioAvatar) target;
    targetSprite.airsteering = airsteering;
    super.copyTo(targetSprite);
  }

}
