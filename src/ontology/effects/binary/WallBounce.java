package ontology.effects.binary;

import core.vgdl.VGDLSprite;
import core.content.InteractionContent;
import core.game.Game;
import core.logging.Logger;
import core.logging.Message;
import ontology.Types;
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
 * Date: 23/10/13
 * Time: 15:23
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class WallBounce extends Effect
{
    public WallBounce(InteractionContent cnt)
    {
        super.inBatch = true;
        this.parseParameters(cnt);
    }

    @Override
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game)
    {
	if(sprite1 == null || sprite2 == null){
	    Logger.getInstance().addMessage(new Message(Message.WARNING, "Neither the 1st nor 2nd sprite can be EOS with WallBounce interaction."));
	    return;
	}
	
        if (sprite1.gravity > 0)
            sprite1.physics.activeMovement(sprite1, new Direction(0,-1), 0);

        doBounce(sprite1, sprite2.rect, game);

        sprite1.setRect(sprite1.lastrect);
        sprite2.setRect(sprite2.lastrect);
    }


    public int executeBatch(VGDLSprite sprite1, ArrayList<VGDLSprite> sprite2list, Game game) {


        int nColls = super.sortBatch(sprite1, sprite2list, game);

        if(nColls == 1)
        {
            doBounce(sprite1, sprite2list.get(0).rect, game);
        }else{
            doBounce(sprite1, collision, game);
        }

        sprite1.setRect(sprite1.lastrect);
        for(VGDLSprite sprite2 : sprite2list)
            sprite2.setRect(sprite2.lastrect);

        return nColls;

    }


    private void doBounce(VGDLSprite sprite1, Rectangle s2rect, Game g)
    {
        boolean collisions[] = super.determineCollision(sprite1, s2rect, g);
        boolean horizontalBounce = collisions[0];
        boolean verticalBounce = collisions[1];

        if(verticalBounce)
        {
            sprite1.orientation = new Direction(sprite1.orientation.x(), -sprite1.orientation.y());
            return;
        }
        else if(horizontalBounce){
            sprite1.orientation = new Direction(-sprite1.orientation.x(), sprite1.orientation.y());
            return;
        }else{
            sprite1.orientation = new Direction(-sprite1.orientation.x(), -sprite1.orientation.y());
            return;
        }


    }


}