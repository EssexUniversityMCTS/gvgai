package ontology.effects.unary;

import core.VGDLRegistry;
import core.VGDLSprite;
import core.content.InteractionContent;
import core.game.Game;
import ontology.effects.Effect;

import java.util.ArrayList;

/**
 * Created by Diego on 18/02/14.
 */
public class Spawn extends Effect {

    public String stype;
    public int itype;

    public Spawn(InteractionContent cnt)
    {
        this.parseParameters(cnt);
        itype = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stype);
    }

    @Override
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game)
    {
        if(game.getRandomGenerator().nextDouble() >= prob) return;
        game.addSprite(itype, sprite1.getPosition());
    }
    
    @Override
    public ArrayList<String> getEffectSprites(){
    	ArrayList<String> result = new ArrayList<String>();
    	if(stype!=null) result.add(stype);
    	
    	return result;
    }
}
