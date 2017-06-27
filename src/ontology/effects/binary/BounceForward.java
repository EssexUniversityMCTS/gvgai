package ontology.effects.binary;

import core.vgdl.VGDLSprite;
import core.content.InteractionContent;
import core.game.Game;
import core.logging.Logger;
import core.logging.Message;
import ontology.effects.Effect;
import tools.Direction;
import tools.Vector2d;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 04/11/13
 * Time: 15:56
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class BounceForward extends Effect
{

    public BounceForward(InteractionContent cnt)
    {
        this.parseParameters(cnt);
    }

    @Override
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game)
    {
        if(sprite1 == null || sprite2 == null){
            Logger.getInstance().addMessage(new Message(Message.WARNING, "Neither the 1st nor 2nd sprite can be EOS with BounceForward interaction."));
            return;
        }

        Vector2d dir = new Vector2d(sprite2.lastDirection());
        dir.normalise();

        if(sprite2.lastDirection().x * sprite2.orientation.x() < 0)
            dir.x *= -1;

        if(sprite2.lastDirection().y * sprite2.orientation.y() < 0)
            dir.y *= -1;

        //Rectangle r = new Rectangle(sprite1.rect);
        sprite1.physics.activeMovement(sprite1, new Direction(dir.x, dir.y), sprite2.speed);
        //sprite1.lastrect = r;
        sprite1.orientation = new Direction(dir.x, dir.y);
        game._updateCollisionDict(sprite1);
    }
}
