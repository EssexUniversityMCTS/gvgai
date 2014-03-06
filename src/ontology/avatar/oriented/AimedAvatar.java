package ontology.avatar.oriented;

import core.VGDLSprite;
import core.content.SpriteContent;
import tools.Vector2d;

import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 22/10/13
 * Time: 18:11
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class AimedAvatar extends ShootAvatar
{
    public double angle_diff;

    public AimedAvatar(){}

    public AimedAvatar(Vector2d position, Dimension size, SpriteContent cnt)
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
        speed = 0;
        angle_diff = 0.05;
    }


    public VGDLSprite copy()
    {
        AimedAvatar newSprite = new AimedAvatar();
        this.copyTo(newSprite);
        return newSprite;
    }

    public void copyTo(VGDLSprite target)
    {
        AimedAvatar targetSprite = (AimedAvatar) target;
        targetSprite.angle_diff = this.angle_diff;
        super.copyTo(targetSprite);
    }
}
