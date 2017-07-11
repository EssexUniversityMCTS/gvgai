package ontology;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.Random;
import tools.Direction;
import tools.Vector2d;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 17/10/13
 * Time: 11:05
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Types {
    public static final int PHYSICS_NONE = -1;
    public static final int GRID = 0;
    public static final int CONT = 1;

    public static final int VGDL_GAME_DEF = 0;
    public static final int VGDL_SPRITE_SET = 1;
    public static final int VGDL_INTERACTION_SET = 2;
    public static final int VGDL_LEVEL_MAPPING = 3;
    public static final int VGDL_TERMINATION_SET = 4;
    public static final int VGDL_PARAMETER_SET = 5;

    public static final Vector2d NIL = new Vector2d(-1, -1);
    public static final Vector2d NONE = new Vector2d(0, 0);
    public static final Vector2d RIGHT = new Vector2d(1, 0);
    public static final Vector2d LEFT = new Vector2d(-1, 0);
    public static final Vector2d UP = new Vector2d(0, -1);
    public static final Vector2d DOWN = new Vector2d(0, 1);
    public static final Vector2d[] BASEDIRS = new Vector2d[]{UP, LEFT, DOWN, RIGHT};

    public static final Direction DNIL = new Direction(-1, -1);
    public static final Direction DNONE = new Direction(0, 0);
    public static final Direction DRIGHT = new Direction(1, 0);
    public static final Direction DLEFT = new Direction(-1, 0);
    public static final Direction DUP = new Direction(0, -1);
    public static final Direction DDOWN = new Direction(0, 1);
    public static final Direction[] DBASEDIRS = new Direction[]{DUP, DLEFT, DDOWN, DRIGHT};

    public static final int NUM_LEARNING_LEVELS = 5;
    public static final int NUM_TRAINING_LEVELS = 3; //NUM_EVALUATION = NUM_LEARNING_LEVELS - NUM_TRAINING_LEVELS
    public static final int LEARNING_RESULT_DISQ = -1;
    public static final int LEARNING_FINISH_ROUND = -2;

    //This is a small method to automatically link and parse vectors to directions.
    public static Field processField(String value)
    {
        Field cfield = null;
        try{
            cfield = Types.class.getField(value);
            Object objVal = cfield.get(null);

            if(objVal instanceof Vector2d) {
                value = _v2DirStr((Vector2d)objVal);
                cfield = Types.class.getField(value);
            }
        }catch(Exception e) { }
        return cfield;
    }

    public static String v2DirStr(Vector2d v)
    {
        if (v.equals(NIL)) return "NIL";
        if (v.equals(NONE)) return "NONE";
        if (v.equals(UP)) return "UP";
        if (v.equals(DOWN)) return "DOWN";
        if (v.equals(LEFT)) return "LEFT";
        if (v.equals(RIGHT)) return "RIGHT";
        return null;
    }

    private static String _v2DirStr(Vector2d v)
    {
        if (v.equals(NIL)) return "DNIL";
        if (v.equals(NONE)) return "DNONE";
        if (v.equals(UP)) return "DUP";
        if (v.equals(DOWN)) return "DDOWN";
        if (v.equals(LEFT)) return "DLEFT";
        if (v.equals(RIGHT)) return "DRIGHT";
        return null;
    }

    public static int DEFAULT_SINGLE_PLAYER_KEYIDX = 0;
    public static int[][] ALL_ACTIONS = new int[][]{    {KeyEvent.VK_UP, KeyEvent.VK_LEFT, KeyEvent.VK_DOWN,
                                                         KeyEvent.VK_RIGHT, KeyEvent.VK_SPACE, KeyEvent.VK_ESCAPE},
                                                        {KeyEvent.VK_W, KeyEvent.VK_A, KeyEvent.VK_S,
                                                         KeyEvent.VK_D, KeyEvent.VK_SHIFT, KeyEvent.VK_ESCAPE}};

    public static enum ACTIONS {
        ACTION_NIL(new int[]{0, 0}),
        ACTION_UP(new int[]{KeyEvent.VK_UP, KeyEvent.VK_W}),
        ACTION_LEFT(new int[]{KeyEvent.VK_LEFT, KeyEvent.VK_A}),
        ACTION_DOWN(new int[]{KeyEvent.VK_DOWN, KeyEvent.VK_S}),
        ACTION_RIGHT(new int[]{KeyEvent.VK_RIGHT, KeyEvent.VK_D}),
        ACTION_USE(new int[]{KeyEvent.VK_SPACE, KeyEvent.VK_SHIFT}),
        ACTION_ESCAPE(new int[]{KeyEvent.VK_ESCAPE, KeyEvent.VK_ESCAPE});

        private int[] key;

        ACTIONS(int[] numVal) {
            this.key = numVal;
        }

        public int[] getKey() {
            return this.key;
        }

        public static ACTIONS fromString(String strKey) {
            if (strKey.equalsIgnoreCase("ACTION_UP")) return ACTION_UP;
            else if (strKey.equalsIgnoreCase("ACTION_LEFT")) return ACTION_LEFT;
            else if (strKey.equalsIgnoreCase("ACTION_DOWN")) return ACTION_DOWN;
            else if (strKey.equalsIgnoreCase("ACTION_RIGHT")) return ACTION_RIGHT;
            else if (strKey.equalsIgnoreCase("ACTION_USE")) return ACTION_USE;
            else if (strKey.equalsIgnoreCase("ACTION_ESCAPE")) return ACTION_ESCAPE;
            else return ACTION_NIL;
        }

        public static ACTIONS fromVector(Vector2d move) {
        	// Probably better to use .equals() instead of == to test for equality,
        	// but not necessary for the current call hierarchy of this method
            if (move.equals(UP)) return ACTION_UP;
            else if (move.equals(DOWN)) return ACTION_DOWN;
            else if (move.equals(LEFT)) return ACTION_LEFT;
            else if (move.equals(RIGHT)) return ACTION_RIGHT;
            else return ACTION_NIL;
        }
        
        public static boolean isMoving(ACTIONS value){
        	return value == ACTIONS.ACTION_UP || value == ACTIONS.ACTION_DOWN ||
        			value == ACTIONS.ACTION_LEFT || value == ACTIONS.ACTION_RIGHT;
        }

        public static ACTIONS reverseACTION(ACTIONS value){
        	if(value == ACTIONS.ACTION_DOWN){
        		return ACTIONS.ACTION_UP;
        	}
        	if(value == ACTIONS.ACTION_UP){
        		return ACTIONS.ACTION_DOWN;
        	}
        	if(value == ACTIONS.ACTION_RIGHT){
        		return ACTIONS.ACTION_LEFT;
        	}
        	if(value == ACTIONS.ACTION_LEFT){
        		return ACTIONS.ACTION_RIGHT;
        	}
        	return ACTIONS.ACTION_NIL;
        }

        public static ACTIONS fromVector(Direction move) {
        	// Probably better to use .equals() instead of == to test for equality,
        	// but not necessary for the current call hierarchy of this method
            if (move.equals(DUP)) return ACTION_UP;
            else if (move.equals(DDOWN)) return ACTION_DOWN;
            else if (move.equals(DLEFT)) return ACTION_LEFT;
            else if (move.equals(DRIGHT)) return ACTION_RIGHT;
            else return ACTION_NIL;
        }

    }


    public static enum WINNER {
        PLAYER_DISQ(-100),
        NO_WINNER(-1),
        PLAYER_LOSES(0),
        PLAYER_WINS(1);

        private int key;
        WINNER(int val) {key=val;}
        public int key() {return key;}
    }

    /**
     * This is an enum type that describes the potential states of the game
     */
    public static enum GAMESTATES{
        INIT_STATE, ACT_STATE, END_STATE, ABORT_STATE, CHOOSE_LEVEL
    }

    public static enum MOVEMENT {
        STILL,
        ROTATE,
        MOVE
    }

    /**
     * This is an enum type that specifies the type of sso required
     */
    public static enum LEARNING_SSO_TYPE {
        IMAGE,
        JSON,
        BOTH
    }

    public static final int SCORE_DISQ = -1000;

    public static final Color GREEN = new Color(0, 200, 0);
    public static final Color BLUE = new Color(0, 0, 200);
    public static final Color RED = new Color(200, 0, 0);
    public static final Color GRAY = new Color(90, 90, 90);
    public static final Color WHITE = new Color(250, 250, 250);
    public static final Color BROWN = new Color(140, 120, 100);
    public static final Color BLACK = new Color(0, 0, 0);
    public static final Color ORANGE = new Color(250, 160, 0);
    public static final Color YELLOW = new Color(250, 250, 0);
    public static final Color PINK = new Color(250, 200, 200);
    public static final Color GOLD = new Color(250, 212, 0);
    public static final Color LIGHTRED = new Color(250, 50, 50);
    public static final Color LIGHTORANGE = new Color(250, 200, 100);
    public static final Color LIGHTBLUE = new Color(50, 100, 250);
    public static final Color LIGHTGREEN = new Color(50, 250, 50);
    public static final Color DARKGREEN = new Color(35, 117, 29);
    public static final Color LIGHTYELLOW = new Color(255, 250, 128);
    public static final Color LIGHTGRAY = new Color(238, 238, 238);
    public static final Color DARKGRAY = new Color(30, 30, 30);
    public static final Color DARKBLUE = new Color(20, 20, 100);

    public static final Color RANDOM = new Color(new Random().nextInt(256), new Random().nextInt(256), new Random().nextInt(256));


    public static final Integer[] COLOR_DISC = new Integer[]{20, 80, 140, 200};

    public static final int TYPE_AVATAR = 0;
    public static final int TYPE_RESOURCE = 1;
    public static final int TYPE_PORTAL = 2;
    public static final int TYPE_NPC = 3;
    public static final int TYPE_STATIC = 4;
    public static final int TYPE_FROMAVATAR = 5;
    public static final int TYPE_MOVABLE = 6;
}
