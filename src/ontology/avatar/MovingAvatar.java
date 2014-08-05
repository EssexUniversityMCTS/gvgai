package ontology.avatar;

import core.VGDLSprite;
import core.competition.CompetitionParameters;
import core.content.SpriteContent;
import core.game.Game;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Utils;
import tools.Vector2d;

import java.awt.*;
import java.util.ArrayList;

import core.player.AbstractPlayer;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 22/10/13
 * Time: 18:04
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class MovingAvatar extends VGDLSprite {

    public boolean alternate_keys;
    public ArrayList<Types.ACTIONS> actions;
    public AbstractPlayer player;

    public boolean hasMoved = false;

    public MovingAvatar() {
    }



    public MovingAvatar(Vector2d position, Dimension size, SpriteContent cnt) {
        //Init the sprite
        this.init(position, size);

        //Specific class default parameter values.
        loadDefaults();

        //Parse the arguments.
        this.parseParameters(cnt);
    }


    protected void loadDefaults() {
        super.loadDefaults();
        actions = new ArrayList<Types.ACTIONS>();

        color = Types.WHITE;
        speed = 1;
        is_avatar = true;
        alternate_keys = false;
    }

    public void postProcess() {

        //Define actions here first.
        if(actions.size()==0)
        {
            actions.add(Types.ACTIONS.ACTION_LEFT);
            actions.add(Types.ACTIONS.ACTION_RIGHT);
            actions.add(Types.ACTIONS.ACTION_DOWN);
            actions.add(Types.ACTIONS.ACTION_UP);
        }

        super.postProcess();

    }

    public void update(Game game) {
        super.updatePassive();

        hasMoved = false;

        ElapsedCpuTimer ect = new ElapsedCpuTimer(ElapsedCpuTimer.TimerType.CPU_TIME);
        ect.setMaxTimeMillis(CompetitionParameters.ACTION_TIME);

        Types.ACTIONS action = this.player.act(game.getObservation(), ect);

        if(ect.exceededMaxTime())
        {
            long exceeded =  - ect.remainingTimeMillis();

            if(ect.elapsedMillis() > CompetitionParameters.ACTION_TIME_DISQ)
            {
                //The agent took too long to replay. The game is over and the agent is disqualified
                System.out.println("Too long: " + "(exceeding "+(exceeded)+"ms): controller disqualified.");
                game.disqualify();
            }else{
                System.out.println("Overspent: " + "(exceeding "+(exceeded)+"ms): applying ACTION_NIL.");
            }

            action = Types.ACTIONS.ACTION_NIL;
        }

        if(!actions.contains(action))
            action = Types.ACTIONS.ACTION_NIL;

        this.player.logAction(action);
        game.ki.reset();
        game.ki.setAction(action);

        Vector2d action2D = Utils.processMovementActionKeys(game.ki.getMask());

        if(action2D != Types.NONE)
            hasMoved = true;

        this.physics.activeMovement(this, action2D, this.speed);
    }

    public void updateUse(Game game)
    {
        //Nothing to do by default.
    }

    /**
     * Performs a given movement, with an action
     * @param actionMask action mask to perform.
     */
    public void performActiveMovement(boolean[] actionMask)
    {
        Vector2d action = Utils.processMovementActionKeys(actionMask);
        this.physics.activeMovement(this, action, this.speed);
    }

    public VGDLSprite copy() {
        MovingAvatar newSprite = new MovingAvatar();
        this.copyTo(newSprite);
        return newSprite;
    }

    public void copyTo(VGDLSprite target) {
        MovingAvatar targetSprite = (MovingAvatar) target;
        targetSprite.alternate_keys = this.alternate_keys;
        targetSprite.actions = new ArrayList<Types.ACTIONS>();
        targetSprite.postProcess();
        super.copyTo(targetSprite);
    }

}
