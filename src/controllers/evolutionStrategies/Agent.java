package controllers.evolutionStrategies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import controllers.Heuristics.SimpleStateHeuristic;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;

public class Agent extends AbstractPlayer{
	private int maxLength;
	private Random random;
	private ArrayList<Types.ACTIONS> actions;
	private int lambda;
	private int mu;
	
	public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer){
		random = new Random();
		actions = stateObs.getAvailableActions();
		
		maxLength = 8;
		lambda = 5;
		mu = 5;
		
	}
	
	private Chromosome rouletteWheelSelection(ArrayList<Chromosome> population){
		double[] probabilities = new double[population.size()];
		probabilities[0] = population.get(0).getFitness();
		for(int i=1; i<population.size(); i++){
			probabilities[i] = probabilities[i-1];
			if(population.get(i).getFitness() > 0){
				probabilities[i] = probabilities[i-1] + population.get(i).getFitness();
			}
		}
		
		for(int i=0; i<probabilities.length; i++){
			probabilities[i] = probabilities[i] / probabilities[probabilities.length - 1];
		}
		
		double prob = random.nextDouble();
		
		for(int i=0; i<probabilities.length; i++){
			if(prob < probabilities[i]){
				return population.get(i);
			}
		}
		
		return population.get(random.nextInt(population.size()));
	}
	
	@Override
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		ArrayList<Chromosome> chromosomes = new ArrayList<Chromosome>();
		for(int i=0; i<lambda+mu; i++){
			chromosomes.add(new Chromosome(stateObs, actions, random, maxLength));
		}
		
		double worstTime = 10;
		double avgTime = 10;
		double totalTime = 0;
		double numberOfTime = 0;
		
		Collections.sort(chromosomes);
		while(elapsedTimer.remainingTimeMillis() > 2 * avgTime && 
				elapsedTimer.remainingTimeMillis() > worstTime){
			ElapsedCpuTimer time = new ElapsedCpuTimer();
			
			for(int i=0; i<lambda; i++){
				chromosomes.remove(chromosomes.size() - 1);
			}
			
			while(chromosomes.size() < lambda + mu){
				Chromosome c = rouletteWheelSelection(chromosomes);
				chromosomes.add(c.getMutation(random));
			}
			
			Collections.sort(chromosomes);
			
			totalTime += time.elapsedMillis();
			numberOfTime += 1;
			avgTime = totalTime / numberOfTime;
		}
		
		return chromosomes.get(0).firstAction();
	}

	private class Chromosome implements Comparable<Chromosome>{
		private ArrayList<Types.ACTIONS> list;
		private double fitness;
		private StateObservation stateObs;
		
		public Chromosome(StateObservation stateObs, ArrayList<Types.ACTIONS> actions, Random random, int length){
			this.stateObs = stateObs;
			this.list = new ArrayList<Types.ACTIONS>();
			StateObservation newState = stateObs.copy();
			for(int i=0; i<length; i++){
				Types.ACTIONS act = actions.get(random.nextInt(actions.size()));
				list.add(act);
				newState.advance(act);
			}
			SimpleStateHeuristic win = new SimpleStateHeuristic(newState);
			fitness = win.evaluateState(newState);
		}
		
		public Chromosome(StateObservation stateObs, ArrayList<Types.ACTIONS> list){
			this.stateObs = stateObs;
			this.list = list;
			StateObservation newState = stateObs.copy();
			for(int i=0; i<list.size(); i++){
				newState.advance(list.get(i));
			}
			SimpleStateHeuristic win = new SimpleStateHeuristic(newState);
			fitness = win.evaluateState(newState);
		}
		
		public Chromosome getMutation(Random random){
			ArrayList<Types.ACTIONS> newList = (ArrayList<Types.ACTIONS>)list.clone();
			
			int index = random.nextInt(newList.size());
			Types.ACTIONS act = actions.get(random.nextInt(actions.size()));
			newList.set(index, act);
			
			return new Chromosome(stateObs, newList);
		}
		
		public double getFitness(){
			return fitness;
		}
		
		public Types.ACTIONS firstAction(){
			return list.get(0);
		}
		
		@Override
		public int compareTo(Chromosome c) {
			if(this.fitness > c.fitness){
				return -1;
			}
			
			if(this.fitness <= c.fitness){
				return 1;
			}
			
			return 0;
		}
	}
}
