package controllers.NovTea;

import java.util.ArrayList;
import tools.Vector2d;
import core.game.Observation;
import core.game.StateObservation;

public class AvatarState {

	public int posX;
	public int posY;
	public int oriX;
	public int oriY;
	public int itype;
	
	
	public AvatarState(StateObservation state){
		Vector2d ori = state.getAvatarOrientation();
		Vector2d pos = state.getAvatarPosition();
		int factor = state.getBlockSize();
				
		posX = ((int) pos.x) / factor;
		posY = ((int) pos.y) / factor;
		oriX = (int) ori.x;
		oriY = (int) ori.y;
		
		itype = this.getAvatarItype(state, posX, posY);
	}
	
	
	private int getAvatarItype(StateObservation state, int posX, int posY) {
		ArrayList<Observation>[][] grid = state.getObservationGrid();
		
		if (posX < 0 || posY < 0 || posX >= grid.length || posY >= grid[0].length) return 0;
		ArrayList<Observation> obsPosAvatar = grid[posX][posY];
		Observation auxObs;
		int avatarCat = 0;
		
		for (int k = 0; k < obsPosAvatar.size(); k++){
			auxObs = obsPosAvatar.get(k);
			if (auxObs.category == avatarCat) return auxObs.itype;
		}
		return 0;
	}
	
	
	@Override
    public int hashCode() {
		int code;
		
		code = posX + 100 * posY + 10 * (oriX - oriY) + 5 * (oriX + oriY);
		
		code += itype * 17 + oriX * itype - oriY * itype;
		
		return code;
	}
	
	
	@Override
    public boolean equals(Object obj){
		if (!(obj instanceof AvatarState))
            return false;
		
		AvatarState as = (AvatarState) obj;
		
		if (as.posX != posX) return false;
		if (as.posY != posY) return false;
		if (as.oriX != oriX) return false;
		if (as.oriY != oriY) return false;
		if (as.itype != itype) return false;
		
		return true;
	}
	
}
