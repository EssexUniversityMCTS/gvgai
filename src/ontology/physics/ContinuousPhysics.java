package ontology.physics;

import java.awt.Rectangle;

import core.VGDLSprite;
import ontology.Types;
import tools.Vector2d;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 17/10/13
 * Time: 11:37
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class ContinuousPhysics extends GridPhysics
{
    /**
     * Gravity of the physics
     */
    public double gravity;

    /**
     * Friction applicable in this physics system.
     */
    public double friction;

    /**
     * Default constructor.
     */
    public ContinuousPhysics()
    {
        gravity = 0;
        friction = 0.02;
    }

    @Override
    public Types.MOVEMENT passiveMovement(VGDLSprite sprite)
    {
        //This needs to be thoroughly tested when continuous physics are added
        //Specially the returned type.
        if(sprite.speed != 0 && sprite.is_oriented)
        {
            sprite._updatePos(sprite.orientation, (int) sprite.speed);
            if(this.gravity > 0 && sprite.mass > 0)
            {
                Vector2d gravityAction = new Vector2d(0, this.gravity * sprite.mass);
                this.activeMovement(sprite, gravityAction, 0);
            }else return Types.MOVEMENT.STILL;
            sprite.speed *= (1-this.friction);

            return Types.MOVEMENT.MOVE;
        }
        return Types.MOVEMENT.STILL;
    }


    @Override
    public Types.MOVEMENT activeMovement(VGDLSprite sprite, Vector2d action, double speed)
    {
        //Here the assumption is that the controls determine the direction of
        //acceleration of the sprite.
        if(speed == 0)
            speed = sprite.speed;

        if(speed == 0)
            return Types.MOVEMENT.STILL;

        double v1 = (action.x / (float)sprite.mass) + (sprite.orientation.x * speed);
        double v2 = (action.y / (float)sprite.mass) + (sprite.orientation.y * speed);
        Vector2d dir  = new Vector2d(v1,v2);

        /*Vector2d unitDir = dir.unitVector();
        sprite.orientation = unitDir;
        sprite.speed = dir.mag() / sprite.orientation.mag(); */

        sprite.orientation = dir;
        sprite.orientation.normalise();
        sprite.speed = dir.mag();

        return Types.MOVEMENT.MOVE;
    }


    /**
     * Euclidean distance between two rectangles.
     * @param r1 rectangle 1
     * @param r2 rectangle 2
     * @return Euclidean distance between the top-left corner of the rectangles.
     */
    public double distance(Rectangle r1, Rectangle r2)
    {
        double topDiff = r1.getMinY() - r2.getMinY();
        double leftDiff = r1.getMinX() - r2.getMinX();
        return Math.sqrt(topDiff*topDiff + leftDiff*leftDiff);
    }
}
