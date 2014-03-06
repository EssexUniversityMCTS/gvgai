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
public class AimedFlakAvatar extends AimedAvatar
{
    public AimedFlakAvatar(){}

    public AimedFlakAvatar(Vector2d position, Dimension size, SpriteContent cnt)
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
        only_active = true;
        speed = 0;
    }


    public VGDLSprite copy()
    {
        AimedFlakAvatar newSprite = new AimedFlakAvatar();
        this.copyTo(newSprite);
        return newSprite;
    }

    public void copyTo(VGDLSprite target)
    {
        AimedFlakAvatar targetSprite = (AimedFlakAvatar) target;
        super.copyTo(targetSprite);
    }
}
