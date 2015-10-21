package controllers.NovTea;

import core.game.Observation;

public class ObjectState {

	public int posX;
	public int posY;
	public int oCategory;
	public int oItype;
	
	public ObjectState(Observation obs){
		posX = (int) obs.position.x;
		posY = (int) obs.position.y;
		oCategory = obs.category;
		oItype = obs.itype;		
	}
	
	@Override
    public int hashCode() {
		int code;
		
		code = posX + posY - posX * posY + oCategory * posX - oItype * posY - oCategory + 3 * oItype;
		
		code += 1000 * posX - 10000 * posY;
		
		code += 1563 * (oItype - oCategory);
		
		return code;
	}
	
	
	@Override
    public boolean equals(Object obj){
		if (!(obj instanceof ObjectState))
            return false;
		
		ObjectState os = (ObjectState) obj;
		
		if (os.posX != posX) return false;
		if (os.posY != posY) return false;
		if (os.oItype != oItype) return false;
		if (os.oCategory != oCategory) return false;
		
		return true;
	}
	
}
