package ontology.effects.unary;

import core.VGDLRegistry;
import core.VGDLSprite;
import core.content.InteractionContent;
import core.game.Game;
import ontology.effects.Effect;
import ontology.sprites.producer.BomberRandomMissile;

import java.util.ArrayList;

public class UpdateMissile extends Effect {

    public String stype; //new missile to replace sprite2
    public String bomber; //stype string of bomber
    public int itype, ibomber;

    public UpdateMissile(InteractionContent cnt)
    {
        this.parseParameters(cnt);
        itype = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stype);
        ibomber = VGDLRegistry.GetInstance().getRegisteredSpriteValue(bomber);
    }

    @Override
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game)
    {
        VGDLSprite bombr = game.getSpriteGroup(ibomber).next();
        try {
            BomberRandomMissile b = (BomberRandomMissile)bombr;
            b.updateItype(sprite2.getType(),itype);
        }
        catch (ClassCastException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public ArrayList<String> getEffectSprites(){
    	ArrayList<String> result = new ArrayList<String>();
    	if(stype!=null) result.add(stype);
    	
    	return result;
    }
}
