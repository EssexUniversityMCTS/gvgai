package ontology.effects.unary;

import core.vgdl.VGDLSprite;
import core.content.InteractionContent;
import core.game.Game;
import ontology.effects.Effect;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 04/11/13
 * Time: 15:57
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class KillIfNotUpright extends Effect
{
	
    public KillIfNotUpright(InteractionContent cnt)
    {
        is_kill_effect = true;
        this.parseParameters(cnt);
    }

    @Override
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game)
    {
    	double current_rotation = ((sprite1.rotation+2*Math.PI)%(2*Math.PI));
    	if (!(current_rotation < 5.0 && current_rotation > 4.4)){
    		game.killSprite(sprite1, false);
    	}
    }
}
