package ontology.effects.binary;

import core.VGDLRegistry;
import core.VGDLSprite;
import core.content.InteractionContent;
import core.game.Game;
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

        if(sprite1.is_resource)
        {
            Resource r = (Resource) sprite1;

            //Check if we have the secondary resource first
            int numResourcesHeld = sprite2.getAmountResource(heldResourceId);
            if(numResourcesHeld < value)
                return;

            int numResources = sprite2.getAmountResource(r.resource_type);
            if(numResources + r.value <= game.getResourceLimit(r.resource_type))
            {
                sprite2.modifyResource(r.resource_type, r.value);
            }

            if(killResource)
                game.killSprite(sprite1);
        }
    }
}
