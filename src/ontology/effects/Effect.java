package ontology.effects;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import core.vgdl.VGDLFactory;
import core.vgdl.VGDLSprite;
import core.content.InteractionContent;
import core.game.Game;
import core.logging.Logger;
import core.logging.Message;
import ontology.Types;
import tools.Direction;
import tools.Vector2d;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 23/10/13
 * Time: 15:20
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public abstract class Effect{

    //Indicates if this effect kills any sprite
    public boolean is_kill_effect = false;

    //Indicates if this effect has some random element.
    public boolean is_stochastic = false;

    // indicates whether the interactions of this effect should be carried out sequentially or simultaneously
    public boolean sequential = false;

    //Change of the score this effect makes.
    public String scoreChange = "0";

    //Count something
    public boolean count = true;
    public String counter = "0";
    
    //Count something else
    public boolean countElse = true;
    public String counterElse = "0";

    //Probabilty for stochastic effects.
    public double prob = 1;

    //Indicates if this effects changes the score.
    public boolean applyScore = true;

    //Indicates the number of repetitions of this effect. This affects how many times this
    // effect is taken into account at each step. This is useful for chain effects (i.e. pushing
    // boxes in a chain - thecitadel, enemycitadel).
    public int repeat = 1;

    /**
     * 'Unique' hashcode for this effect
     */
    public long hashCode;

    /**
     * Indicates if this effect is enabled or not (default: true)
     */
    public boolean enabled;

    /**
     * Indicates if the effect wishes to take into account all sprites of the second type at once.
     */
    public boolean inBatch = false;

    /**
     * Collision for batches
     */
    protected Rectangle collision;

    /**
     * Executes the effect
     *
     * @param sprite1 first sprite of the collision
     * @param sprite2 second sprite of the collision
     * @param game    reference to the game object with the current state.
     */
    public abstract void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game);


    /**
     * Executes the effect to all second sprites at once.
     *
     * @param sprite1       first sprite of the collision
     * @param sprite2list   list of all second sprites of the collision
     * @param game          reference to the game object with the current state.
     * @return the number of sprites considered in the collision
     */
    public int executeBatch(VGDLSprite sprite1, ArrayList<VGDLSprite> sprite2list, Game game) {return -1;}

    /**
     * Takes a batch of sprites to collide against at once, and builds the collision boundary with them.
     * @param sprite1 Sprite that collides with the batch
     * @param sprite2list Sprites to collide against.
     * @param game our game.
     * @return number of sprites in the list to collide with. List comes back sorted by proximity.
     */
    public int sortBatch(VGDLSprite sprite1, ArrayList<VGDLSprite> sprite2list, Game game) {

        if(sprite2list.size() == 1) {
            //execute(sprite1, sprite2list.get(0), game);
            collision = sprite2list.get(0).rect;
            return 1;

        }else if(sprite2list.size() > 2)
        {
            //More than 2, sort by proximity and return the first.
            sortByProximity(sprite1, sprite2list);
            //execute(sprite1, sprite2list.get(0), game);
            collision = sprite2list.get(0).rect;
            return 1;
        }

        //Else, do a cleverer collision with a wall of 2
        VGDLSprite b1 = sprite2list.get(0);
        VGDLSprite b2 = sprite2list.get(1);

        if(b1.rect.getCenterX() == b2.rect.getCenterX())
        {
            boolean b1Above = b1.rect.getY() < b2.rect.getY();

            double x = b1.rect.getX();
            double y = b1Above ? b1.rect.getY() : b2.rect.getY();
            double w = b1.rect.getWidth();
            double h = b1.rect.getHeight() * 2;
            collision = new Rectangle((int)x,(int)y,(int)w,(int)h);

        }else if (b1.rect.getCenterY() == b2.rect.getCenterY())
        {
            boolean b1Left = b1.rect.getX() < b2.rect.getX();

            double x = b1Left ? b1.rect.getX() : b2.rect.getX();
            double y = b1.rect.getY();
            double w = b1.rect.getWidth() * 2;
            double h = b1.rect.getHeight();
            collision = new Rectangle((int)x,(int)y,(int)w,(int)h);
        }else {
            //Not aligned, better to use the closest one.
            sortByProximity(sprite1, sprite2list);
            //execute(sprite1, sprite2list.get(0), game);
            collision = sprite2list.get(0).rect;
            return 1;
        }

        return 2;

    }

    private void sortByProximity(VGDLSprite sprite1, ArrayList<VGDLSprite> sprite2list)
    {
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

    /**
     * Determines if the collision is horizontal and/or vertical
     * @param sprite1 Sprite colliding
     * @param s2rect Collision colliding against.
     * @param g game
     * @return An array indicating if the collision is []{horizontal, vertical}.
     */
    protected boolean[] determineCollision(VGDLSprite sprite1, Rectangle s2rect, Game g)
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

        return new boolean[]{horizontalBounce, verticalBounce};
    }


    public void setStochastic() {
        if (prob > 0 && prob < 1)
            is_stochastic = true;
    }

    public void parseParameters(InteractionContent content) {

        enabled=true;
        //parameters from the object.
        VGDLFactory.GetInstance().parseParameters(content, this);
        hashCode = content.hashCode;
    }

    /**
     * Determine score change for specific player
     * @param playerID - player affected
     * @return - score change
     */
    public int getScoreChange(int playerID) {
        String[] scores = scoreChange.split(",");
        try{
            return playerID < scores.length ? Integer.parseInt(scores[playerID]) : Integer.parseInt(scores[0]);
        }
        catch(Exception e){
            Logger.getInstance().addMessage(new Message(Message.WARNING, "scoreChange must be an integer number not " + scoreChange + "."));
            return 0;
        }
    }

    public int getCounter(int idx) {
        String[] scores = counter.split(",");
        try{
            return idx < scores.length ? Integer.parseInt(scores[idx]) : Integer.parseInt(scores[0]);
        }
        catch(Exception e){
            Logger.getInstance().addMessage(new Message(Message.WARNING, "counter must be an integer number not " + counter + "."));
            return 0;
        }
    }
    
    public int getCounterElse(int idx) {		
    	String[] scores = counterElse.split(",");
    	try{
    	    return idx < scores.length ? Integer.parseInt(scores[idx]) : Integer.parseInt(scores[0]);	
    	}
    	catch(Exception e){
            Logger.getInstance().addMessage(new Message(Message.WARNING, "counterElse must be an integer number not " + counterElse + "."));
            return 0;
        }
    }
    
    public ArrayList<String> getEffectSprites(){
    	return new ArrayList<String>();
    }


    protected Rectangle calculatePixelPerfect(VGDLSprite sprite1, VGDLSprite sprite2)
    {
        Vector2d sprite1v = new Vector2d(sprite1.rect.getCenterX() - sprite1.lastrect.getCenterX(),
                sprite1.rect.getCenterY() - sprite1.lastrect.getCenterY());

        sprite1v.normalise();
        Direction sprite1Dir = new Direction(sprite1v.x, sprite1v.y);

        if(sprite1Dir.equals(Types.DDOWN))
        {
            return adjustDown(sprite1, sprite2);
        }
        else if(sprite1Dir.equals(Types.DRIGHT))
        {
            return adjustRight(sprite1, sprite2);
        }
        else if(sprite1Dir.equals(Types.DUP))
        {
            return adjustUp(sprite1, sprite2);
        }
        else if(sprite1Dir.equals(Types.DLEFT))
        {
            return adjustLeft(sprite1, sprite2);

//        }else{
//
//            //Not an integral direction.
//            double centerXDiff = Math.abs(sprite1.rect.getCenterX() - sprite2.rect.getCenterX());
//            double centerYDiff = Math.abs(sprite1.rect.getCenterY() - sprite2.rect.getCenterY());
//
//            //if(centerXDiff > centerYDiff)
//            if(Math.abs(sprite1Dir.x()) > Math.abs(sprite1Dir.y()))
//            {
//                if(sprite1Dir.x()>0) {
//                    return adjustRight(sprite1, sprite2);
//                }else{
//                    return adjustLeft(sprite1, sprite2);
//                }
//            }else
//            {
//                if(sprite1Dir.y()>0) { //down
//                    return adjustDown(sprite1, sprite2);
//                }else{
//                    return adjustUp(sprite1, sprite2);
//                }
//            }
        }

        return sprite1.lastrect;

    }

    private Rectangle adjustRight(VGDLSprite sprite1, VGDLSprite sprite2)
    {
        int overlay = (sprite1.rect.x + sprite1.rect.width) - sprite2.rect.x;
        return new Rectangle(sprite1.rect.x - overlay, sprite1.rect.y,
                sprite1.rect.width, sprite1.rect.height);
    }

    private Rectangle adjustLeft(VGDLSprite sprite1, VGDLSprite sprite2)
    {
        return new Rectangle(sprite2.rect.x + sprite2.rect.width, sprite1.rect.y,
                sprite1.rect.width, sprite1.rect.height);
    }

    private Rectangle adjustUp(VGDLSprite sprite1, VGDLSprite sprite2)
    {
        return new Rectangle(sprite1.rect.x, sprite2.rect.y + sprite2.rect.height,
                sprite1.rect.width, sprite1.rect.height);
    }

    private Rectangle adjustDown(VGDLSprite sprite1, VGDLSprite sprite2)
    {
        int overlay = (sprite1.rect.y + sprite1.rect.height) - sprite2.rect.y;
        return new Rectangle(sprite1.rect.x, sprite1.rect.y - overlay,
                sprite1.rect.width, sprite1.rect.height);
    }




}
