package controllers.geneticAlgorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import controllers.Heuristics.WinScoreHeuristic;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;

public class Agent extends AbstractPlayer{
	private int maxLength;
	private Random random;
	private ArrayList<Types.ACTIONS> actions;
	private double crossOver;
	private double mutation;
	private int populationSize;
	
	public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer){
		random = new Random();
		actions = stateObs.getAvailableActions();
		
		maxLength = 10;
		crossOver = 0.7;
		mutation = 0.1;
		populationSize = 10;
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
			probabilities[i] = probabilities[i] / (probabilities[probabilities.length - 1]);
		}
		
		double prob = random.nextDouble();
		
		for(int i=0; i<probabilities.length; i++){
			if(prob < probabilities[i]){
				return population.get(i);
			}
		}
		
		return population.get(random.nextInt(populationSize));
	}
	
	@Override
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		ArrayList<Chromosome> chromosomes = new ArrayList<Chromosome>();
		for(int i=0; i<populationSize; i++){
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
			
			ArrayList<Chromosome> newChromosomes = new ArrayList<Chromosome>();
			while(newChromosomes.size() < populationSize){
				Chromosome c1 = rouletteWheelSelection(chromosomes);
				Chromosome c2 = rouletteWheelSelection(chromosomes);
				if(random.nextDouble() < crossOver){
					newChromosomes.addAll(c1.getChildren(c2, random));
					if(random.nextDouble() < mutation){
						newChromosomes.set(newChromosomes.size() - 2, 
								newChromosomes.get(newChromosomes.size() - 2).getMutation(random));
					}
					if(random.nextDouble() < mutation){
						newChromosomes.set(newChromosomes.size() - 1, 
								newChromosomes.get(newChromosomes.size() - 1).getMutation(random));
					}
				}
				else{
					newChromosomes.add(c1);
					newChromosomes.add(c2);
				}
			}
			
			chromosomes = newChromosomes;
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
			WinScoreHeuristic win = new WinScoreHeuristic(stateObs);
			fitness = win.evaluateState(newState);
		}
		
		public Chromosome(StateObservation stateObs, ArrayList<Types.ACTIONS> list){
			this.stateObs = stateObs;
			this.list = list;
			StateObservation newState = stateObs.copy();
			for(int i=0; i<list.size(); i++){
				newState.advance(list.get(i));
			}
			WinScoreHeuristic win = new WinScoreHeuristic(stateObs);
			fitness = win.evaluateState(newState);
		}
		
		public Chromosome getMutation(Random random){
			ArrayList<Types.ACTIONS> newList = (ArrayList<Types.ACTIONS>)list.clone();
			
			int index = random.nextInt(newList.size());
			Types.ACTIONS act = actions.get(random.nextInt(actions.size()));
			newList.set(index, act);
			
			return new Chromosome(stateObs, newList);
		}
		
		public ArrayList<Chromosome> getChildren(Chromosome chromosome, Random random){
			ArrayList<Chromosome> chromosomes = new ArrayList<Chromosome>();
			
			int crossOver = random.nextInt(this.list.size() - 2) + 1;
			
			ArrayList<Types.ACTIONS> actions1 = new ArrayList<Types.ACTIONS>();
			ArrayList<Types.ACTIONS> actions2 = new ArrayList<Types.ACTIONS>();
			
			for(int i=0; i < this.list.size(); i++){
				if(i<crossOver){
					actions1.add(this.list.get(i));
					actions2.add(chromosome.list.get(i));
				}
				else{
					actions1.add(chromosome.list.get(i));
					actions2.add(this.list.get(i));
				}
			}
			
			chromosomes.add(new Chromosome(stateObs, actions1));
			chromosomes.add(new Chromosome(stateObs, actions2));
			
			return chromosomes;
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
