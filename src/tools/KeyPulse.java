package tools;


import ontology.Types;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * This class is used to manage the key input.
 * Created by Diego Perez, University of Essex.
 * Date: 20/12/11
 */
@SuppressWarnings("unchecked")
public class KeyPulse extends KeyHandler {

    //Pulses holds the key presses, although they are not mapped to actions until they are released.
    private boolean[] pulses = new boolean[1000];

    //Queue with pulses, to be mapped into actions as they were created (on key releases).
    private LinkedList<Pulse> pulsesFIFO[];

    private HashMap<Integer, Integer> keyRecord;


    public KeyPulse(int no_players)
    {
        keyRecord = new HashMap<>();
        pulsesFIFO = new LinkedList[no_players];
        for(int i = 0; i < no_players; ++i)
            pulsesFIFO[i] = new LinkedList<Pulse>();
    }

    //Sets the mask for this cycle. In this KeyHandler, only one action per frame is guaranteed:
    public void setMask(int playerID)
    {
        reset(playerID); //void the mask
        poll(playerID); //check if there's any pending event on the queue
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
        int registered = getRegisteredID(key);
        if(registered!=-1 && registered<pulsesFIFO.length) {
            pulsesFIFO[registered].addLast(new Pulse(key));
            pulses[key] = false;
        }
    }

    //Polls from the FIFO queue, if there's any action to apply.
    private void poll(int playerID)
    {
        if(pulsesFIFO[playerID].size() > 0)
            key_mask[pulsesFIFO[playerID].poll().key] = true;
    }


    private int getRegisteredID(int key)
    {
        //Gets the player this key is registered for.
        if(keyRecord.containsKey(key))
            return keyRecord.get(key);

        for (int i = 0; i < Types.ALL_ACTIONS.length ; i++) {
            for (int j = 0; j < Types.ALL_ACTIONS[i].length ; j++) {
               if(key == Types.ALL_ACTIONS[i][j])
               {
                   keyRecord.put(i,key);
                   return i;
               }
            }
        }
        return -1;
    }

    //Private class to handle complete presses (down and up)
    private class Pulse{
        int key;

        public Pulse(int key)  {
            this.key = key;
        }

    }

}
