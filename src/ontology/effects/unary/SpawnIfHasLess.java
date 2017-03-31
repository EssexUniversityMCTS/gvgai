package ontology.effects.unary;

import java.util.ArrayList;

import core.vgdl.VGDLRegistry;
import core.vgdl.VGDLSprite;
import core.content.InteractionContent;
import core.game.Game;
import core.logging.Logger;
import core.logging.Message;
import ontology.effects.Effect;

/**
 * Created by Diego on 18/02/14.
 */
public class SpawnIfHasLess extends Effect {

    public String resource;
    public int resourceId;
    public int limit;
    public String stype;
    public int itype;

    public SpawnIfHasLess(InteractionContent cnt) throws Exception
    {
        resourceId = -1;
        this.parseParameters(cnt);
        resourceId = VGDLRegistry.GetInstance().getRegisteredSpriteValue(resource);
        if(resourceId == -1){
            String[] className = this.getClass().getName().split(".");
            throw new Exception("[" + className[className.length - 1] + "] Undefined sprite " + resource);
        }
        itype = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stype);
        if(itype == -1){
            String[] className = this.getClass().getName().split(".");
            throw new Exception("[" + className[className.length - 1] + "] Undefined sprite " + stype);
        }
    }

    @Override
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game)
    {
	if(sprite1 == null){
            String[] className = this.getClass().getName().split(".");
            Logger.getInstance().addMessage(new Message(Message.WARNING, "[" + className[className.length - 1] + "] sprite1 is null."));
            return;
        }
	
        applyScore = false;

        if(game.getRandomGenerator().nextDouble() >= prob) return;

        if(sprite1.getAmountResource(resourceId) <= limit)
        {
            game.addSprite(itype, sprite1.getPosition());
            applyScore = true;
        }
    }
    
    @Override
    public ArrayList<String> getEffectSprites(){
    	ArrayList<String> result = new ArrayList<String>();
    	if(stype!=null) result.add(stype);
    	
    	return result;
    }
}
