package ontology.avatar.oriented;

import java.awt.Dimension;
import java.util.ArrayList;

import core.VGDLRegistry;
import core.VGDLSprite;
import core.content.SpriteContent;
import core.game.Game;
import ontology.Types;
import tools.Utils;
import tools.Vector2d;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 22/10/13
 * Time: 18:10
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class ShootAvatar extends OrientedAvatar
{

    //This is the resource I need, to be able to shoot.
    public String ammo; //If ammo is null, no resource needed to shoot.
    public int ammoId;

    //This is the sprite I shoot
    public String stype;
    public int itype;

    public ShootAvatar(){}

    public ShootAvatar(Vector2d position, Dimension size, SpriteContent cnt)
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
        ammo = null;
        ammoId = -1;
        stype = null;
        itype = -1;
    }

    /**
     * This update call is for the game tick() loop.
     * @param game current state of the game.
     */
    public void update(Game game)
    {
        super.update(game);
        if(lastMovementType == Types.MOVEMENT.STILL)
            updateUse(game);
    }


    /**
     * This move call is for the Forward Model tick() loop.
     * @param game current state of the game.
     * @param actionMask action to apply.
     */
    public void move(Game game, boolean[] actionMask)
    {
        super.move(game, actionMask);
        updateUse(game);
    }

    public void updateUse(Game game)
    {
        if(Utils.processUseKey(game.ki.getMask()) && hasAmmo())
        {
            shoot(game);
        }
    }

    private void shoot(Game game)
    {
        //TODO: Theoretically, we should be able to shoot many things here... to be done.
        Vector2d dir = this.orientation.copy();
        dir.normalise();

        VGDLSprite newOne = game.addSprite(itype, new Vector2d(this.rect.x + dir.x*this.lastrect.width,
                                           this.rect.y + dir.y*this.lastrect.height));

        if(newOne != null)
        {
            if(newOne.is_oriented)
                newOne.orientation = dir;
            reduceAmmo();
            newOne.setFromAvatar(true);
        }
    }

    private boolean hasAmmo()
    {
        if(ammo == null)
            return true; //no ammo defined, I can shoot.

        //If I have ammo, I must have enough resource of ammo type to be able to shoot.
        if(resources.containsKey(ammoId))
            return resources.get(ammoId) > 0;

        return false;
    }

    private void reduceAmmo()
    {
        if(ammo != null && resources.containsKey(ammoId))
        {
            resources.put(ammoId, resources.get(ammoId) - 1);
        }
    }

    public void postProcess()
    {
        //Define actions here first.
        if(actions.size()==0)
        {
            actions.add(Types.ACTIONS.ACTION_USE);
            actions.add(Types.ACTIONS.ACTION_LEFT);
            actions.add(Types.ACTIONS.ACTION_RIGHT);
            actions.add(Types.ACTIONS.ACTION_DOWN);
            actions.add(Types.ACTIONS.ACTION_UP);
        }

        super.postProcess();

        itype =  VGDLRegistry.GetInstance().getRegisteredSpriteValue(stype);
        if(ammo != null)
            ammoId = VGDLRegistry.GetInstance().getRegisteredSpriteValue(ammo);
    }

    public VGDLSprite copy()
    {
        ShootAvatar newSprite = new ShootAvatar();
        this.copyTo(newSprite);
        return newSprite;
    }

    public void copyTo(VGDLSprite target)
    {
        ShootAvatar targetSprite = (ShootAvatar) target;
        targetSprite.stype = this.stype;
        targetSprite.itype= this.itype;
        targetSprite.ammo = this.ammo;
        targetSprite.ammoId= this.ammoId;

        super.copyTo(targetSprite);
    }
    
    @Override
    public ArrayList<String> getDependentSprites(){
    	ArrayList<String> result = new ArrayList<String>();
    	if(ammo != null) result.add(ammo);
    	if(stype != null) result.add(stype);
    	
    	return result;
    }
}
