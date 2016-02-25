package ontology;

import java.awt.Color;
import java.awt.event.KeyEvent;

import tools.Vector2d;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 17/10/13
 * Time: 11:05
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Types {
    public static final int PHYSICS_NONE = -1;
    public static final int PHYSICS_GRID = 0;
    public static final int PHYSICS_CONT = 1;
    public static final int PHYSICS_NON_FRICTION = 2;
    public static final int PHYSICS_GRAVITY = 3;

    public static final int VGDL_GAME_DEF = 0;
    public static final int VGDL_SPRITE_SET = 1;
    public static final int VGDL_INTERACTION_SET = 2;
    public static final int VGDL_LEVEL_MAPPING = 3;
    public static final int VGDL_TERMINATION_SET = 4;

    public static final Vector2d NIL = new Vector2d(-1, -1);

    public static final Vector2d NONE = new Vector2d(0, 0);
    public static final Vector2d RIGHT = new Vector2d(1, 0);
    public static final Vector2d LEFT = new Vector2d(-1, 0);
    public static final Vector2d UP = new Vector2d(0, -1);
    public static final Vector2d DOWN = new Vector2d(0, 1);

    public static final Vector2d[] BASEDIRS = new Vector2d[]{UP, LEFT, DOWN, RIGHT};

    public static int[] ALL_ACTIONS = new int[]{KeyEvent.VK_UP, KeyEvent.VK_LEFT, KeyEvent.VK_DOWN,
                                                KeyEvent.VK_RIGHT, KeyEvent.VK_SPACE, KeyEvent.VK_ESCAPE};
    public static enum ACTIONS {
        ACTION_NIL(new int[]{0}),
        ACTION_UP(new int[]{KeyEvent.VK_UP}),
        ACTION_LEFT(new int[]{KeyEvent.VK_LEFT}),
        ACTION_DOWN(new int[]{KeyEvent.VK_DOWN}),
        ACTION_RIGHT(new int[]{KeyEvent.VK_RIGHT}),
        ACTION_USE(new int[]{KeyEvent.VK_SPACE}),
        ACTION_ESCAPE(new int[]{KeyEvent.VK_ESCAPE});

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
            if (move == UP) return ACTION_UP;
            else if (move == DOWN) return ACTION_DOWN;
            else if (move == LEFT) return ACTION_LEFT;
            else if (move == RIGHT) return ACTION_RIGHT;
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

    public static enum MOVEMENT {
        STILL,
        ROTATE,
        MOVE
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
    public static final Color LIGHTYELLOW = new Color(255, 250, 128);
    public static final Color LIGHTGRAY = new Color(238, 238, 238);
    public static final Color DARKGRAY = new Color(30, 30, 30);
    public static final Color DARKBLUE = new Color(20, 20, 100);

    public static final Integer[] COLOR_DISC = new Integer[]{20, 80, 140, 200};

    public static final int TYPE_AVATAR = 0;
    public static final int TYPE_RESOURCE = 1;
    public static final int TYPE_PORTAL = 2;
    public static final int TYPE_NPC = 3;
    public static final int TYPE_STATIC = 4;
    public static final int TYPE_FROMAVATAR = 5;
    public static final int TYPE_MOVABLE = 6;
}
