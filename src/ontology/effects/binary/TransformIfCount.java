package ontology.effects.binary;

import core.VGDLRegistry;
import core.VGDLSprite;
import core.content.InteractionContent;
import core.game.Game;
import ontology.effects.unary.TransformTo;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 23/10/13
 * Time: 15:21
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class TransformIfCount extends TransformTo {

    //This effect transforms sprite1 into stype if
    // * num(stypeCount) >= GEQ
    // * num(stypeCount) <= LEQ
    public String stypeCount;
    public int itypeCount;
    public String estype;
    public int eitype;
    public int geq;
    public int leq;

    public TransformIfCount(InteractionContent cnt)
    {
        super(cnt);
        geq=0;
        leq=Game.getMaxSprites();
        this.parseParameters(cnt);
        itypeCount = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stypeCount);

        if(estype != null)
            eitype = VGDLRegistry.GetInstance().getRegisteredSpriteValue(estype);
    }

    @Override
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game)
    {
        int numSpritesCheck = game.getNumSprites(itypeCount);
        this.applyScore = false;
        this.count = false;
        if(numSpritesCheck <= leq && numSpritesCheck >= geq)
        {
            VGDLSprite newSprite = game.addSprite(itype, sprite1.getPosition(), true);
            super.transformTo(newSprite, sprite1, sprite2, game);
            this.applyScore = true;
            this.count = true;
        } else if (estype != null) {
            VGDLSprite newSprite = game.addSprite(eitype, sprite1.getPosition(), true);
            super.transformTo(newSprite, sprite1, sprite2, game);
        }
    }
    
    @Override
    public ArrayList<String> getEffectSprites(){
    	ArrayList<String> result = new ArrayList<String>();
        if(stype!=null) result.add(stype);
        if(stypeCount!=null) result.add(stypeCount);
        if(estype!=null) result.add(estype);
    	
    	return result;
    }
}
