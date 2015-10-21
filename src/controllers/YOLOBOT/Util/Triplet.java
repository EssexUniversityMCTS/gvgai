package controllers.YOLOBOT.Util;

public class Triplet<T1,T2,T3> {
	public final T1 Item1;
    public final T2 Item2;
    public final T3 Item3;
    
    public Triplet(T1 item1, T2 item2, T3 item3) {
        this.Item1 = item1;
        this.Item2 = item2;
        this.Item3 = item3;
    }

    @SuppressWarnings("rawtypes")
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Triplet)) return false;
        @SuppressWarnings("unchecked")
        Triplet<T1,T2,T3> triplet = (Triplet) o;
        return Item1 == triplet.Item1 && Item2 == triplet.Item2 && Item3 == triplet.Item3;
    }

    @Override
    public int hashCode() {
        int result = Item1.hashCode();
        result = 31 * result + Item2.hashCode();
        result = 31 * result + Item3.hashCode();
        return result;
    }
}
