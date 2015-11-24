package tools;


import java.awt.event.KeyEvent;
import java.util.LinkedList;

/**
 * This class is used to manage the key input.
 * Created by Diego Perez, University of Essex.
 * Date: 20/12/11
 */
public class KeyPulse extends KeyHandler {

    //Pulses holds the key presses, although they are not mapped to actions until they are released.
    private boolean[] pulses = new boolean[1000];

    //Queue with pulses, to be mapped into actions as they were created (on key releases).
    private LinkedList<Pulse> pulsesFIFO = new LinkedList<Pulse>();

    //Sets the mask for this cycle. In this KeyHandler, only one action per frame is guaranteed:
    public void setMask()
    {
        reset(); //void the mask
        poll(); //check if there's any pending event on the queue
    }

    /**
     * Manages KeyPressed events
     * @param e the event.
     */
    public void keyPressed(KeyEvent e) {
        pulses[e.getKeyCode()] = true;
    }

    /**
     * Manages keyReleased events
     * @param e the event.
     */
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        pulsesFIFO.addLast(new Pulse(key));
        pulses[key] = false;
    }

    //Polls from the FIFO queue, if there's any action to apply.
    private void poll()
    {
        if(pulsesFIFO.size() > 0)
            key_mask[pulsesFIFO.poll().key] = true;
    }


    //Private class to handle complete presses (down and up)
    private class Pulse{
        int key;
        public Pulse(int key)  { this.key = key; }

    }

}
