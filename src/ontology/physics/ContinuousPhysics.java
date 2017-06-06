package ontology.physics;

import java.awt.Rectangle;

import core.vgdl.VGDLSprite;
import ontology.Types;
import tools.Direction;
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

    @Override
    public Types.MOVEMENT passiveMovement(VGDLSprite sprite)
    {
    	
    	if(sprite.isFirstTick)
        {
            sprite.isFirstTick = false;
            return Types.MOVEMENT.STILL;
        }

        //This needs to be thoroughly tested when continuous physics are added
        //Specially the returned type.
    	
        if(sprite.speed != 0)
        {
            sprite._updatePos(sprite.orientation, (int) sprite.speed);

            if(sprite.gravity > 0 && sprite.mass > 0 && !sprite.on_ground)
            {
            	Direction gravityAction = new Direction(0, sprite.gravity * sprite.mass);
                this.activeMovement(sprite, gravityAction, 0);
            }
            sprite.speed *= (1-sprite.friction);
            return Types.MOVEMENT.MOVE;
        }
        return Types.MOVEMENT.STILL;
    }


    @Override
    public Types.MOVEMENT activeMovement(VGDLSprite sprite, Direction action, double speed)
    {
        //Here the assumption is that the controls determine the direction of
        //acceleration of the sprite.
    	
        if(speed == 0)
            speed = sprite.speed;
        
        if(speed == -1)
            speed = sprite.speed;
    	
        double v1 = (action.x() / (float)sprite.mass) + (sprite.orientation.x() * speed);
        double v2 = (action.y() / (float)sprite.mass) + (sprite.orientation.y() * speed);

        Vector2d dir = new Vector2d(v1, v2);

        double speedD = dir.mag();
        if(sprite.max_speed != -1) {
            speedD = Math.min(dir.mag(), sprite.max_speed);
        }

        dir.normalise();
        Direction d = new Direction(dir.x, dir.y);

        sprite.orientation = d;
        sprite.speed = speedD;

        if(action.equals(Types.DNONE))
            return Types.MOVEMENT.STILL;
        else
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
