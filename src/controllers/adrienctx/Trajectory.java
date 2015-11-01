/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers.adrienctx;

/**
 *
 * @author acouetoux
 */
public class Trajectory {
    private IntDoubleHashMap[] features1;

    private IntDoubleHashMap[] features2;

    public double reward;

    public boolean isFinal;
    
    public int length;
    
    public IntArrayOfDoubleHashMap[] basisFunctionValues1;
    
    public IntArrayOfDoubleHashMap[] basisFunctionValues2;
    
    public Trajectory(IntDoubleHashMap[] _f1, IntDoubleHashMap[] _f2, double _r, boolean _final, int _length, IntArrayOfDoubleHashMap[] _bf1, IntArrayOfDoubleHashMap[] _bf2){
        features1 = _f1;
        features2 = _f2;
        reward = _r;
        isFinal = _final;
        length = _length;
        basisFunctionValues1 = _bf1;
        basisFunctionValues2 = _bf2;
    }
}
