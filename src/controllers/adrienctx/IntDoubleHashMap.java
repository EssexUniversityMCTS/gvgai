/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers.adrienctx;

import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author acouetoux
 */
public class IntDoubleHashMap {
    
    public HashMap<Integer, Double> hashMap;
    
    public IntDoubleHashMap (){
        hashMap = new HashMap<Integer, Double>();
    }
    
    public Double get(Integer key){
        return hashMap.get(key);
    }
    
    public void put(Integer key, Double value){
        hashMap.put(key, value);
    }
    
    public Set<Integer> keySet(){
        return hashMap.keySet();
    }
    
    public boolean containsKey(Integer key){
        return hashMap.containsKey(key);
    }
    
}
