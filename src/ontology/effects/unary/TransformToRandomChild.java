package ontology.effects.unary;

import core.vgdl.VGDLRegistry;
import core.vgdl.VGDLSprite;
import core.content.InteractionContent;
import core.game.Game;
import core.logging.Logger;
import core.logging.Message;
import tools.Utils;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 23/10/13
 * Time: 15:21
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class TransformToRandomChild extends TransformTo {

    public TransformToRandomChild(InteractionContent cnt) throws Exception
    {
        super(cnt);
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
	
        ArrayList<Integer> subtypes = game.getSubTypes(itype);
        if (!subtypes.isEmpty()) {
            int[] types = new int[subtypes.size()-1];
            int j = -1;
            for (Integer i : subtypes) {
                if (i != itype) {
                    types[++j] = i;
                }
            }

            VGDLSprite newSprite = game.addSprite(Utils.choice(types, game.getRandomGenerator()), sprite1.getPosition());
            transformTo(newSprite, sprite1, sprite2, game);
        }
    }

    @Override
    public ArrayList<String> getEffectSprites(){
    	ArrayList<String> result = new ArrayList<String>();
    	if(stype!=null) result.add(stype);
    	
    	return result;
    }
}
