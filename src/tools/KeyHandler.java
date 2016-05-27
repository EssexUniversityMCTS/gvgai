package tools;

import ontology.Types;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Created by dperez on 25/10/15.
 */
public abstract class KeyHandler extends KeyAdapter {

    //Mask with the actions.
    protected boolean[] key_mask  = new boolean[1000];

    /**
     * @return key mask with pressed keys having values of False.
     */
    public boolean[] getMask()
    {
        return key_mask;
    }

    public void resetAll() {
        for (int i = 0; i < Types.ALL_ACTIONS.length ; i++) {
            for (int j = 0; j < Types.ALL_ACTIONS[i].length ; j++) {
                key_mask[Types.ALL_ACTIONS[i][j]] = false;
            }
        }
    }

    public void reset(int playerID) {
        for (int i = 0; i < Types.ALL_ACTIONS[playerID].length ; i++) {
            key_mask[Types.ALL_ACTIONS[playerID][i]] = false;
        }
    }

    public void setAction(Types.ACTIONS action, int idx) {
        key_mask[action.getKey()[idx]] = true;
    }


    public void setMask(int playerID) { }


    /**
     * Manages KeyPressed events
     * @param e the event.
     */
    public abstract void keyPressed(KeyEvent e);

    /**
     * Manages keyReleased events
     * @param e the event.
     */
    public abstract void keyReleased(KeyEvent e);

}
