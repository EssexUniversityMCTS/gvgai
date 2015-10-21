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
public class Game {
    public static int NUM_ACTIONS;
    public static Types.ACTIONS[] actions;
    
    public static String ActionName(int i){
        if(actions == null || actions.length < i) return null;
        return actions[i].toString();
    }
    
    public static int indexOf(Types.ACTIONS action){
        for(int i=0;i<actions.length;++i){
            if(actions[i] == action) return i;
        }
        return -1;
    }
}
