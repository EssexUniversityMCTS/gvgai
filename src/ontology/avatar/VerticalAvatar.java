package ontology.avatar;

import core.content.SpriteContent;
import core.game.Game;
import core.vgdl.VGDLSprite;
import ontology.Types;
import tools.Vector2d;

import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 22/10/13
 * Time: 18:07
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class VerticalAvatar extends MovingAvatar
{
    public VerticalAvatar(){}

    public VerticalAvatar(Vector2d position, Dimension size, SpriteContent cnt)
    {
        //Init the sprite
        this.init(position, size);

        //Specific class default parameter values.
        loadDefaults();

        //Parse the arguments.
        this.parseParameters(cnt);
    }

    public void postProcess()
    {
        //Define actions here first.
        if(actions.size()==0)
        {
            actions.add(Types.ACTIONS.ACTION_UP);
            actions.add(Types.ACTIONS.ACTION_DOWN);
        }

        super.postProcess();
    }

    protected void loadDefaults()
    {
        super.loadDefaults();
    }

    public VGDLSprite copy()
    {
        VerticalAvatar newSprite = new VerticalAvatar();
        this.copyTo(newSprite);
        return newSprite;
    }

    public void copyTo(VGDLSprite target)
    {
        VerticalAvatar targetSprite = (VerticalAvatar) target;
        super.copyTo(targetSprite);
    }
}
