/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers.alxio;

/**
 *
 * @author ALX
 * @param <A>
 * @param <B>
 */
public class Pair<A,B> {
    public A x;
    public B y;
    public Pair(A a, B b){
        x = a;
        y = b;
    }
}
