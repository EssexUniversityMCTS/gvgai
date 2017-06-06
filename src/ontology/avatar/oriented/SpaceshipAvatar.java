package ontology.avatar.oriented;

import java.awt.Dimension;

import core.vgdl.VGDLSprite;
import core.content.SpriteContent;
import core.game.Game;
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
public class SpaceshipAvatar extends ShootAvatar
{

    public double angle_diff = 0.3;

    public SpaceshipAvatar(){}

    public SpaceshipAvatar(Vector2d position, Dimension size, SpriteContent cnt)
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
        speed=5;
        orientation = Types.DNONE;
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
    
    public void shoot(Game game, int idx)
    {
        Vector2d dir = this.orientation.getVector();
        dir.normalise();

        VGDLSprite newOne = game.addSprite(itype[idx], new Vector2d(this.rect.x + Math.cos(this.rotation)*this.lastrect.width,
                                           this.rect.y + Math.sin(this.rotation)*this.lastrect.height));

        if(newOne != null)
        {
            if(newOne.is_oriented)
                newOne.orientation = new Direction(Math.cos(this.rotation), Math.sin(this.rotation));
            reduceAmmo(idx);
            newOne.setFromAvatar(true);
        }
    }
    
    public void applyMovement(Game game, Direction action)
    {
    	//this.physics.passiveMovement(this);
    	if (physicstype != 0)
    		super.updatePassive();
    }

    
    private void aim()
    {
    	double angle = this.rotation;

    	if(Utils.processMovementActionKeys(getKeyHandler().getMask(), getPlayerID()) == Types.DLEFT) 
    	{
    		angle -= angle_diff;
    	}
    	else if (Utils.processMovementActionKeys(getKeyHandler().getMask(), getPlayerID()) == Types.DRIGHT) 
    	{
    		angle += angle_diff;
    	}
    	this._updateRotation(angle);
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
        SpaceshipAvatar newSprite = new SpaceshipAvatar();
        this.copyTo(newSprite);
        return newSprite;
    }

    public void copyTo(VGDLSprite target)
    {
        SpaceshipAvatar targetSprite = (SpaceshipAvatar) target;
        targetSprite.angle_diff = this.angle_diff;
        super.copyTo(targetSprite);
    }

}
