package ontology.effects.unary;

import core.VGDLRegistry;
import core.VGDLSprite;
import core.content.InteractionContent;
import core.game.Game;
import ontology.effects.Effect;
import ontology.sprites.producer.SpawnPoint;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 04/11/13
 * Time: 15:57
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class SubtractHealthPoints extends Effect
{
    public int value; //healthpoints removed from sprite1
    public String stype = "";
    public int itype = -1;
    public int limit; //kills sprite1 when less or equal to this value (default=0).

    public SubtractHealthPoints(InteractionContent cnt)
    {
        is_kill_effect = true;
        limit = 0;
        value = 1;
        this.parseParameters(cnt);
        if (!Objects.equals(stype, ""))
            itype = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stype);
    }

    @Override
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game)
    {
        VGDLSprite s = sprite1;
        if (itype != -1) {
            ArrayList<Integer> subtypes = game.getSubTypes(itype);
            for (Integer i : subtypes) {
                Iterator<VGDLSprite> spriteIt = game.getSpriteGroup(i);
                if (spriteIt != null) while (spriteIt.hasNext()) {
                    try {
                        s = spriteIt.next();
                        break;
                    } catch (ClassCastException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        s.healthPoints -= value;
        if(s.healthPoints <= limit)
        {
            //boolean variable set to false to indicate the sprite was not transformed
            game.killSprite(s, false);
        }
    }
}
