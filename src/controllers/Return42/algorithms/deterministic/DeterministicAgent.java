package controllers.Return42.algorithms.deterministic;

import java.awt.Graphics2D;

import core.game.StateObservation;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;

public interface DeterministicAgent {
	
	public ACTIONS act( StateObservation state, ElapsedCpuTimer timer );
	public void useConstructorExtraTime( StateObservation state, ElapsedCpuTimer timer );
	public boolean didFinish();
	public void draw(Graphics2D g);
	
}
