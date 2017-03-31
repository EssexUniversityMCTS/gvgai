package ontology.effects.unary;

import core.vgdl.VGDLRegistry;
import core.vgdl.VGDLSprite;
import core.content.InteractionContent;
import core.game.Game;
import core.logging.Logger;
import core.logging.Message;
import ontology.effects.Effect;
import tools.Vector2d;

import java.util.ArrayList;

public class SpawnBehind extends Effect {

    public String stype;
    public int itype;

    public SpawnBehind(InteractionContent cnt) throws Exception
    {
        this.parseParameters(cnt);
        itype = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stype);
        if(itype == -1){
            String[] className = this.getClass().getName().split("\\.");
            throw new Exception("[" + className[className.length - 1] + "] Undefined sprite " + stype);
        }
    }

    @Override
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game)
    {
	if(sprite2 == null){
            String[] className = this.getClass().getName().split("\\.");
            Logger.getInstance().addMessage(new Message(Message.WARNING, "[" + className[className.length - 1] + "] sprite2 is null."));
            return;
        }
        if(game.getRandomGenerator().nextDouble() >= prob) return;
        Vector2d lastPos = sprite2.getLastPosition();
        if (lastPos != null) {
            game.addSprite(itype, lastPos);
        }
    }
    
    @Override
    public ArrayList<String> getEffectSprites(){
    	ArrayList<String> result = new ArrayList<String>();
    	if(stype!=null) result.add(stype);
    	
    	return result;
    }
}
