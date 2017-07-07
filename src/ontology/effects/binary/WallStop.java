package ontology.effects.binary;

import java.util.ArrayList;

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
public class WallStop extends Effect
{
    private double friction;
    private int lastGameTime;
    private ArrayList<VGDLSprite> spritesThisCycle;

    public WallStop(InteractionContent cnt)
    {
        lastGameTime = -1;
        spritesThisCycle = new ArrayList<VGDLSprite>();
        this.parseParameters(cnt);
    }

    @Override
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game)
    {
	if(sprite1 == null || sprite2 == null){
	    Logger.getInstance().addMessage(new Message(Message.WARNING, "Neither the 1st nor 2nd sprite can be EOS with WallStop interaction."));
	    return;
	}
	
        // Stop just in front of the wall, removing that velocity component, but possibly sliding along it.

        //Keep in the list, for the current cycle, the sprites that have triggered this event.
        int currentGameTime = game.getGameTick();
        if(currentGameTime > lastGameTime)
        {
            spritesThisCycle.clear();
            lastGameTime = currentGameTime;
        }

        //the event gets triggered only once per time-step on each sprite.
        if(spritesThisCycle.contains(sprite1))
            return;

        //sprite1.setRect(sprite1.lastrect);
        sprite1.setRect(calculatePixelPerfect(sprite1, sprite2));

        double centerXDiff = Math.abs(sprite1.rect.getCenterX() - sprite2.rect.getCenterX());
        double centerYDiff = Math.abs(sprite1.rect.getCenterY() - sprite2.rect.getCenterY());

        Vector2d v;
        if(centerXDiff > centerYDiff)
        {
            //sprite1.orientation = new Direction(0, sprite1.orientation.y() * (1.0 - friction));
            v = new Vector2d(0, sprite1.orientation.y());
        }else
        {
            //sprite1.orientation = new Direction(sprite1.orientation.x() * (1.0 - friction), 0);
            v = new Vector2d(sprite1.orientation.x(), 0);
        }

        double mag = v.mag();
        v.normalise();
        sprite1.orientation = new Direction(v.x, v.y);
        sprite1.speed = mag * sprite1.speed;
        if (sprite1.speed < sprite1.gravity){
            sprite1.speed = sprite1.gravity;
        }
    }
}
