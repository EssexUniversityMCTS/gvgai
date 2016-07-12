package ontology.avatar;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import core.VGDLSprite;
import core.competition.CompetitionParameters;
import core.content.SpriteContent;
import core.game.Game;
import core.player.Player;
import ontology.Types;
import tools.*;

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
    public ArrayList<Types.ACTIONS> actionsNIL;
    public Player player;
    private int playerID;
    private double score = 0.0;
    private Types.WINNER winState = Types.WINNER.NO_WINNER;

    /**
     * Disqualified flag, moved from Game class to individual players,
     * as there may be more than 1 in a game; variable still in Game
     * class for single player games to keep back-compatibility
     */
    protected boolean is_disqualified;

    //Avatar can have any KeyHandler system. We use KeyInput by default.
    private KeyHandler ki;

    public Types.MOVEMENT lastMovementType = Types.MOVEMENT.STILL;

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
        actionsNIL = new ArrayList<Types.ACTIONS>();

        color = Types.WHITE;
        speed = 1;
        is_avatar = true;
        alternate_keys = false;
        is_disqualified = false;
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

        //A separate array with the same actions, plus NIL.
        for(Types.ACTIONS act : actions)
        {
            actionsNIL.add(act);
        }
        actionsNIL.add(Types.ACTIONS.ACTION_NIL);
    }

    /**
     * This update call is for the game tick() loop.
     * @param game current state of the game.
     */
    public void update(Game game) {
        lastMovementType = Types.MOVEMENT.STILL;

        //Sets the input mask for this cycle.
        ki.setMask(getPlayerID());

        //Get the input from the player.
        requestPlayerInput(game);

        //Map from the action mask to a Vector2D action.
        Direction action2D = Utils.processMovementActionKeys(ki.getMask(), getPlayerID());

        //Apply the physical movement.
        applyMovement(game, action2D);
    }


    /**
     * This move call is for the Forward Model tick() loop.
     * @param game current state of the game.
     * @param actionMask action to apply.
     */
    public void move(Game game, boolean[] actionMask) {

        //Apply action supplied (active movement). USE is checked up in the hierarchy.
        Direction action = Utils.processMovementActionKeys(actionMask, getPlayerID());
        applyMovement(game, action);
    }

    private void applyMovement(Game game, Direction action)
    {
        lastMovementType = this.physics.activeMovement(this, action, this.speed);
    }

    /**
     * Requests the controller's input, setting the game.ki.action mask with the processed data.
     * @param game
     */
    protected void requestPlayerInput(Game game)
    {
        ElapsedCpuTimer ect = new ElapsedCpuTimer(CompetitionParameters.TIMER_TYPE);
        ect.setMaxTimeMillis(CompetitionParameters.ACTION_TIME);

        Types.ACTIONS action;
        if (game.no_players > 1) {
            action = this.player.act(game.getObservationMulti().copy(), ect.copy());
        } else {
            action = this.player.act(game.getObservation(), ect.copy());
        }
        //System.out.println(action);

        
        if(ect.exceededMaxTime())
        {
            long exceeded =  - ect.remainingTimeMillis();

            if(ect.elapsedMillis() > CompetitionParameters.ACTION_TIME_DISQ)
            {
                //The agent took too long to replay. The game is over and the agent is disqualified
                System.out.println("Too long: " + playerID + "(exceeding "+(exceeded)+"ms): controller disqualified.");
                game.disqualify(playerID);
            }else{
                System.out.println("Overspent: " + playerID + "(exceeding "+(exceeded)+"ms): applying ACTION_NIL.");
            }

            action = Types.ACTIONS.ACTION_NIL;
        }


        if(!actions.contains(action))
            action = Types.ACTIONS.ACTION_NIL;

        this.player.logAction(action);
        game.setAvatarLastAction(action, getPlayerID());
        ki.reset(getPlayerID());
        ki.setAction(action, getPlayerID());
    }


    public void updateUse(Game game)
    {
        //Nothing to do by default.
    }

    /**
     * Gets the key handler of this avatar.
     * @return - KeyHandler object.
     */
    public KeyHandler getKeyHandler() { return ki; }

    /**
     * Sets the key handler of this avatar.
     * @param k - new KeyHandler object.
     */
    public void setKeyHandler(KeyHandler k) {
        if (k instanceof KeyInput)
            ki = new KeyInput();
        else ki = k;
    }

    /**
     * Checks whether this player is disqualified.
     * @return true if disqualified, false otherwise.
     */
    public boolean is_disqualified() {
        return is_disqualified;
    }

    /**
     * Sets the disqualified flag.
     */
    public void disqualify(boolean is_disqualified) { this.is_disqualified = is_disqualified; }

    /**
     * Gets the score of this player.
     * @return score.
     */
    public double getScore() { return score; }

    /**
     * Sets the score of this player to a new value.
     * @param s - new score.
     */
    public void setScore(double s) { score = s; }

    /**
     * Adds a value to the current score of this player.
     * @param s - value to add to the score.
     */
    public void addScore (double s) { score += s; }

    /**
     * Gets the win state of this player.
     * @return - win state, value of Types.WINNER
     */
    public Types.WINNER getWinState() { return winState; }

    /**
     * Sets the win state of this player.
     * @param w - new win state.
     */
    public void setWinState(Types.WINNER w) { winState = w; }


    /**
     * Get this player's ID.
     * @return player ID.
     */
    public int getPlayerID() {
        return playerID;
    }

    /**
     * Set this player's ID to a new value.
     * @param id - new player ID.
     */
    public void setPlayerID(int id) {
        playerID = id;
    }

    public VGDLSprite copy() {
        MovingAvatar newSprite = new MovingAvatar();
        this.copyTo(newSprite);

        //copy player
        try {
            newSprite.player = player;
        } catch (Exception e) {e.printStackTrace();}


        return newSprite;
    }

    public void copyTo(VGDLSprite target) {
        MovingAvatar targetSprite = (MovingAvatar) target;
        targetSprite.alternate_keys = this.alternate_keys;
        targetSprite.actions = new ArrayList<Types.ACTIONS>();
        targetSprite.actionsNIL = new ArrayList<Types.ACTIONS>();
        targetSprite.playerID = this.playerID;
        targetSprite.winState = this.winState;
        targetSprite.score = this.score;

        //copy key handler
        targetSprite.setKeyHandler(this.getKeyHandler());
        
        // need to copy orientation here already because MovingAvatar.postProcess() requires the orientation
        targetSprite.orientation = this.orientation.copy();	
        
        targetSprite.postProcess();
        super.copyTo(targetSprite);
    }


}
