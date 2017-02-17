package ontology.avatar.oriented;

import core.VGDLRegistry;
import core.VGDLSprite;
import core.content.SpriteContent;
import core.game.Game;
import ontology.Types;
import ontology.avatar.MovingAvatar;
import tools.Direction;
import tools.Utils;
import tools.Vector2d;

import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 22/10/13
 * Time: 18:10
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class WizardAvatar extends MovingAvatar
{
	public boolean on_ground;
	public double ground_speedup_factor;
    public double air_slowdown_factor;

    public WizardAvatar(){}

    public WizardAvatar(Vector2d position, Dimension size, SpriteContent cnt)
    {
        //Init the sprite
        this.init(position, size);

        //Specific class default parameter values.
        loadDefaults();

        //Parse the arguments.
        this.parseParameters(cnt);
    }

    //This is the sprite I shoot
    public String stype;
    public String[] stypes;
    public int[] itype;

    public Direction facing_dir;

    public int last_block_time;

    protected void loadDefaults()
    {
        super.loadDefaults();
        draw_arrow = false;
        strength = 15;
        on_ground = false;
        speed=0;
        stype = null;
        stypes = new String[1];
        itype = new int[1];
        last_block_time = 0;
        ground_speedup_factor = 2.0;
        air_slowdown_factor = 2.0;

        facing_dir = new Direction(1,0);
    }


    /**
     * This update call is for the game tick() loop.
     * @param game current state of the game.
     */
    public void update(Game game)
    {
        super.update(game);
        
        on_ground = false;
        Rectangle test_rect = new Rectangle(this.rect);
        test_rect.setLocation(this.rect.x,this.rect.y+3);
        
        for (int i = 0; i <  game.getSpriteData().size(); i++){
        	if (game.getSpriteData().get(i).name.equals("wall") ||
                game.getSpriteData().get(i).name.equals("bullet"))
            {
        		for (int j = 0; j <  game.getSprites(i).size(); j++){
        			if (game.getSprites(i).get(j).rect.intersects(test_rect) &&
                            !game.getSprites(i).get(j).rect.intersects((this.rect))){
        				on_ground = true;
        			}
        		}
        	}
        }
        
        if(Utils.processUseKey(getKeyHandler().getMask(), getPlayerID()) && on_ground) {
        	Direction action = new Direction (0,-strength);
        	this.orientation = new Direction (this.orientation.x(),0.0);
        	this.physics.activeMovement(this, action, this.speed);
        	Direction temp = new Direction (0,-1);
        	this._updatePos(temp, 5);
        }

        if (Utils.processMovementActionKeys(getKeyHandler().getMask(), getPlayerID()) == Types.DUP &&
                last_block_time+5 <= game.getGameTick()){
            this.physics.activeMovement(this, new Direction(0,1), -1);

            for (int i = 0; i < itype.length; i++) {
                    game.addSprite(itype[i], new Vector2d(this.rect.x + facing_dir.x()*this.lastrect.width*1.2,
                        this.rect.y + facing_dir.y()*this.lastrect.height));

            }
            last_block_time = game.getGameTick();
        }

        if (Utils.processMovementActionKeys(getKeyHandler().getMask(), getPlayerID()) == Types.DLEFT){
            facing_dir = new Direction(-1,0);
        }

        if (Utils.processMovementActionKeys(getKeyHandler().getMask(), getPlayerID()) == Types.DRIGHT){
            facing_dir = new Direction(1,0);
        }

    }
    
    public void postProcess()
    {
        //Define actions here first.
        if(actions.size()==0)
        {
            actions.add(Types.ACTIONS.ACTION_LEFT);
            actions.add(Types.ACTIONS.ACTION_RIGHT);
            actions.add(Types.ACTIONS.ACTION_USE);
            actions.add(Types.ACTIONS.ACTION_UP);
        }

        stypes = stype.split(",");
        itype = new int[stypes.length];

        for (int i = 0; i < itype.length; i++)
            itype[i] = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stypes[i]);

        super.postProcess();
    }
    
    public void move(Game game, boolean[] actionMask)
    {
        super.move(game, actionMask);
    }
    
    public void applyMovement(Game game, Direction action)
    {
    	//this.physics.passiveMovement(this);
    	if (physicstype_id != 0)
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
        WizardAvatar newSprite = new WizardAvatar();
        this.copyTo(newSprite);
        return newSprite;
    }

    public void copyTo(VGDLSprite target)
    {
    	WizardAvatar targetSprite = (WizardAvatar) target;
        targetSprite.stype = this.stype;
        targetSprite.itype = this.itype.clone();
        targetSprite.stypes = this.stypes.clone();
        super.copyTo(targetSprite);
    }


}
