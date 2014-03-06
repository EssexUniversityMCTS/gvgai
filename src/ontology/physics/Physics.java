package ontology.physics;

import core.VGDLSprite;
import tools.Vector2d;

import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 17/10/13
 * Time: 11:21
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public interface Physics
{
    public void passiveMovement(VGDLSprite sprite);
    public void activeMovement(VGDLSprite sprite, Vector2d action, double speed);
    public double distance(Rectangle r1, Rectangle r2);
}
