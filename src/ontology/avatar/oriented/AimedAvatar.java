package ontology.avatar.oriented;

import java.awt.Dimension;

import core.content.SpriteContent;
import core.game.Game;
import core.vgdl.VGDLRegistry;
import core.vgdl.VGDLSprite;
import ontology.Types;
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
public class AimedAvatar extends ShootAvatar
{

    public double angle_diff = 0.15;

    public AimedAvatar(){}

    public AimedAvatar(Vector2d position, Dimension size, SpriteContent cnt)
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
        speed=0;
        stationary = true;
    }

    public void postProcess()
    {
        //Define actions here first.
        if(actions.size()==0)
        {
            actions.add(Types.ACTIONS.ACTION_USE);
            actions.add(Types.ACTIONS.ACTION_DOWN);
            actions.add(Types.ACTIONS.ACTION_UP);
        }

        super.postProcess();

        stypes = stype.split(",");
        itype = new int[stypes.length];

        for (int i = 0; i < itype.length; i++)
            itype[i] = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stypes[i]);
        if(ammo != null) {
            ammos = ammo.split(",");
            ammoId = new int[ammos.length];
            for (int i = 0; i < ammos.length; i++) {
                ammoId[i] = VGDLRegistry.GetInstance().getRegisteredSpriteValue(ammos[i]);
            }
        }
    }

    /**
     * This update call is for the game tick() loop.
     * @param game current state of the game.
     */
    public void updateAvatar(Game game, boolean requestInput, boolean[] actionMask)
    {
        super.updateAvatar(game, requestInput, actionMask);
        updateUse(game);
        aim();
        move();
    }

    
    private void aim()
    {
    	double angle = 0.0;
    	if(Utils.processMovementActionKeys(getKeyHandler().getMask(), getPlayerID()) == Types.DUP) 
    	{
    		angle = -angle_diff;
    	}
    	else if (Utils.processMovementActionKeys(getKeyHandler().getMask(), getPlayerID()) == Types.DDOWN) 
    	{
    		angle = angle_diff;
    	}
    	double new_x_angle = this.orientation.x()*Math.cos(angle)-this.orientation.y()*Math.sin(angle);
    	double new_y_angle = this.orientation.x()*Math.sin(angle)+this.orientation.y()*Math.cos(angle);
    	this.orientation = new Direction(new_x_angle,new_y_angle);

        this._updateRotation(Math.atan2(this.orientation.y(),this.orientation.x()));
    }


    public void move()
    {
        Direction facing = new Direction(0,0);

        if (Utils.processMovementActionKeys(getKeyHandler().getMask(), getPlayerID()) == Types.DUP)
        {
            facing = new Direction(Math.cos(this.rotation), Math.sin(this.rotation));
            this.physics.activeMovement(this, facing, speed);
        }
        else if (Utils.processMovementActionKeys(getKeyHandler().getMask(), getPlayerID()) == Types.DDOWN)
        {
            facing = new Direction(Math.cos(this.rotation+Math.toRadians(180)), Math.sin(this.rotation+Math.toRadians(180.0)));
            this.physics.activeMovement(this, facing, speed);
        }
    }


    public VGDLSprite copy()
    {
        AimedAvatar newSprite = new AimedAvatar();
        this.copyTo(newSprite);
        return newSprite;
    }

    public void copyTo(VGDLSprite target)
    {
        AimedAvatar targetSprite = (AimedAvatar) target;
        targetSprite.angle_diff = this.angle_diff;

        super.copyTo(targetSprite);
    }
}
