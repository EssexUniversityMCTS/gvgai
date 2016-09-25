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
public class SpawnIfCounterSubTypes extends Effect {

    public String stype; //sprite to spawn
    public int itype;
    public String estype; //sprite to spawn if condition not met
    public int eitype;
    public String stypeCount; //sprite to count
    public int citype;
    public int subTypesNum=-1; // number of subtypes
    public int limit; //number of total sprites

    public SpawnIfCounterSubTypes(InteractionContent cnt)
    {
        this.parseParameters(cnt);
        eitype = VGDLRegistry.GetInstance().getRegisteredSpriteValue(estype);
        citype = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stypeCount);
        itype = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stype);
    }

    @Override
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game)
    {
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
