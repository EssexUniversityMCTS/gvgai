package ontology.effects.binary;

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

        if(sprite1.is_resource)
        {
            Resource r = (Resource) sprite1;

            int numResources = sprite2.getAmountResource(r.resource_type);
            if(numResources + r.value <= game.getResourceLimit(r.resource_type))
            {
                sprite2.modifyResource(r.resource_type, r.value);

                if(killResource)
                    game.killSprite(sprite1);
            }

        }
    }
}
