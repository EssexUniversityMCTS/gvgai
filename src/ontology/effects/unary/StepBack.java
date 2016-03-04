package ontology.effects.unary;

import core.VGDLSprite;
import core.content.InteractionContent;
import core.game.Game;
import ontology.Types;
import ontology.effects.Effect;
import tools.Vector2d;

import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 23/10/13
 * Time: 15:23
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class StepBack extends Effect
{
    public boolean pixelPerfect;

    public StepBack(InteractionContent cnt)
    {
        pixelPerfect = false;
        this.parseParameters(cnt);
    }

    @Override
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game)
    {
        if(pixelPerfect)
            sprite1.setRect(calculatePixelPerfect(sprite1, sprite2));
        else
            sprite1.setRect(sprite1.lastrect);
    }

    private Rectangle calculatePixelPerfect(VGDLSprite sprite1, VGDLSprite sprite2)
    {
        Vector2d sprite1Dir = new Vector2d(sprite1.rect.getCenterX() - sprite1.lastrect.getCenterX(),
                                           sprite1.rect.getCenterY() - sprite1.lastrect.getCenterY());

        sprite1Dir.normalise();

        if(sprite1Dir.equals(Types.DOWN))
        {
            int overlay = (sprite1.rect.y + sprite1.rect.height) - sprite2.rect.y;
            return new Rectangle(sprite1.rect.x, sprite1.rect.y - overlay,
                    sprite1.rect.width, sprite1.rect.height);
        }
        else if(sprite1Dir.equals(Types.RIGHT))
        {
            int overlay = (sprite1.rect.x + sprite1.rect.width) - sprite2.rect.x;
            return new Rectangle(sprite1.rect.x - overlay, sprite1.rect.y,
                    sprite1.rect.width, sprite1.rect.height);
        }
        else if(sprite1Dir.equals(Types.UP))
        {
            return new Rectangle(sprite1.rect.x, sprite2.rect.y + sprite2.rect.height,
                    sprite1.rect.width, sprite1.rect.height);
        }
        else if(sprite1Dir.equals(Types.LEFT))
        {
            return new Rectangle(sprite2.rect.x + sprite2.rect.width, sprite1.rect.y,
                    sprite1.rect.width, sprite1.rect.height);
        }


        return sprite1.lastrect;

    }
}
