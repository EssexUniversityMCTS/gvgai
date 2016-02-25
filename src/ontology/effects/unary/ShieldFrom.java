package ontology.effects.unary;

import core.VGDLRegistry;
import core.VGDLSprite;
import core.content.InteractionContent;
import core.game.Game;
import ontology.Types;
import ontology.avatar.MovingAvatar;
import ontology.effects.Effect;

import java.awt.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 23/10/13
 * Time: 15:21
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class ShieldFrom extends Effect {


    public String stype;
    public int istype;

    public String ftype;
    public long iftype;


    public ShieldFrom(InteractionContent cnt)
    {
        this.parseParameters(cnt);
        istype = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stype);
        iftype = ftype.hashCode();
    }

    @Override
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game)
    {
        game.addShield(sprite1.getType(), istype, iftype);
    }

}
