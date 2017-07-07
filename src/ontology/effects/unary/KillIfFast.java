package ontology.effects.unary;

import core.vgdl.VGDLSprite;
import core.content.InteractionContent;
import core.game.Game;
import core.logging.Logger;
import core.logging.Message;
import ontology.effects.Effect;
import tools.Vector2d;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 04/11/13
 * Time: 15:57
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class KillIfFast extends Effect
{
	public double limspeed;

    public KillIfFast(InteractionContent cnt)
    {
        is_kill_effect = true;
		limspeed = 1.0;
        this.parseParameters(cnt);
    }

    @Override
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game)
    {
	if(sprite1 == null || sprite2 == null){
            Logger.getInstance().addMessage(new Message(Message.WARNING, "Neither 1st not 2nd sprite can be EOS with KillIfFast interaction."));
            return;
        }
    	double relspeed = 0.0;
    	if (sprite1.is_static){
    		relspeed = sprite2.speed;
    	}
    	else if (sprite2.is_static){
    		relspeed = sprite1.speed;
    	}
    	else{
    		double vvx = sprite1.orientation.x() - sprite2.orientation.x();
    		double vvy = sprite1.orientation.y() - sprite2.orientation.y();
    		Vector2d vv = new Vector2d(vvx,vvy);
    		relspeed = vv.mag();
    		}
    	if (relspeed > limspeed){
    		game.killSprite(sprite1, false);
    	}
    }
}
