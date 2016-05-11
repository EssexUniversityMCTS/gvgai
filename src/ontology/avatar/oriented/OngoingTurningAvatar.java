package ontology.avatar.oriented;

import core.VGDLFactory;
import core.VGDLRegistry;
import core.VGDLSprite;
import core.content.SpriteContent;
import core.game.Game;
import ontology.Types;
import tools.Direction;
import tools.Utils;
import tools.Vector2d;

import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 21/10/13
 * Time: 17:35
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class OngoingTurningAvatar extends OrientedAvatar
{

    public String spawnBehind;
    private int spawnId;

    public OngoingTurningAvatar(){}

    public OngoingTurningAvatar(Vector2d position, Dimension size, SpriteContent cnt)
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
        speed = 1;
        is_oriented = true;
    }


    public void postProcess()
    {
        super.postProcess();
        if(spawnBehind != null)
            spawnId = VGDLRegistry.GetInstance().getRegisteredSpriteValue(spawnBehind);
    }


    /**
     * This update call is for the game tick() loop.
     * @param game current state of the game.
     */
    public void update(Game game)
    {
        lastMovementType = Types.MOVEMENT.MOVE;

        //Get the input from the player.
        requestPlayerInput(game);

        //Map from the action mask to a Vector2D action.
        Direction action2D = Utils.processMovementActionKeys(getKeyHandler().getMask(), getPlayerID());

        //Update the orientation for this cycle's movement,
        // but only if the movement is perpendicular to the current orientation.
        if((action2D != Types.DNONE) && (Direction.orthogonal(action2D, this.orientation)))
        {
            this._updateOrientation(action2D);
        }

        //Update movement.
        super.updatePassive();

        //Spawn behind:
        if(!this.rect.intersects(this.lastrect))
            game.addSprite(spawnId, this.getLastPosition());
    }



    /**
     * This move call is for the Forward Model tick() loop.
     * @param game current state of the game.
     * @param actionMask action to apply.
     */
    public void move(Game game, boolean[] actionMask)
    {
        lastMovementType = Types.MOVEMENT.MOVE;

        //Map from the action mask to a Vector2D action.
        Direction action2D = Utils.processMovementActionKeys(actionMask, getPlayerID());

        //Update the orientation for this cycle's movement,
        // but only if there was a direction indicated.
        if(!action2D.equals(Types.DNONE))
            this._updateOrientation(action2D);

        //Update movement.
        super.updatePassive();
    }


    public VGDLSprite copy()
    {
        OngoingTurningAvatar newSprite = new OngoingTurningAvatar();
        this.copyTo(newSprite);
        return newSprite;
    }

    public void copyTo(VGDLSprite target)
    {
        OngoingTurningAvatar targetSprite = (OngoingTurningAvatar) target;
        super.copyTo(targetSprite);
    }
}
