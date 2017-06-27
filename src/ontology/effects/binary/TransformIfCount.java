package ontology.effects.binary;

import core.vgdl.VGDLRegistry;
import core.vgdl.VGDLSprite;
import core.content.InteractionContent;
import core.game.Game;
import core.logging.Logger;
import core.logging.Message;
import ontology.effects.unary.TransformTo;

import java.util.ArrayList;

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

    public TransformIfCount(InteractionContent cnt) throws Exception
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
	if(sprite1 == null || sprite2 == null){
	    Logger.getInstance().addMessage(new Message(Message.WARNING, "Neither the 1st nor 2nd sprite can be EOS with TransformIfCount interaction."));
	    return;
	}
	
        int numSpritesCheck = game.getNumSprites(itypeCount);
        this.applyScore = false;
        this.count = false;
        this.countElse = false;
        if(numSpritesCheck <= leq && numSpritesCheck >= geq)
        {
            VGDLSprite newSprite = game.addSprite(itype, sprite1.getPosition(), true);
            super.transformTo(newSprite, sprite1, sprite2, game);
            this.applyScore = true;
            this.count = true;
        } else if (estype != null) {
            VGDLSprite newSprite = game.addSprite(eitype, sprite1.getPosition(), true);
            super.transformTo(newSprite, sprite1, sprite2, game);
            this.countElse=true;
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
