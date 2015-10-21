package controllers.NovTea;

public class PairObjects {

	public ObjectState o1;
	public ObjectState o2;
	
	public PairObjects(ObjectState ob1, ObjectState ob2){
		o1 = ob1;
		o2 = ob2;
	}
	
	@Override
    public int hashCode() {
		int code;
		
		code = o1.hashCode() + o2.hashCode();
		
		return code;
	}
	
	
	@Override
    public boolean equals(Object obj){
		if (!(obj instanceof PairObjects))
            return false;
		
		PairObjects po = (PairObjects) obj;
		
		if ((po.o1).equals(o1) && (po.o2).equals(o2)) return true;
		if ((po.o2).equals(o1) && (po.o1).equals(o2)) return true;
		
		return false;
	}
	
}
