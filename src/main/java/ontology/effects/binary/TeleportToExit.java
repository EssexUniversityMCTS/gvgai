package ontology.effects.binary;

import core.VGDLFactory;
import core.VGDLSprite;
import core.content.InteractionContent;
import core.game.Game;
import ontology.Types;
import ontology.effects.Effect;
import ontology.physics.ContinuousPhysics;
import ontology.physics.GridPhysics;
import tools.Utils;
import tools.Vector2d;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.TreeMap;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 04/11/13
 * Time: 15:56
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class TeleportToExit extends Effect
{

    public TeleportToExit(InteractionContent cnt)
    {
        this.parseParameters(cnt);
    }

    @Override
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game)
    {
        int destinationId = VGDLFactory.GetInstance().requestFieldValueInt(sprite2, "itype");

        Collection<VGDLSprite> sprites = game.getSprites(destinationId).values();
        VGDLSprite destination = (VGDLSprite) Utils.choice(sprites.toArray(), game.getRandomGenerator());

        sprite1.setRect(destination.rect);

        sprite1.lastmove = 0;
    }
}
