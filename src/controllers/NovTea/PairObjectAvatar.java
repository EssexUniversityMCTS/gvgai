package controllers.NovTea;

public class PairObjectAvatar {

	AvatarState a;
	ObjectState o;
	
	public PairObjectAvatar(ObjectState o2, AvatarState a2){
		a = a2;
		o = o2;
	}
	
	@Override
    public int hashCode() {
		int code;
		
		code = o.hashCode() + a.hashCode();
		
		return code;
	}
	
	
	@Override
    public boolean equals(Object obj){
		if (!(obj instanceof PairObjectAvatar))
            return false;
		
		PairObjectAvatar poa = (PairObjectAvatar) obj;
		
		if ((poa.o).equals(o) && (poa.a).equals(a)) return true;
		
		return false;
	}
	
}
