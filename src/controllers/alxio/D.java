/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers.alxio;

import ontology.Types;

/**
 *
 * @author ALX
 */

public class D {
    public static IntPair[] get;
    public static void init(Types.ACTIONS[] actions) {
        get = new IntPair[actions.length];
        for(int i=0;i<actions.length;++i){
            switch(actions[i]){
                case ACTION_DOWN:
                    get[i] = new IntPair(0, 1);
                    break;
                case ACTION_UP:
                    get[i] = new IntPair(0, -1);
                    break;
                case ACTION_LEFT:
                    get[i] = new IntPair(-1, 0);
                    break;
                case ACTION_RIGHT:
                    get[i] = new IntPair(1, 0);
                    break;
            }
        }
    }
}
