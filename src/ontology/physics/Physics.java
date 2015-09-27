package ontology.physics;

import java.awt.Rectangle;

import core.VGDLSprite;
import ontology.Types;
import tools.Vector2d;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 17/10/13
 * Time: 11:21
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public interface Physics
{
    public Types.MOVEMENT passiveMovement(VGDLSprite sprite);
    public Types.MOVEMENT activeMovement(VGDLSprite sprite, Vector2d action, double speed);
    public double distance(Rectangle r1, Rectangle r2);
}
