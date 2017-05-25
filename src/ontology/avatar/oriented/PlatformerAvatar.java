package ontology.avatar.oriented;

import java.awt.Dimension;

import core.vgdl.VGDLSprite;
import core.content.SpriteContent;
import core.game.Game;
import ontology.Types;
import ontology.avatar.MovingAvatar;
import tools.Direction;
import tools.Utils;
import tools.Vector2d;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 22/10/13
 * Time: 18:10
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class PlatformerAvatar extends MovingAvatar
{
	public double ground_speedup_factor;
    public double air_slowdown_factor;
	
    public PlatformerAvatar(){}

    public PlatformerAvatar(Vector2d position, Dimension size, SpriteContent cnt)
    {
        //Init the sprite
        this.init(position, size);

        //Specific class default parameter values.
        loadDefaults();

        //Parse the arguments.
        this.parseParameters(cnt);
    }


    protected void loadDefaults()
    {
        super.loadDefaults();
        draw_arrow = false;
        jump_strength = 10;
        on_ground = false;
        ground_speedup_factor = 1.0;
        air_slowdown_factor = 2.0;
        max_speed = 30.0;
    }


    /**
     * Overwritting intersects to check if we are on ground.
     * @return true if it directly intersects with sp (as in the normal case), but additionally checks for on_ground condition.
     */
    public boolean intersects (VGDLSprite sp)
    {
        return this.groundIntersects(sp);
    }


    /**
     * This update call is for the game tick() loop.
     * @param game current state of the game.
     */
    public void updateAvatar(Game game, boolean requestInput, boolean[] actionMask)
    {
        super.updateAvatar(game, requestInput, actionMask);

        //Managing jumps
        if(Utils.processUseKey(getKeyHandler().getMask(), getPlayerID()) && on_ground) {
            Direction action = new Direction (0,-jump_strength);
        	this.orientation = new Direction (this.orientation.x(),0.0);
        	this.physics.activeMovement(this, action, this.speed);
            Direction temp = new Direction (0,-1);
            lastmove = cooldown; //need this to force this movement.
            this._updatePos(temp, 5);
        }


        //This at the end, needed for check on ground status in the next cycle.
        on_ground = false;
        	
    }
    
    public void postProcess()
    {
        //Define actions here first.
        if(actions.size()==0)
        {
            actions.add(Types.ACTIONS.ACTION_LEFT);
            actions.add(Types.ACTIONS.ACTION_RIGHT);
            actions.add(Types.ACTIONS.ACTION_USE);
        }

        super.postProcess();
    }
    
    public void applyMovement(Game game, Direction action)
    {
        //this.physics.passiveMovement(this);
    	if (physicstype != 0)
    		super.updatePassive();
    	if (action.x()!=0.0 || action.y()!=0.0){
    		Direction new_action = new Direction(action.x()*ground_speedup_factor, action.y());
    		if (!on_ground){
    			new_action = new Direction(action.x()/air_slowdown_factor, action.y());
    		}
    		lastMovementType = this.physics.activeMovement(this, new_action, speed);
    	}
    }


    public VGDLSprite copy()
    {
        PlatformerAvatar newSprite = new PlatformerAvatar();
        this.copyTo(newSprite);
        return newSprite;
    }

    public void copyTo(VGDLSprite target)
    {
    	PlatformerAvatar targetSprite = (PlatformerAvatar) target;
        targetSprite.air_slowdown_factor = this.air_slowdown_factor;
        targetSprite.ground_speedup_factor = this.ground_speedup_factor;
        super.copyTo(targetSprite);
    }


}
