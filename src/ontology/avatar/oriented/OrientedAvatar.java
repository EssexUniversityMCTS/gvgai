package ontology.avatar.oriented;

import core.VGDLSprite;
import core.game.Game;
import ontology.Types;
import core.content.SpriteContent;
import ontology.avatar.MovingAvatar;
import tools.Vector2d;

import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 22/10/13
 * Time: 18:10
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class OrientedAvatar extends MovingAvatar
{
    public OrientedAvatar(){}

    public OrientedAvatar(Vector2d position, Dimension size, SpriteContent cnt)
    {
        //Init the sprite
        this.init(position, size);

        //Specific class default parameter values.
        loadDefaults();

        //Parse the arguments.
        this.parseParameters(cnt);
    }


    protected void loadDefaults()
    {
        super.loadDefaults();
        orientation = Types.RIGHT.copy();
        draw_arrow = true;
        is_oriented = true;
    }

    public void update(Game game)
    {
        Vector2d tmp = orientation.copy();
        orientation = Types.NONE.copy();

        super.update(game);

        Vector2d dir = lastDirection();
        if(dir.x == 0 && dir.y == 0)
        {
            //No movement.
            orientation = tmp;
        }else{
            //moved, update:
            dir.normalise();
            orientation = dir;
        }
    }

    public VGDLSprite copy()
    {
        OrientedAvatar newSprite = new OrientedAvatar();
        this.copyTo(newSprite);
        return newSprite;
    }

    public void copyTo(VGDLSprite target)
    {
        OrientedAvatar targetSprite = (OrientedAvatar) target;
        super.copyTo(targetSprite);
    }

}
