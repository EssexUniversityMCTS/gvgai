package tools;

import ontology.Types;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

/**
 * This class is used to manage the key input.
 * PTSP-Competition
 * Created by Diego Perez, University of Essex.
 * Date: 20/12/11
 */
public class KeyInput extends KeyAdapter
{
    private boolean[] key_mask  = new boolean[1000];

    /**
     * QUIT action (ESC key stroke). Obviously, quits the game.
     */
    private boolean quit;

    /**
     * @return key mask with pressed keys having values of False.
     */
    public boolean[] getMask()
    {
        return key_mask;
    }


    /**
     * Manages KeyPressed events
     * @param e the event.
     */
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        key_mask[key] = true;
    }

    /**
     * Manages keyReleased events
     * @param e the event.
     */
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        key_mask[key] = false;

    }

    public void reset() {
        //for (int i = 0; i < key_mask.length ; i++) {
        for (int i = 0; i < Types.ALL_ACTIONS.length ; i++) {
            key_mask[Types.ALL_ACTIONS[i]] = false;
        }
    }

    public void setAction(Types.ACTIONS action) {
        for(int i : action.getKey()){
            key_mask[i] = true;
        }
    }
}
