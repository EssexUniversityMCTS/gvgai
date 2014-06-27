package ontology.avatar;

import core.VGDLSprite;
import core.competition.CompetitionParameters;
import core.content.SpriteContent;
import core.game.Game;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Utils;
import tools.Vector2d;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA. User: Diego Date: 22/10/13 Time: 18:04 This is a Java port from Tom Schaul's VGDL -
 * https://github.com/schaul/py-vgdl
 */
public class MovingAvatar extends VGDLSprite {

  public boolean alternate_keys;
  public ArrayList<Types.ACTIONS> actions;
  public AbstractPlayer player;

  public boolean hasMoved;

  public MovingAvatar() {
  }

  public MovingAvatar(Vector2d position, Dimension size, SpriteContent cnt) {
    // Init the sprite
    init(position, size);

    // Specific class default parameter values.
    loadDefaults();

    // Parse the arguments.
    parseParameters(cnt);
  }

  @Override
  protected void loadDefaults() {
    super.loadDefaults();
    actions = new ArrayList<>();

    color = Types.WHITE;
    speed = 1;
    is_avatar = true;
    alternate_keys = false;
  }

  @Override
  public void postProcess() {

    // Define actions here first.
    if (actions.isEmpty()) {
      actions.add(Types.ACTIONS.ACTION_LEFT);
      actions.add(Types.ACTIONS.ACTION_RIGHT);
      actions.add(Types.ACTIONS.ACTION_DOWN);
      actions.add(Types.ACTIONS.ACTION_UP);
    }

    super.postProcess();

  }

  @Override
  public void update(Game game) {
    updatePassive();

    hasMoved = false;

    ElapsedCpuTimer ect = new ElapsedCpuTimer(ElapsedCpuTimer.TimerType.CPU_TIME);
    ect.setMaxTimeMillis(CompetitionParameters.ACTION_TIME);

    Types.ACTIONS action = player.act(game.getObservation(), ect);

    if (ect.exceededMaxTime()) {
      long exceeded = -ect.remainingTimeMillis();

      if (ect.elapsedMillis() > CompetitionParameters.ACTION_TIME_DISQ) {
        // The agent took too long to replay. The game is over and the
        // agent is disqualified
        System.out.println("Too long: " + "(exceeding " + exceeded
            + "ms): controller disqualified.");
        game.disqualify();
      } else {
        System.out.println("Overspent: " + "(exceeding " + exceeded + "ms): applying ACTION_NIL.");
      }

      action = Types.ACTIONS.ACTION_NIL;
    }

    if (!actions.contains(action))
      action = Types.ACTIONS.ACTION_NIL;

    player.logAction(action);
    Game.ki.reset();
    Game.ki.setAction(action);

    Vector2d action2D = Utils.processMovementActionKeys(Game.ki.getMask());

    if (action2D != Types.NONE)
      hasMoved = true;

    physics.activeMovement(this, action2D, speed);
  }

  /**
   * Performs a given movement, with an action
   * 
   * @param actionMask action mask to perform.
   */
  public void performActiveMovement(boolean... actionMask) {
    Vector2d action = Utils.processMovementActionKeys(actionMask);
    physics.activeMovement(this, action, speed);
  }

  @Override
  public VGDLSprite copy() {
    MovingAvatar newSprite = new MovingAvatar();
    copyTo(newSprite);
    return newSprite;
  }

  @Override
  public void copyTo(VGDLSprite target) {
    MovingAvatar targetSprite = (MovingAvatar) target;
    targetSprite.alternate_keys = alternate_keys;
    targetSprite.actions = new ArrayList<>();
    targetSprite.postProcess();
    super.copyTo(targetSprite);
  }

}
