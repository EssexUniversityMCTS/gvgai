package ontology.effects.binary;

import core.vgdl.VGDLSprite;
import core.content.InteractionContent;
import core.game.Game;
import core.logging.Logger;
import core.logging.Message;
import ontology.effects.Effect;
import ontology.sprites.Resource;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 04/11/13
 * Time: 13:25
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class CollectResource extends Effect
{

    public boolean killResource;

    public CollectResource(InteractionContent cnt)
    {
        killResource = true;
        this.parseParameters(cnt);
        is_kill_effect = killResource;
    }

    @Override
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game) {
	if(sprite1 == null || sprite2 == null){
	    Logger.getInstance().addMessage(new Message(Message.WARNING, "Neither the 1st nor 2nd sprite can be EOS with CollectResource interaction."));
	    return;
	}
	
        if(sprite1.is_resource)
        {
            Resource r = (Resource) sprite1;
            applyScore=false;
            int numResources = sprite2.getAmountResource(r.resource_type);

            if(numResources < game.getResourceLimit(r.resource_type))
            {
                int topup = Math.min(r.value, game.getResourceLimit(r.resource_type) - numResources);
                applyScore=true;
                sprite2.modifyResource(r.resource_type, topup);

                if(killResource)
                    //boolean variable set to false to indicate the sprite was not transformed
                    game.killSprite(sprite1, true);
            }

        }
    }
}
