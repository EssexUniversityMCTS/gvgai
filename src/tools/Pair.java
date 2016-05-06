package tools;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 20/10/13
 * Time: 17:07
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Pair<T,U> implements Map.Entry<T, U>, Comparable{

    public T first;
    public U second;

    public Pair(T first, U second)
    {
        this.first = first;
        this.second = second;
    }

    @Override
    public T getKey() {
        return first;
    }

    @Override
    public U getValue() {
        return second;
    }

    @Override
    public U setValue(U value) {
        second = value;
        return second;
    }

    @Override
    public int compareTo(Object o) {
        try{
            Pair p = (Pair)o;

            //if(p.first == this.first && p.second == this.second)
            if(p.first.equals(this.first) && p.second.equals(this.second))
                return 0;

        }catch(ClassCastException e)
        {
            //Whatever this is, this is not a Pair. So, not equal at all.
            return -1;
        }
        return -1; //Not sure what would the order be, so always -1.
    }

    @Override
    public boolean equals(Object obj) {
        return (compareTo(obj)==0);
    }

    public Pair copy()  { return new Pair(first, second); }
}