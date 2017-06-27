package ontology.effects.unary;

import core.vgdl.VGDLRegistry;
import core.vgdl.VGDLSprite;
import core.content.InteractionContent;
import core.game.Game;
import core.logging.Logger;
import core.logging.Message;
import ontology.effects.Effect;

import java.util.ArrayList;

/**
 * Created by Diego on 18/02/14.
 */
public class SpawnIfCounterSubTypes extends Effect {

    public String stype; //sprite to spawn
    public int itype;
    public String estype; //sprite to spawn if condition not met
    public int eitype;
    public String stypeCount; //sprite to count
    public int citype;
    public int subTypesNum=-1; // number of subtypes
    public int limit; //number of total sprites

    public SpawnIfCounterSubTypes(InteractionContent cnt) throws Exception
    {
        this.parseParameters(cnt);
        eitype = VGDLRegistry.GetInstance().getRegisteredSpriteValue(estype);
        if(eitype == -1){
            throw new Exception("Undefined sprite " + estype);
        }
        citype = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stypeCount);
        if(citype == -1){
            throw new Exception("Undefined sprite " + stypeCount);
        }
        itype = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stype);
        if(itype == -1){
            throw new Exception("Undefined sprite " + stype);
        }
    }

    @Override
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game)
    {
	if(sprite1 == null){
	    Logger.getInstance().addMessage(new Message(Message.WARNING, "1st sprite can't be EOS with SpawnIfCounterSubTypes interaction."));
	    return;
	}
	
        applyScore = false;
        count=false;

        int countAcum = 0;

        if(citype != -1) countAcum += game.getNumSprites(citype) - game.getNumDisabledSprites(citype);

        if(countAcum == limit) {

            ArrayList<Integer> subtypes = game.getSubTypes(citype);
            int countAcumSubTypes = 0;
            for (Integer subtype : subtypes) {

                int count = (game.getNumSprites(subtype) - game.getNumDisabledSprites(subtype));
                if(count > 0)
                {
                    if(game.getSpriteGroup(subtype) != null) //This avoids non-terminal types
                    {
                        countAcumSubTypes += count > 0 ? 1 : 0;
                    }
                }
            }

            countAcumSubTypes /= 2;
            if(countAcumSubTypes == subTypesNum) {
                game.addSprite(itype, sprite1.getPosition());
                applyScore = true;
                count=true;
            } else {
                game.addSprite(eitype, sprite1.getPosition());
            }
        } else {
            game.addSprite(eitype, sprite1.getPosition());
        }
    }
    
    @Override
    public ArrayList<String> getEffectSprites(){
    	ArrayList<String> result = new ArrayList<String>();
    	if(stype!=null) result.add(stype);
    	
    	return result;
    }
}
