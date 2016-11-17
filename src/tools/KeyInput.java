package tools;

import java.awt.event.KeyEvent;

/**
 * This class is used to manage the key input.
 * Created by Diego Perez, University of Essex.
 * Date: 20/12/11
 */
public class KeyInput extends KeyHandler {

    /**
     * Manages KeyPressed events
     * @param e the event.
     */
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        key_mask[key] = true;
    }

    /**
     * Manages keyReleased events
     * @param e the event.
     */
    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        key_mask[key] = false;

    }
}
