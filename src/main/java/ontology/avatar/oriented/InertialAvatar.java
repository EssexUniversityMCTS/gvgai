package ontology.avatar.oriented;

import core.VGDLSprite;
import core.content.SpriteContent;
import ontology.Types;
import tools.Vector2d;

import java.awt.*;

/**
 * Created with IntelliJ IDEA. User: Diego Date: 22/10/13 Time: 18:11 This is a Java port from Tom Schaul's VGDL -
 * https://github.com/schaul/py-vgdl
 */
public class InertialAvatar extends OrientedAvatar {

  public InertialAvatar() {
  }

  public InertialAvatar(Vector2d position, Dimension size, SpriteContent cnt) {
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
    speed = 1;
    physicstype_id = Types.PHYSICS_CONT;
  }

  @Override
  public VGDLSprite copy() {
    InertialAvatar newSprite = new InertialAvatar();
    copyTo(newSprite);
    return newSprite;
  }

  @Override
  public void copyTo(VGDLSprite target) {
    InertialAvatar targetSprite = (InertialAvatar) target;
    super.copyTo(targetSprite);
  }
}
