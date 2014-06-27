package core;

import core.content.*;
import core.game.BasicGame;
import core.game.Game;
import core.termination.MultiSpriteCounter;
import core.termination.SpriteCounter;
import core.termination.Termination;
import core.termination.Timeout;
import ontology.Types;
import ontology.avatar.*;
import ontology.avatar.oriented.*;
import ontology.effects.Effect;
import ontology.effects.binary.*;
import ontology.effects.unary.*;
import ontology.sprites.*;
import ontology.sprites.missile.*;
import ontology.sprites.npc.*;
import ontology.sprites.producer.Bomber;
import ontology.sprites.producer.Portal;
import ontology.sprites.producer.SpawnPoint;
import ontology.sprites.producer.SpriteProducer;
import tools.Vector2d;

import java.awt.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA. User: Diego Date: 22/10/13 Time: 15:33 This is a Java port from Tom Schaul's VGDL -
 * https://github.com/schaul/py-vgdl
 */
public class VGDLFactory {

  /**
   * Available sprites for VGDL.
   */
  private String[] spriteStrings = {
      "Conveyor", "Flicker", "Immovable", "OrientedFlicker", "Passive", "Resource", "Spreader",
      "ErraticMissile", "Missile", "RandomMissile", "Walker", "WalkerJumper", "ResourcePack",
      "Chaser", "Fleeing", "RandomInertial", "RandomNPC", "AlternateChaser", "RandomAltChaser",
      "Bomber", "Portal", "SpawnPoint", "SpriteProducer", "Door", "FlakAvatar", "HorizontalAvatar",
      "MovingAvatar", "VerticalAvatar", "NoisyRotatingFlippingAvatar", "RotatingAvatar",
      "RotatingFlippingAvatar", "AimedAvatar", "AimedFlakAvatar", "InertialAvatar", "MarioAvatar",
      "OrientedAvatar", "ShootAvatar", "MissileAvatar"};

  /**
   * Available Sprite classes for VGDL.
   */
  private Class[] spriteClasses = {
      Conveyor.class, Flicker.class, Immovable.class, OrientedFlicker.class, Passive.class,
      Resource.class, Spreader.class, ErraticMissile.class, Missile.class, RandomMissile.class,
      Walker.class, WalkerJumper.class, ResourcePack.class, Chaser.class, Fleeing.class,
      RandomInertial.class, RandomNPC.class, AlternateChaser.class, RandomAltChaser.class,
      Bomber.class, Portal.class, SpawnPoint.class, SpriteProducer.class, Door.class,
      FlakAvatar.class, HorizontalAvatar.class, MovingAvatar.class, VerticalAvatar.class,
      NoisyRotatingFlippingAvatar.class, RotatingAvatar.class, RotatingFlippingAvatar.class,
      AimedAvatar.class, AimedFlakAvatar.class, InertialAvatar.class, MarioAvatar.class,
      OrientedAvatar.class, ShootAvatar.class, MissileAvatar.class};

  /**
   * Available effects for VGDL.
   */
  private String[] effectStrings = {
      "stepBack", "turnAround", "killSprite", "transformTo", "wrapAround", "changeResource",
      "killIfHasLess", "killIfHasMore", "cloneSprite", "flipDirection", "reverseDirection",
      "undoAll", "spawnIfHasMore", "pullWithIt", "wallStop", "collectResource",
      "killIfOtherHasMore", "killIfFromAbove", "teleportToExit", "bounceForward", "attractGaze"};

  /**
   * Available effect classes for VGDL.
   */
  private Class[] effectClasses = {
      StepBack.class, TurnAround.class, KillSprite.class, TransformTo.class, WrapAround.class,
      ChangeResource.class, KillIfHasLess.class, KillIfHasMore.class, CloneSprite.class,
      FlipDirection.class, ReverseDirection.class, UndoAll.class, SpawnIfHasMore.class,
      PullWithIt.class, WallStop.class, CollectResource.class, KillIfOtherHasMore.class,
      KillIfFromAbove.class, TeleportToExit.class, BounceForward.class, AttractGaze.class};

  /**
   * Available terminations for VGDL.
   */
  private String[] terminationStrings = {"MultiSpriteCounter", "SpriteCounter", "Timeout"};

  /**
   * Available termination classes for VGDL.
   */
  private Class[] terminationClasses = {
      MultiSpriteCounter.class, SpriteCounter.class, Timeout.class};

  /**
   * Singleton reference to game/sprite factory
   */
  private static VGDLFactory factory;

  /**
   * Cache for registered games.
   */
  public static Map<String, Class> registeredGames;

  /**
   * Cache for registered sprites.
   */
  public static Map<String, Class> registeredSprites;

  /**
   * Cache for registered effects.
   */
  public static Map<String, Class> registeredEffects;

  /**
   * Cache for registered effects.
   */
  public static Map<String, Class> registeredTerminations;

  /**
   * Default private constructor of this singleton.
   */
  private VGDLFactory() {
  }

  /**
   * Initializes the maps for caching classes.
   */
  public void init() {
    VGDLFactory.registeredGames = new HashMap<>();
    VGDLFactory.registeredGames.put("BasicGame", BasicGame.class);

    VGDLFactory.registeredSprites = new HashMap<>();
    for (int i = 0; i < spriteStrings.length; ++i) {
      VGDLFactory.registeredSprites.put(spriteStrings[i], spriteClasses[i]);
    }

    VGDLFactory.registeredEffects = new HashMap<>();
    for (int i = 0; i < effectStrings.length; ++i) {
      VGDLFactory.registeredEffects.put(effectStrings[i], effectClasses[i]);
    }

    VGDLFactory.registeredTerminations = new HashMap<>();
    for (int i = 0; i < terminationStrings.length; ++i) {
      VGDLFactory.registeredTerminations.put(terminationStrings[i], terminationClasses[i]);
    }
  }

