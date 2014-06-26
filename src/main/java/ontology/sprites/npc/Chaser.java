package ontology.sprites.npc;

import core.VGDLRegistry;
import core.VGDLSprite;
import core.content.SpriteContent;
import core.game.Game;
import ontology.Types;
import tools.Utils;
import tools.Vector2d;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA. User: Diego Date: 21/10/13 Time: 18:14 This is a Java port from Tom Schaul's VGDL -
 * https://github.com/schaul/py-vgdl
 */
public class Chaser extends RandomNPC {
  public boolean fleeing;
  public String stype;
  public int itype;

  java.util.List<VGDLSprite> targets;
  ArrayList<Vector2d> actions;

  public Chaser() {
  }

  public Chaser(Vector2d position, Dimension size, SpriteContent cnt) {
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
    fleeing = false;
    targets = new ArrayList<>();
    actions = new ArrayList<>();
  }

  @Override
  public void postProcess() {
    super.postProcess();
    // Define actions here.
    itype = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stype);
  }

  @Override
    public void update(Game game)
    {
        actions.clear();

        //passive moment.
        updatePassive();

        //Get the closest targets
        closestTargets(game);
        //Update the list of actions that moves me towards each target
        targets.forEach(this::movesToward);

        //Choose randomly an action among the ones that allows me to chase.
        Vector2d act = actions.isEmpty() ? (Vector2d) Utils.choice(Types.BASEDIRS, game.getRandomGenerator()) : Utils.choice(actions, game.getRandomGenerator());

        //Apply the action to move.
        physics.activeMovement(this, act, speed);
    }

  protected void movesToward(VGDLSprite target) {
    double distance = physics.distance(rect, target.rect);
    for (Vector2d act : Types.BASEDIRS) {
      // Calculate the distance if I'd apply this move.
      Rectangle r = new Rectangle(rect);
      r.translate((int) act.x, (int) act.y);
      double newDist = physics.distance(r, target.rect);

      // depending on getting me closer/farther, if I'm fleeing/chasing,
      // add move:
      if (fleeing && distance < newDist)
        actions.add(act);
      if (!fleeing && distance > newDist)
        actions.add(act);
    }
  }

  /**
   * Sets a list with the closest targets (sprites with the type 'stype'), by distance
   * 
   * @param game game to access all sprites
   */
  protected void closestTargets(Game game) {
    targets.clear();
    double bestDist = Double.MAX_VALUE;

    Iterator<VGDLSprite> spriteIt = game.getSpriteGroup(itype);
    if (null != spriteIt)
      while (spriteIt.hasNext()) {
        VGDLSprite s = spriteIt.next();
        double distance = physics.distance(rect, s.rect);
        if (distance < bestDist) {
          bestDist = distance;
          targets.clear();
          targets.add(s);
        } else if (distance == bestDist) {
          targets.add(s);
        }
      }
  }

  @Override
  public VGDLSprite copy() {
    Chaser newSprite = new Chaser();
    copyTo(newSprite);
    return newSprite;
  }

  @Override
  public void copyTo(VGDLSprite target) {
    Chaser targetSprite = (Chaser) target;
    targetSprite.fleeing = fleeing;
    targetSprite.stype = stype;
    targetSprite.itype = itype;
    targetSprite.targets = new ArrayList<>();
    targetSprite.actions = new ArrayList<>();
    super.copyTo(targetSprite);
  }

}
