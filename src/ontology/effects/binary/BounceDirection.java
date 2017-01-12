package ontology.effects.binary;

import core.VGDLSprite;
import core.content.InteractionContent;
import core.game.Game;
import ontology.Types;
import ontology.effects.Effect;
import tools.Direction;
import tools.Vector2d;

import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 23/10/13
 * Time: 15:23
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class BounceDirection extends Effect
{
    public boolean pixelPerfect;

    public BounceDirection(InteractionContent cnt)
    {
        pixelPerfect = false;
        this.parseParameters(cnt);
    }

    @Override
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game)
    { 
        sprite1.setRect(sprite1.lastrect);
        Direction inc = sprite1.orientation;
        Vector2d snorm = new Vector2d((-sprite1.rect.getCenterX() + sprite2.rect.getCenterX()), (-sprite1.rect.getCenterY() + sprite2.rect.getCenterY()));
        snorm.normalise();
        Double dp = snorm.x * inc.x() + snorm.y * inc.y();
        sprite1.orientation = new Direction(-2 * dp * snorm.x + inc.x(), -2 * dp * snorm.y + inc.y());
        //sprite1.speed *= (1. - sprite1.friction);
    }

    private Rectangle calculatePixelPerfect(VGDLSprite sprite1, VGDLSprite sprite2)
    {
        Vector2d sprite1v = new Vector2d(sprite1.rect.getCenterX() - sprite1.lastrect.getCenterX(),
                sprite1.rect.getCenterY() - sprite1.lastrect.getCenterY());

        sprite1v.normalise();
        Direction sprite1Dir = new Direction(sprite1v.x, sprite1v.y);

        if(sprite1Dir.equals(Types.DDOWN))
        {
            int overlay = (sprite1.rect.y + sprite1.rect.height) - sprite2.rect.y;
            return new Rectangle(sprite1.rect.x, sprite1.rect.y - overlay,
                    sprite1.rect.width, sprite1.rect.height);
        }
        else if(sprite1Dir.equals(Types.DRIGHT))
        {
            int overlay = (sprite1.rect.x + sprite1.rect.width) - sprite2.rect.x;
            return new Rectangle(sprite1.rect.x - overlay, sprite1.rect.y,
                    sprite1.rect.width, sprite1.rect.height);
        }
        else if(sprite1Dir.equals(Types.DUP))
        {
            return new Rectangle(sprite1.rect.x, sprite2.rect.y + sprite2.rect.height,
                    sprite1.rect.width, sprite1.rect.height);
        }
        else if(sprite1Dir.equals(Types.DLEFT))
        {
            return new Rectangle(sprite2.rect.x + sprite2.rect.width, sprite1.rect.y,
                    sprite1.rect.width, sprite1.rect.height);
        }


        return sprite1.lastrect;

    }
}