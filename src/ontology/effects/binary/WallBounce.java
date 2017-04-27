package ontology.effects.binary;

import core.vgdl.VGDLSprite;
import core.content.InteractionContent;
import core.game.Game;
import ontology.Types;
import ontology.effects.Effect;
import tools.Direction;
import tools.Vector2d;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 23/10/13
 * Time: 15:23
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class WallBounce extends Effect
{
    public WallBounce(InteractionContent cnt)
    {
        super.inBatch = true;
        this.parseParameters(cnt);
    }

    @Override
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game)
    {

        if (sprite1.gravity > 0)
            sprite1.physics.activeMovement(sprite1, new Direction(0,-1), 0);


        doBounce(sprite1, sprite2.rect, game);

        sprite1.setRect(sprite1.lastrect);
        sprite2.setRect(sprite2.lastrect);
    }

    private void doBounce(VGDLSprite sprite1, Rectangle s2rect, Game g)
    {

        Rectangle intersec = sprite1.rect.intersection(s2rect);
        boolean horizontalBounce = (sprite1.rect.height == intersec.height);
        boolean verticalBounce =   (sprite1.rect.width == intersec.width);

        if(!horizontalBounce && !verticalBounce)
        {
            Vector2d vel = sprite1._velocity();

            //Distance on X, according to the direction of travel
            double distX = (vel.x == 0.0) ?  Math.abs (sprite1.lastrect.x - s2rect.x) :                         //Travelling vertically
                           ((vel.x > 0.0) ?  Math.abs ((sprite1.lastrect.x + sprite1.rect.width) - s2rect.x) :  //Going right
                                             Math.abs ((s2rect.x + s2rect.width) - sprite1.lastrect.x));        //Going left


            //Distance on Y, according to the direction of travel
            double distY =  (vel.y == 0.0) ?  Math.abs (sprite1.lastrect.y - s2rect.y) :                          //Travelling laterally
                            ((vel.y > 0.0) ?  Math.abs ((sprite1.lastrect.y + sprite1.rect.height) - s2rect.y) :  //Going downwards
                                              Math.abs (sprite1.lastrect.y - (s2rect.y + s2rect.height)));        //Going upwards


            double tX = Math.abs(distX / vel.x);
            double tY = Math.abs(distY / vel.y);
            horizontalBounce = (tX < tY);
            verticalBounce = (tY < tX);
        }

        if(verticalBounce)
        {
            sprite1.orientation = new Direction(sprite1.orientation.x(), -sprite1.orientation.y());
            return;
        }
        else if(horizontalBounce){
            sprite1.orientation = new Direction(-sprite1.orientation.x(), sprite1.orientation.y());
            return;
        }else{
            sprite1.orientation = new Direction(-sprite1.orientation.x(), -sprite1.orientation.y());
            return;
        }


    }

    public int executeBatch(VGDLSprite sprite1, ArrayList<VGDLSprite> sprite2list, Game game) {

        if(sprite2list.size() == 1) {
            execute(sprite1, sprite2list.get(0), game);
            return 1;

        }else if(sprite2list.size() > 2)
        {
            //More than 2, sort by proximity and return the first.
            sortByProximity(sprite1, sprite2list);
            execute(sprite1, sprite2list.get(0), game);
            return 1;
        }

        //Else, do a cleverer collision with a wall of 2
        VGDLSprite b1 = sprite2list.get(0);
        VGDLSprite b2 = sprite2list.get(1);

        Rectangle big;
        if(b1.rect.getCenterX() == b2.rect.getCenterX())
        {
            boolean b1Above = b1.rect.getY() < b2.rect.getY();

            double x = b1.rect.getX();
            double y = b1Above ? b1.rect.getY() : b2.rect.getY();
            double w = b1.rect.getWidth();
            double h = b1.rect.getHeight() * 2;
            big = new Rectangle((int)x,(int)y,(int)w,(int)h);

        }else if (b1.rect.getCenterY() == b2.rect.getCenterY())
        {
            boolean b1Left = b1.rect.getX() < b2.rect.getX();

            double x = b1Left ? b1.rect.getX() : b2.rect.getX();
            double y = b1.rect.getY();
            double w = b1.rect.getWidth() * 2;
            double h = b1.rect.getHeight();
            big = new Rectangle((int)x,(int)y,(int)w,(int)h);
        }else {
            //Not aligned, better to use the closest one.
            sortByProximity(sprite1, sprite2list);
            execute(sprite1, sprite2list.get(0), game);
            return 1;
        }

        //System.out.println("Colliding with 2");
        if (sprite1.gravity > 0)
            sprite1.physics.activeMovement(sprite1, new Direction(0,-1), 0);

        doBounce(sprite1, big, game);

        sprite1.setRect(sprite1.lastrect);
        for(VGDLSprite sprite2 : sprite2list)
            sprite2.setRect(sprite2.lastrect);

        return 2;

    }

    private void sortByProximity(VGDLSprite sprite1, ArrayList<VGDLSprite> sprite2list)
    {
        final VGDLSprite spCompare = sprite1;
        final Vector2d spCompareCenter = new Vector2d(sprite1.lastrect.getCenterX(), sprite1.lastrect.getCenterY());
        Collections.sort(sprite2list, new Comparator<VGDLSprite>() {
            @Override
            public int compare(VGDLSprite o1, VGDLSprite o2) {

                Vector2d s1Center = new Vector2d(o1.lastrect.getCenterX(), o1.lastrect.getCenterY());
                Vector2d s2Center = new Vector2d(o2.lastrect.getCenterX(), o2.lastrect.getCenterY());

                if(spCompareCenter.dist(s1Center) < spCompareCenter.dist(s2Center))  		return -1;
                else if(spCompareCenter.dist(s1Center) > spCompareCenter.dist(s2Center))	return 1;
                return 0;
            }
        });
    }


}