package ontology.effects.unary;

import core.vgdl.VGDLRegistry;
import core.vgdl.VGDLSprite;
import core.content.InteractionContent;
import core.game.Game;
import core.logging.Logger;
import core.logging.Message;
import ontology.effects.Effect;

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
    public String scoreChangeIfKilled;
    private String defScoreChange;

    public SubtractHealthPoints(InteractionContent cnt) throws Exception
    {
        is_kill_effect = true;
        limit = 0;
        value = 1;
        scoreChangeIfKilled = "0";
        this.parseParameters(cnt);
        if (!Objects.equals(stype, "")){
            itype = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stype);
            if(itype == -1){
        	throw new Exception("Undefined sprite " + stype);
            }
        }
        defScoreChange = scoreChange;
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
        else{
            if(sprite1 == null){
        	Logger.getInstance().addMessage(new Message(Message.WARNING, "1st sprite can't be EOS with SubtractHealthPoints interaction."));
        	return;
            }
        }
        s.healthPoints -= value;
        if(s.healthPoints <= limit)
        {
            //boolean variable set to false to indicate the sprite was not transformed
            game.killSprite(s, false);
            scoreChange = scoreChangeIfKilled;
        } else {
            scoreChange = defScoreChange;
        }
    }
}
