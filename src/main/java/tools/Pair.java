package tools;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 20/10/13
 * Time: 17:07
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Pair implements Map.Entry<Integer, Integer>, Comparable{

    public Integer first;
    public Integer second;

    public Pair(Integer first, Integer second)
    {
        this.first = first;
        this.second = second;
    }

    @Override
    public Integer getKey() {
        return first;
    }

    @Override
    public Integer getValue() {
        return second;
    }

    @Override
    public Integer setValue(Integer value) {
        second = value;
        return second;
    }

    @Override
    public int compareTo(Object o) {
        try{
            Pair p = (Pair)o;

            if(p.first == this.first && p.second == this.second)
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
}