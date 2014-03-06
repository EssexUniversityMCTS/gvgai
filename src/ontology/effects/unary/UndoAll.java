package ontology.effects.unary;

import core.VGDLSprite;
import core.content.InteractionContent;
import core.game.Game;
import ontology.effects.Effect;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 23/10/13
 * Time: 15:23
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class UndoAll extends Effect
{
    public UndoAll(InteractionContent cnt)
    {
        this.parseParameters(cnt);
    }

    @Override
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game)
    {
        int[] gameSpriteOrder = game.getSpriteOrder();
        int spriteOrderCount = gameSpriteOrder.length;
        for(int i = 0; i < spriteOrderCount; ++i)
        {
            int spriteTypeInt = gameSpriteOrder[i];
            ArrayList<VGDLSprite> sprites = game.getSpriteGroup(spriteTypeInt);
            if(sprites != null)
            {
                int numSprites = sprites.size();
                for(int j = 0; j < numSprites; j++)
                {
                    VGDLSprite sp = sprites.get(j);
                    sp.rect = sp.lastrect;
                }
            }
        }
    }
}
