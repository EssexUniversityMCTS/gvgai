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
public class IntArrayOfDoubleHashMap {
    public HashMap<Integer, double[]> hashMap;
    
    public IntArrayOfDoubleHashMap (){
        hashMap = new HashMap<Integer, double[]>();
    }
    
    public double[] get(Integer key){
        return hashMap.get(key);
    }
    
    public Set<Integer> keySet(){
        return hashMap.keySet();
    }
    
    public void put(Integer key, double[] value){
        hashMap.put(key, value);
    }
    
    public boolean containsKey(Integer key){
        return hashMap.containsKey(key);
    }
    
}
