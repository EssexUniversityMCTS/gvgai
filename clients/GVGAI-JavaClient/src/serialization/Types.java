package serialization;

/**
 * Created by Daniel on 16.03.2017.
 */
public class Types {

    /**
     * All actions available.
     */
    public static enum ACTIONS {
        ACTION_NIL,
        ACTION_UP,
        ACTION_LEFT,
        ACTION_DOWN,
        ACTION_RIGHT,
        ACTION_USE,
        ACTION_ESCAPE;

        private static ACTIONS[] vals = values();

        public ACTIONS next(){
            return vals[(this.ordinal()+1) % vals.length];
        }
    }

    /**
     * Winner of the game.
     */
    public static enum WINNER {
        PLAYER_DISQ(-100),
        NO_WINNER(-1),
        PLAYER_LOSES(0),
        PLAYER_WINS(1);

        private int key;
        WINNER(int val) {key=val;}
        public int key() {return key;}
    }

    public static enum LEARNING_SSO_TYPE {
        IMAGE,
        JSON,
        BOTH
    }
}
