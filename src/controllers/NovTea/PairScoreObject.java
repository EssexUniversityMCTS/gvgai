package controllers.NovTea;

public class PairScoreObject {

	double score;
	ObjectState o;
	
	public PairScoreObject(double sc, ObjectState o2){
		score = sc;
		o = o2;
	}
	
	@Override
    public int hashCode() {
		int code;
		int sc = (int) score;
		
		code = o.hashCode() + sc - sc * sc;
		
		return code;
	}
	
	
	@Override
    public boolean equals(Object obj){
		if (!(obj instanceof PairScoreObject))
            return false;
		
		PairScoreObject pso = (PairScoreObject) obj;
		
		if ((pso.score) == score && (pso.o).equals(o)) return true;
		
		return false;
	}
	
}
