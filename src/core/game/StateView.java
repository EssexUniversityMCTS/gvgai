package core.game;

import core.VGDLSprite;
import core.player.AbstractPlayer;
import core.player.Player;
import ontology.Types;
import tools.Direction;
import tools.Vector2d;

import java.awt.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 13/11/13
 * Time: 15:37
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class StateView extends StateObservation{

  /**
   * Game object to extract the state view from.
   */
  protected Game game;

  /**
   * Constructor for StateView. Does not use a forward model
   *
   * @param a_game forward model of the game.
   */
  public StateView(Game a_game) {
    game = a_game;
  }

  /**
   * Assigns the player and avatar of the game.
   * @param players
   */
  public void assignPlayer(Player[] players)
  {
    game.assignPlayer(players); //TODO the assignPlayer was private in Game !!!
  }

  /**
   * Gets the score of the game at this observation.
   * @return score of the game.
   */
  public double getGameScore()
  {
    return game.getScore();
  }

  /**
   * Returns the game tick of this particular observation.
   * @return the game tick.
   */
  public int getGameTick()
  {
    return game.getGameTick();
  }

  /**
   * Indicates if there is a game winner in the current observation.
   * Possible values are Types.WINNER.PLAYER_WINS, Types.WINNER.PLAYER_LOSES and
   * Types.WINNER.NO_WINNER.
   * @return the winner of the game.
   */
  public Types.WINNER getGameWinner()
  {
    return game.getWinner();
  }

  /**
   * Indicates if the game is over or if it hasn't finished yet.
   * @return true if the game is over.
   */
  public boolean isGameOver()
  {
    return game.isGameOver();
  }

  /**
   * Returns the world dimensions, in pixels.
   * @return the world dimensions, in pixels.
   */
  public Dimension getWorldDimension()
  {
    return game.screenSize;
  }

  /**
   * Indicates how many pixels form a block in the game.
   * @return how many pixels form a block in the game.
   */
  public int getBlockSize()
  {
    return game.block_size;
  }


  /**
   * Returns a String object with all information about the state of the game. The format is:
   *  Game#gameScore#gameTick#gameWinner#gameOver#            (or)
   *  Game#gameScore#gameTick#gameWinner#gameOver#worldDimWidth#worldDimHeight#blockSize#
   * All elements of the string will always be present (on each variant).
   * @param variable if set to true, the string contains ONLY variable information (winner, game-over, score, time-step).
   *                 If it's set to false, returns the variable information PLUS static info (dimensions and blocksize).
   */
  public String getGameInfo(boolean variable)
  {
    String line = "Game#";
    line += getGameScore() + "#" +  getGameTick() + "#" +  getGameWinner() + "#" + isGameOver() + "#";
    if(!variable)
    {
      line += getWorldDimension().width + "#" + getWorldDimension().height + "#" + getBlockSize() + "#";
    }
    return line;
  }


  //Methods to retrieve the state of the avatar, in the game...


  /**
   * Returns the actions that are available in this game for
   * the avatar.
   * @return the available actions.
   */
  public ArrayList<Types.ACTIONS> getAvailableActions()
  {
    return this.getAvailableActions(false);
  }

  /**
   * Returns the actions that are available in this game for
   * the avatar. If the parameter 'includeNIL' is true, the array contains the (always available)
   * NIL action. If it is false, this is equivalent to calling getAvailableActions().
   * @param includeNIL true to include Types.ACTIONS.ACTION_NIL in the array of actions.
   * @return the available actions.
   */
  public ArrayList<Types.ACTIONS> getAvailableActions(boolean includeNIL)
  {
    //if(game.isEnded)
    //    return new ArrayList<Types.ACTIONS>();
    if(includeNIL)
      return game.getAvatar().actionsNIL;
    return game.getAvatar().actions;
  }

  /**
   * Returns a String object with all information about the available actions of the game. Format:
   *  Actions#act1,act2,act3#
   * The available actions are separated by commas, and the number of 'tokens' depends on the game.
   * This information does not change through the game. This list always includes the action NIL.
   */
  public String getActionsInfo()
  {
    String actionsLine = "Actions#";
    ArrayList<Types.ACTIONS> actions = getAvailableActions(true);
    //for(Types.ACTIONS act : actions)
    for(int i = 0; i < actions.size(); ++i)
    {
      Types.ACTIONS act = actions.get(i);
      actionsLine += act;
      if(i < actions.size()-1 )
        actionsLine += ",";
    }
    actionsLine += "#";

    return actionsLine;
  }

  /**
   * Returns the position of the avatar. If the game is finished, we cannot guarantee that
   * this position reflects the real position of the avatar (the avatar itself could be
   * destroyed). If game finished, this returns Types.NIL.
   * @return position of the avatar, or Types.NIL if game is over.
   */
  public Vector2d getAvatarPosition()
  {
//    if(game.avatar == null)
//      return game.deadAvatar.getPosition();
    return game.getAvatar().getPosition();
  }

  /**
   * Returns the speed of the avatar. If the game is finished, we cannot guarantee that
   * this speed reflects the real speed of the avatar (the avatar itself could be
   * destroyed). If game finished, this returns 0.
   * @return orientation of the avatar, or 0 if game is over.
   */
  public double getAvatarSpeed()
  {
//    if(game.avatar == null)
//      return game.deadAvatar.speed;
    return game.getAvatar().speed;
  }

  /**
   * Returns the orientation of the avatar. If the game is finished, we cannot guarantee that
   * this orientation reflects the real orientation of the avatar (the avatar itself could be
   * destroyed). If game finished, this returns Types.NIL.
   * @return orientation of the avatar, or Types.NIL if game is over.
   */
  public Vector2d getAvatarOrientation() {
//    if(game.avatar == null)
//      return game.deadAvatar.orientation;
    return new Vector2d(game.getAvatar().orientation.x(), game.getAvatar().orientation.y());
  }

  /**
   * Returns the resources in the avatar's possession. As there can be resources of different
   * nature, each entry is a key-value pair where the key is the resource ID, and the value is
   * the amount of that resource type owned. It should be assumed that there might be other resources
   * available in the game, but the avatar could have none of them.
   * If the avatar has no resources, an empty HashMap is returned.
   * @return resources owned by the avatar.
   */
  public HashMap<Integer, Integer> getAvatarResources() {

    //Determine how many different resources does the avatar have.
    HashMap<Integer, Integer> owned = new HashMap<Integer, Integer>();

    //Extract the set of resources
    Set<Map.Entry<Integer, Integer>> entries;
//    if(game.avatar == null)
//      entries = game.deadAvatar.resources.entrySet();
//    else
      entries = game.getAvatar().resources.entrySet();

    //And for each type, add their amount.
    for(Map.Entry<Integer, Integer> entry : entries)
    {
      owned.put(entry.getKey(), entry.getValue());
    }

    return owned;
  }

  /**
   * Returns the avatar's last move. At the first game cycle, it returns ACTION_NIL.
   * Note that this may NOT be the same as the last action given by the agent, as it may
   * have overspent in the last game cycle.
   * @return the action that was executed in the real game in the last cycle. ACTION_NIL
   * is returned in the very first game step.
   */
//  public Types.ACTIONS getAvatarLastAction()
//  {
////    if(game.avatar == null)
////    {
////      return game.deadAvatar.lastAction;
////    }
//    return game.getAvatarLastAction()[0];
//  }
//
//  public Types.ACTIONS[] getAvatarLastActions()
//  {
//    return game.getAvatarLastAction();
//  }

  /**
   * Returns a String object with all information about the avatar.
   * All this information is dynamic (may change at every game cycle). Format:
   *  Avatar#posX#posY#speed#lastAction#res1,val1;res2,val2;res3,val3#
   * position, speed and last actions are always present. The number of pairs <resource-key,resource-amount> may vary (from 0 to N).
   */
  public String getAvatarInfo()
  {
    String line = "Avatar#";
    line += getAvatarPosition().x + "#" +  getAvatarPosition().y + "#"
        + getAvatarOrientation().x + "#" +  getAvatarOrientation().y + "#"
        + getAvatarSpeed() + "#" + getAvatarLastAction() + "#";

    //Resources.
    Set<Map.Entry<Integer, Integer>> entries;
//    if(game.avatar == null)
//      entries = game.deadAvatar.resources.entrySet();
//    else
      entries = game.getAvatar().resources.entrySet();

    int nEntries = entries.size();
    int idx = 0;
    for(Map.Entry<Integer, Integer> entry : entries)
    {
      line += entry.getKey() + "," + entry.getValue();
      if(idx < nEntries-1)
        line += ";";
      idx++;

    }
    line += "#";

    return line;
  }



  /**
   * Returns a boolean grid with all observations of a given type in the level,
   * accessible by the x,y coordinates of the grid. Each grid cell has a width and height of
   * getBlockSize() pixels. Each cell contains a list with all observations in that position.
   * Note that the same observation may occupy more than one grid cell.
   * @param itype type of the observation to query.
   * @return the grid of observations
   */
  public boolean[][] getObservationGrid(int itype)
  {
    boolean [][]grid  = new boolean[game.screenSize.width/game.block_size][game.screenSize.height/game.block_size];

    if(game.spriteGroups[itype] != null && game.spriteGroups[itype].getFirstSprite() != null)
    {
      Iterator<VGDLSprite> spriteIt = game.spriteGroups[itype].getSpriteIterator();
      if(spriteIt != null) while(spriteIt.hasNext())
      {
        VGDLSprite sp = spriteIt.next();
        addSpriteToGrid(grid, sp.getPosition());
      }
    }
    return grid;
  }

  /**
   * Adds an sprite to the grid of sprites passed as parameter. Sets to true the
   * position(s) where this sprite is.
   * @param grid Grid to fill.
   * @param position Position of the sprite.
   */
  protected void addSpriteToGrid(boolean[][] grid, Vector2d position)
  {
    int x = (int) position.x / game.block_size;
    boolean validX = x >= 0 && x < grid.length;
    boolean xPlus = (position.x % game.block_size) > 0 && (x+1 < grid.length);
    int y = (int) position.y / game.block_size;
    boolean validY = y >= 0 && y < grid[0].length;
    boolean yPlus = (position.y % game.block_size) > 0 && (y+1 < grid[0].length);

    if(validX && validY) {
      grid[x][y] = true;

      if(xPlus)
        grid[x+1][y] = true;
      if(yPlus)
        grid[x][y+1] = true;
      if(xPlus && yPlus)
        grid[x+1][y+1] = true;
    }
  }

  /**
   * Returns a string with a bit map of the presence of the given sprite type in the level. Format:
   *  sX#00110101,01001010,10010101#
   *  X: itype of this bit grid.
   *  0: no presence; 1: presence.
   *  Each row is separated by a comma. The dimensions of the grid won't change during the game, but the contents may.
   * @param itype sprite type to map.
   * @return The string of this sprite type.
   */
  public String getBitGrid(int itype)
  {
    String line = "s" + itype + "#";
    boolean [][]grid  = getObservationGrid(itype);
    for(int i = 0; i < grid.length; ++i)
    {
      for(int j = 0; j < grid[i].length; ++j)
      {
        char bit = grid[i][j] ? '1' : '0';
        line += bit;
      }
      if(i < grid.length - 1)
        line += ",";
    }
    line += "#";
    return line;
  }


  /**
   *  This method does nothing (there is nothing to copy in the state view).
   *
   * @return a copy of the state observation.
   */
  public StateView copy() {
    //Nothing to copy.
    return this;
  }

  /**
   * This method does nothing (there is nothing to advance in the state view).
   * Note: stochastic events will not be necessarily the same as in the real game.
   *
   * @param action agent action to execute in the next cycle.
   */
  public void advance(Types.ACTIONS action) {
    //Nothing to advance.
  }

  /**
   * Compares if this and the received StateObservation state are equivalent.
   * DEBUG ONLY METHOD.
   * @param o Object to compare this to.
   * @return true if o has the same components as this.
   */
  public boolean equiv(Object o)
  {
    //First simple object-level checks.
    if(this == o) return true;
    if(!(o instanceof StateView)) return false;
    StateView other = (StateView)o;

    //Game state checks.
    if(this.getGameScore() != other.getGameScore()) return false;
    if(this.getGameTick() != other.getGameTick()) return false;
    if(this.getGameWinner() != other.getGameWinner()) return false;
    if(this.isGameOver() != other.isGameOver()) return false;
    if(this.getAvatarSpeed() != other.getAvatarSpeed()) return false;
    if(!this.getAvatarPosition().equals(other.getAvatarPosition())) return false;
    if (!this.getAvatarOrientation().equals(other.getAvatarOrientation())) return false;

    //Check resources
    HashMap<Integer, Integer> thisResources = this.getAvatarResources();
    HashMap<Integer, Integer> otherResources = other.getAvatarResources();
    if(thisResources.size() != otherResources.size()) return false;
    try
    {
      Set<Integer> resKeys = otherResources.keySet();
      for (Integer k : resKeys) {
        if (!(otherResources.get(k).equals(thisResources.get(k))))
          return false;
      }
    }catch(Exception e)
    {
      System.out.println(e.toString());
      return false;
    }

    //Check events.
    TreeSet<Event> thisEvents = this.getEventsHistory();
    TreeSet<Event> otherEvents = other.getEventsHistory();
    if(thisEvents.size() != otherEvents.size()) return false;
    try
    {
      Iterator<Event> otherIt = otherEvents.descendingIterator();
      Iterator<Event> thisIt = thisEvents.descendingIterator();

      while(otherIt.hasNext())
      {
        if(!otherIt.next().equals(thisIt.next()))
          return false;
      }

    }catch(Exception e)
    {
      System.out.println(e.toString());
      return false;
    }

    //Check observations?

    return true;
  }

}