  /**
   * Returns the unique instance of this class.
   * 
   * @return the factory that creates the game and the sprite objects.
   */
  public static VGDLFactory GetInstance() {
    if (null == VGDLFactory.factory)
      VGDLFactory.factory = new VGDLFactory();
    return VGDLFactory.factory;
  }

  /**
   * Creates a game, receiving a GameContent object
   * 
   * @param content potential parameters for the class.
   * @return The game just created.
   */
  public Game createGame(GameContent content) {
    try {
      Class gameClass = VGDLFactory.registeredGames.get(content.referenceClass);
      Constructor gameConstructor = gameClass.getConstructor(new Class[] {GameContent.class});
      return (Game) gameConstructor.newInstance(content);

    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("Error creating game of class " + content.referenceClass);
    }

    return null;
  }

  /**
   * Creates a new sprite with a given dimension in a certain position. Parameters are passed as SpriteContent.
   * 
   * @param content parameters for the sprite, including its class.
   * @param position position of the object.
   * @param dim dimensions of the sprite on the world.
   * @return the new sprite, created and initialized, ready for play!
   */
  public VGDLSprite createSprite(SpriteContent content, Vector2d position, Dimension dim) {
    try {
      Class spriteClass = VGDLFactory.registeredSprites.get(content.referenceClass);
      Constructor spriteConstructor =
          spriteClass.getConstructor(new Class[] {
              Vector2d.class, Dimension.class, SpriteContent.class});
      return (VGDLSprite) spriteConstructor.newInstance(position, dim, content);

    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("Error creating sprite " + content.identifier + " of class "
          + content.referenceClass);
    }

    return null;
  }

  /**
   * Creates a new effect, with parameters passed as InteractionContent.
   * 
   * @param content parameters for the effect, including its class.
   * @return the new effect, created and initialized, ready to be triggered!
   */
  public Effect createEffect(InteractionContent content) {
    try {
      Class effectClass = VGDLFactory.registeredEffects.get(content.function);
      Constructor effectConstructor =
          effectClass.getConstructor(new Class[] {InteractionContent.class});
      return (Effect) effectConstructor.newInstance(content);

    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("Error creating effect " + content.function + " between "
          + content.object1 + " and " + content.object2);
    }

    return null;
  }

  /**
   * Creates a new termination, with parameters passed as TerminationContent.
   * 
   * @param content parameters for the termination condition, including its class.
   * @return the new termination, created and initialized, ready to be checked!
   */
  public Termination createTermination(TerminationContent content) {
    try {
      Class terminationClass = VGDLFactory.registeredTerminations.get(content.identifier);
      Constructor terminationConstructor =
          terminationClass.getConstructor(new Class[] {TerminationContent.class});
      return (Termination) terminationConstructor.newInstance(content);

    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("Error creating termination condition " + content.identifier);
    }

    return null;
  }

  /**
   * Parses the parameters from content, assigns them to variables in obj.
   * 
   * @param content contains the parameters to read.
   * @param obj object with the variables to assign.
   */
  public void parseParameters(Content content, Object obj) {
    // Get all fields from the class and store it as key->field
    Field[] fields = obj.getClass().getFields();
    Map<String, Field> fieldMap = new HashMap<>();
    for (Field field : fields) {
      String strField = field.toString();
      int lastDot = strField.lastIndexOf('.');
      String fieldName = strField.substring(lastDot + 1).trim();

      fieldMap.put(fieldName, field);
    }
    // Check all parameters from content
    for (String parameter : content.parameters.keySet()) {
      String value = content.parameters.get(parameter);
      if (fieldMap.containsKey(parameter)) {

        Object objVal;
        try {
          Field cfield = Types.class.getField(value);
          objVal = cfield.get(null);
        } catch (Exception ignored) {
          try {
            objVal = Integer.parseInt(value);

          } catch (NumberFormatException ignored0) {
            try {
              objVal = Double.parseDouble(value);
            } catch (NumberFormatException ignored1) {
              try {
                objVal =
                    "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value) ? Boolean
                        .parseBoolean(value) : value;
              } catch (NumberFormatException ignored2) {
                objVal = value;
              }
            }
          }
        }
        try {
          fieldMap.get(parameter).set(obj, objVal);
        } catch (Exception e) {
          e.printStackTrace();
        }
      } else {
        System.out.println("Unknown field (" + parameter + '=' + value + ") from " + content);
      }
    }

  }

  /**
   * Returns the value of an int field in the object specified
   * 
   * @param obj object that holds the field.
   * @param fieldName name of the field to retrieve.
   * @return the value, or -1 if the parameter does not exist or it is not an int.
   */
  public int requestFieldValueInt(Object obj, String fieldName) {
    // Get all fields from the class and store it as key->field
    Field[] fields = obj.getClass().getFields();
    for (Field field : fields) {
      String strField = field.getName();
      if (strField.equalsIgnoreCase(fieldName)) {
        try {
          Object objVal = field.get(obj);
          return (Integer) objVal;
        } catch (Exception ignored) {
          System.out.println("ERROR: invalid requested int parameter " + fieldName);
          return -1;
        }
      }
    }
    return -1;
  }

}
