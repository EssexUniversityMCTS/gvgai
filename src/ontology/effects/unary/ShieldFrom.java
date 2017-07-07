package ontology.effects.unary;

import core.vgdl.VGDLRegistry;
import core.vgdl.VGDLSprite;
import core.content.InteractionContent;
import core.game.Game;
import core.logging.Logger;
import core.logging.Message;
import ontology.effects.Effect;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 23/10/13
 * Time: 15:21
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class ShieldFrom extends Effect {


    public String stype;
    public int istype;

    public String ftype;
    public long iftype;


    public ShieldFrom(InteractionContent cnt) throws Exception
    {
        this.parseParameters(cnt);
        istype = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stype);
        if(istype == -1){
            throw new Exception("Undefined sprite " + stype);
        }
        iftype = ftype.hashCode();
    }

    @Override
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game)
    {
	if(sprite1 == null){
	    Logger.getInstance().addMessage(new Message(Message.WARNING, "1st sprite can't be EOS with ShieldFrom interaction."));
	    return;
	}
	
        game.addShield(sprite1.getType(), istype, iftype);
    }

}
