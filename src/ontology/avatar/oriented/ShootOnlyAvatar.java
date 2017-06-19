package ontology.avatar.oriented;

import core.vgdl.VGDLSprite;
import core.content.SpriteContent;
import core.game.Game;
import ontology.Types;
import tools.Direction;
import tools.Utils;
import tools.Vector2d;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 22/10/13
 * Time: 18:08
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class ShootOnlyAvatar extends ShootAvatar
{
    public ShootOnlyAvatar(){}

    public ShootOnlyAvatar(Vector2d position, Dimension size, SpriteContent cnt)
    {
        MAX_WEAPONS = 5;

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
    }


    public void postProcess()
    {
        super.postProcess();

        actions.clear();

        //Define actions here first.
        if(actions.size()==0)
        {
            actions.add(Types.ACTIONS.ACTION_LEFT);
            actions.add(Types.ACTIONS.ACTION_UP);
            actions.add(Types.ACTIONS.ACTION_RIGHT);
            actions.add(Types.ACTIONS.ACTION_DOWN);
            actions.add(Types.ACTIONS.ACTION_USE);
        }
    }

    public void applyMovement(Game game, Direction action)
    {
        //No movement

        //this.physics.passiveMovement(this);
        if (physicstype != 0)
            super.updatePassive();
    }

    public void updateUse(Game game)
    {
        int itypeToShoot;
        if (Utils.processUseKey(getKeyHandler().getMask(), getPlayerID()))
            itypeToShoot = actions.indexOf(Types.ACTIONS.ACTION_USE);
        else {
            Direction action = Utils.processMovementActionKeys(getKeyHandler().getMask(), getPlayerID());
            itypeToShoot = actions.indexOf(Types.ACTIONS.fromVector(action));
        }
        if (itypeToShoot != -1 && itypeToShoot < itype.length) {
            if (hasAmmo(itypeToShoot)) {
                shoot(game, itypeToShoot);
            }
        }
    }

    public VGDLSprite copy()
    {
        ShootOnlyAvatar newSprite = new ShootOnlyAvatar();
        this.copyTo(newSprite);
        return newSprite;
    }

    public void copyTo(VGDLSprite target)
    {
        ShootOnlyAvatar targetSprite = (ShootOnlyAvatar) target;
        super.copyTo(targetSprite);
    }
    
    @Override
    public ArrayList<String> getDependentSprites(){
    	ArrayList<String> result = new ArrayList<String>();
    	
    	return result;
    }
}
