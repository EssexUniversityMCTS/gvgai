package ontology.avatar.oriented;

import core.VGDLSprite;
import core.content.SpriteContent;
import core.game.Game;
import ontology.Types;
import tools.Vector2d;

import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 21/10/13
 * Time: 17:35
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class MissileAvatar extends OrientedAvatar
{
    public MissileAvatar(){}

    public MissileAvatar(Vector2d position, Dimension size, SpriteContent cnt)
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
        speed = 1;
        is_oriented = true;
    }

    public void update(Game game)
    {
        //MissileAvatar has no actions available. Just update movement.
        updatePassive();
    }


    public VGDLSprite copy()
    {
        MissileAvatar newSprite = new MissileAvatar();
        this.copyTo(newSprite);
        return newSprite;
    }

    public void copyTo(VGDLSprite target)
    {
        MissileAvatar targetSprite = (MissileAvatar) target;
        super.copyTo(targetSprite);
    }
}
