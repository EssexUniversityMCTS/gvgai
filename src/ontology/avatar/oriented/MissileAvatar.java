package ontology.avatar.oriented;

import java.awt.Dimension;

import core.VGDLSprite;
import core.content.SpriteContent;
import core.game.Game;
import tools.Vector2d;

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

    /**
     * This update call is for the game tick() loop.
     * @param game current state of the game.
     */
    public void update(Game game)
    {
        //Get the input from the player (it won't be processed, but we allow thinking time).
        requestPlayerInput(game);

        //MissileAvatar has no actions available. Just update movement.
        super.updatePassive();
    }


    /**
     * This move call is for the Forward Model tick() loop.
     * @param game current state of the game.
     * @param actionMask action to apply.
     */
    public void move(Game game, boolean[] actionMask)
    {
        super.updatePassive();
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
