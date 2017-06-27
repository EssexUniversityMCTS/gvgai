package ontology.effects.binary;

import core.vgdl.VGDLSprite;
import core.content.InteractionContent;
import core.game.Game;
import core.logging.*;
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
    public double maxBounceAngleDeg;
    private double maxBounceAngleRad;

    public BounceDirection(InteractionContent cnt)
    {
        maxBounceAngleDeg = 60; //Default value
        this.parseParameters(cnt);
        maxBounceAngleRad = Math.toRadians(maxBounceAngleDeg);
    }

    @Override
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game)
    {
	if(sprite1 == null || sprite2 == null){
	    Logger.getInstance().addMessage(new Message(Message.WARNING, "Neither 1st not 2nd sprite can be EOS with BounceDirection interaction."));
	    return;
	}
	
        //We need the actual intersection:
        Rectangle interRect = sprite1.rect.intersection(sprite2.rect);
        double padLenght = sprite2.rect.height;
        double diff, travelDir;
        Vector2d vel = sprite1._velocity();

//        boolean verticalBounce = interRect.width > interRect.height;
//        boolean horizontalBounce = interRect.width < interRect.height;

        double distX =  Math.min(Math.abs (sprite1.lastrect.x - (sprite2.rect.x + sprite2.rect.width)) ,
                Math.abs ((sprite1.lastrect.x + sprite1.rect.width) - sprite2.rect.x));

        double distY =  Math.min(Math.abs (sprite1.lastrect.y - (sprite2.rect.y + sprite2.rect.height)) ,
                Math.abs ((sprite1.lastrect.y + sprite1.rect.height) - sprite2.rect.y));


        double tX = Math.abs(distX / vel.x);
        double tY = Math.abs(distY / vel.y);
        boolean horizontalBounce = (tX < tY);
        boolean verticalBounce = (tY < tX);

        if(verticalBounce)
        {
            //Bouncing vertically
            padLenght = sprite2.rect.width;
            diff = sprite1.rect.getCenterX() - sprite2.rect.getCenterX();
            travelDir = (sprite1.rect.getCenterY() < sprite2.rect.getCenterY())? 1 : -1;
        }
        else if(horizontalBounce){
            diff = sprite2.rect.getCenterY()  - sprite1.rect.getCenterY();
            travelDir = (sprite1.rect.getCenterX() > sprite2.rect.getCenterX())? 1 : -1;
        }else{
            sprite1.orientation = new Direction(-sprite1.orientation.x(), sprite1.orientation.y());
            //System.out.println("DIAGONAL");
            return;
        }

        //Calculate bouncing angle relative to the position where sprite1 hit sprite2.
        double relHit = diff / (0.5 * padLenght);
        if(relHit < 0) relHit = Math.max(relHit, -1); else relHit = Math.min(relHit, 1); //Keeping it in [-1,1]
        double bounceAngle = relHit * maxBounceAngleRad;

        double xDir, yDir;
        if(verticalBounce)
        {
            xDir = Math.sin(bounceAngle);
            yDir = -Math.cos(bounceAngle) * travelDir;
        }else{
            xDir = Math.cos(bounceAngle) * travelDir;
            yDir = -Math.sin(bounceAngle);
        }

        //Assign new orientation with the calculated new direction.
        Vector2d outDir = new Vector2d(xDir, yDir);
        outDir.normalise();
        sprite1.orientation = new Direction(outDir.x, outDir.y);
    }

}