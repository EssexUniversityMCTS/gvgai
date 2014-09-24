package controllers.puzzleSolver;

import tools.Vector2d;

public class Moveable {
	public Vector2d pos = new Vector2d();
	public int type = -1;
	
	public Moveable(Vector2d pos, int type){
		this.pos = pos;
		this.type = type;
	}
	
	@Override
	public String toString() {
		return "Moveable: " + type + ", pos: " + pos;
	}
	

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pos == null) ? 0 : pos.hashCode());
		result = prime * result + type;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Moveable other = (Moveable) obj;
		if (pos == null) {
			if (other.pos != null)
				return false;
		} else if (!pos.equals(other.pos))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	public boolean equals(Moveable m) {
		if (type == m.type && pos.equals(m.pos)) return true;
		return false;
	}
}
