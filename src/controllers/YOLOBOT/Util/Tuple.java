package controllers.YOLOBOT.Util;

public class Tuple<T1,T2> {

    public final T1 Item1;
    public final T2 Item2;

    public Tuple(T1 item1, T2 item2) {
        this.Item1 = item1;
        this.Item2 = item2;
    }

    @SuppressWarnings("rawtypes")
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tuple)) return false;
        @SuppressWarnings("unchecked")
		Tuple<T1,T2> tuple = (Tuple) o;
        return Item1 == tuple.Item1 && Item2 == tuple.Item2;
    }

    @Override
    public int hashCode() {
        int result = Item1.hashCode();
        result = 31 * result + Item2.hashCode();
        return result;
    }

}
