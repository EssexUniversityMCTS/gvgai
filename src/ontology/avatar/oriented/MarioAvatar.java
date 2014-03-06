package ontology.avatar.oriented;

import core.VGDLSprite;
import ontology.Types;
import core.content.SpriteContent;
import tools.Vector2d;

import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 22/10/13
 * Time: 18:12
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class MarioAvatar extends InertialAvatar
{
    public boolean airsteering;

    public MarioAvatar(){}

    public MarioAvatar(Vector2d position, Dimension size, SpriteContent cnt)
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
        physicstype_id = Types.PHYSICS_GRAVITY;
        draw_arrow = false;
        strength = 10;
        airsteering = false;

    }


    public VGDLSprite copy()
    {
        MarioAvatar newSprite = new MarioAvatar();
        this.copyTo(newSprite);
        return newSprite;
    }

    public void copyTo(VGDLSprite target)
    {
        MarioAvatar targetSprite = (MarioAvatar) target;
        targetSprite.airsteering = this.airsteering;
        super.copyTo(targetSprite);
    }

}
