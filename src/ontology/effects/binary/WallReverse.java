package ontology.effects.binary;

import core.content.InteractionContent;
import core.game.Game;
import core.logging.Logger;
import core.logging.Message;
import core.vgdl.VGDLSprite;
import ontology.effects.Effect;
import tools.Direction;
import tools.Vector2d;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 04/11/13
 * Time: 15:56
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class WallReverse extends Effect
{
    private double friction;
    private int lastGameTime;
    private ArrayList<VGDLSprite> spritesThisCycle;

    public WallReverse(InteractionContent cnt)
    {
        super.inBatch = true;
        lastGameTime = -1;
        spritesThisCycle = new ArrayList<VGDLSprite>();
        this.parseParameters(cnt);
    }

    @Override
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game)
    {
	if(sprite1 == null || sprite2 == null){
	    Logger.getInstance().addMessage(new Message(Message.WARNING, "Neither the 1st nor 2nd sprite can be EOS with WallReverse interaction."));
	    return;
	}
	
        doReverse(sprite1, sprite2.rect, game);

        sprite1.setRect(sprite1.lastrect);
        sprite2.setRect(sprite2.lastrect);
    }

    public int executeBatch(VGDLSprite sprite1, ArrayList<VGDLSprite> sprite2list, Game game) {

        int nColls = super.sortBatch(sprite1, sprite2list, game);

        if(nColls == 1)
        {
            doReverse(sprite1, sprite2list.get(0).rect, game);
        }else{
            doReverse(sprite1, collision, game);
        }

        sprite1.setRect(sprite1.lastrect);
        for (VGDLSprite sprite2 : sprite2list)
            sprite2.setRect(sprite2.lastrect);

        return nColls;
    }

    private void doReverse(VGDLSprite sprite1, Rectangle s2rect, Game g)
    {
        boolean collisions[] = super.determineCollision(sprite1, s2rect, g);
        boolean horizontalBounce = collisions[0];
        boolean verticalBounce = collisions[1];


        Vector2d v;
        if(verticalBounce)
        {
            v = new Vector2d(sprite1.orientation.x(), 0);
        }else if(horizontalBounce)
        {
            v = new Vector2d(-sprite1.orientation.x(), 0);
        }else{
            //By default:
            v = new Vector2d(-sprite1.orientation.x(), 0);
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
