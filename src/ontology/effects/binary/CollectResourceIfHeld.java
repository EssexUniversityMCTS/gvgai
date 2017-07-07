package ontology.effects.binary;

import core.vgdl.VGDLRegistry;
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
public class CollectResourceIfHeld extends Effect
{
    public boolean killResource; //Only if the resource is collected.
    public String heldResource;
    public int heldResourceId;
    public int value;

    public CollectResourceIfHeld(InteractionContent cnt)
    {
        value = 1;
        killResource = true;
        this.parseParameters(cnt);
        is_kill_effect = killResource;
        heldResourceId = VGDLRegistry.GetInstance().getRegisteredSpriteValue(heldResource);

    }

    @Override
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game) {
	if(sprite1 == null || sprite2 == null){
	    Logger.getInstance().addMessage(new Message(Message.WARNING, "Neither the 1st nor 2nd sprite can be EOS with CollectResourceIfHeld interaction."));
	    return;
	}
	
        if(sprite1.is_resource)
        {
            Resource r = (Resource) sprite1;
            applyScore=false;

            //Check if we have the secondary resource first
            int numResourcesHeld = sprite2.getAmountResource(heldResourceId);
            if(numResourcesHeld < value)
                return;

            int numResources = sprite2.getAmountResource(r.resource_type);
            if(numResources + r.value <= game.getResourceLimit(r.resource_type))
            {
                applyScore=true;
                sprite2.modifyResource(r.resource_type, r.value);
            }

            if(killResource)
                //boolean variable set to false to indicate the sprite was not transformed
                game.killSprite(sprite1, false);
        }
    }
}
