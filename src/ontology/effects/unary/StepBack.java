package ontology.effects.unary;

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

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 23/10/13
 * Time: 15:23
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class StepBack extends Effect
{
    public boolean pixelPerfect;

    public StepBack(InteractionContent cnt)
    {
        pixelPerfect = false;
        this.parseParameters(cnt);
    }

    @Override
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game)
    {
	if(sprite1 == null){
	    Logger.getInstance().addMessage(new Message(Message.WARNING, "1st sprite can't be EOS with StepBack interaction."));
	    return;
	}
	
        if(pixelPerfect && sprite2!=null) //Sprite2 could be Null in an EOS case.
            sprite1.setRect(calculatePixelPerfect(sprite1, sprite2));
        else
            sprite1.setRect(sprite1.lastrect);
    }

}
