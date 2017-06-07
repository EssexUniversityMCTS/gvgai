package tracks.ruleGeneration.geneticRuleGenerator;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import core.game.SLDescription;
import core.game.StateObservation;
import core.generator.AbstractRuleGenerator;
import core.player.AbstractPlayer;
import tools.ElapsedCpuTimer;
import tools.LevelAnalyzer;

public class RuleGenerator extends AbstractRuleGenerator{
	/** The best chromosome fitness across generations **/
	private ArrayList<Double> bestFitness;
	/** number of feasible chromosomes across generations **/
	private ArrayList<Integer> numOfFeasible;
	/** number of infeasible chromosomes across generations **/
	private ArrayList<Integer> numOfInFeasible;
	
	/**
	 * initialize the agents used during evaluating the chromosome
	 */
	private void constructAgent(SLDescription sl){
		try{
			Class agentClass = Class.forName(SharedData.BEST_AGENT_NAME);
			Constructor agentConst = agentClass.getConstructor(new Class[]{StateObservation.class, ElapsedCpuTimer.class});
			SharedData.automatedAgent = (AbstractPlayer)agentConst.newInstance(sl.testRules(new String[]{}, new String[]{}), null);
		}
		catch(Exception e){
			e.printStackTrace();
		}

		try{
			Class agentClass = Class.forName(SharedData.NAIVE_AGENT_NAME);
			Constructor agentConst = agentClass.getConstructor(new Class[]{StateObservation.class, ElapsedCpuTimer.class});
			SharedData.naiveAgent = (AbstractPlayer)agentConst.newInstance(sl.testRules(new String[]{}, new String[]{}), null);
		}
		catch(Exception e){
			e.printStackTrace();
		}

		try{
			Class agentClass = Class.forName(SharedData.DO_NOTHING_AGENT_NAME);
			Constructor agentConst = agentClass.getConstructor(new Class[]{StateObservation.class, ElapsedCpuTimer.class});
			SharedData.doNothingAgent = (AbstractPlayer)agentConst.newInstance(sl.testRules(new String[]{}, new String[]{}), null);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		try{
			Class agentClass = Class.forName(SharedData.RANDOM_AGENT_NAME);
			Constructor agentConst = agentClass.getConstructor(new Class[]{StateObservation.class, ElapsedCpuTimer.class});
			SharedData.randomAgent = (AbstractPlayer)agentConst.newInstance(sl.testRules(new String[]{}, new String[]{}), null);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * This is an evolutionary rule generator
	 * @param sl	contains information about sprites and current level
	 * @param time	amount of time allowed to generate
	 */
	public RuleGenerator(SLDescription sl, ElapsedCpuTimer time) {
		SharedData.usefulSprites = new ArrayList<String>();
		SharedData.random = new Random();
		SharedData.la = new LevelAnalyzer(sl);
		
		String[][] currentLevel = sl.getCurrentLevel();
		// Just get the useful sprites from the current level
		for (int y = 0; y < currentLevel.length; y++) {
			for (int x = 0; x < currentLevel[y].length; x++) {
				String[] parts = currentLevel[y][x].split(",");
				for (int i = 0; i < parts.length; i++) {
					if (parts[i].trim().length() > 0) {
						// Add the sprite if it doesn't exisit
						if (!SharedData.usefulSprites.contains(parts[i].trim())) {
						    SharedData.usefulSprites.add(parts[i].trim());
						}
					}
				}
			}
		}
		SharedData.usefulSprites.add("EOS");
		constructAgent(sl);
		SharedData.constGen = new tracks.ruleGeneration.constructiveRuleGenerator.RuleGenerator(sl, time);
		SharedData.constGen.generateRules(sl, time);
	}
	
	private ArrayList<Chromosome> getFirstPopulation(SLDescription sl, String name, int amount, int mutations){
	    	ArrayList<Chromosome> chromosomes = new ArrayList<Chromosome>();
	    	try{
        	    	Class genClass = Class.forName(name);
        	    	Constructor genConst = genClass.getConstructor(new Class[]{SLDescription.class, ElapsedCpuTimer.class});
        	    	AbstractRuleGenerator ruleGen = (AbstractRuleGenerator)genConst.newInstance(sl, null);
            	 	for(int i = 0; i < amount; i++) {
        	 		Chromosome c = new Chromosome(ruleGen.generateRules(sl, null), sl);
        	 		c.cleanseChromosome();
        	 		c.calculateFitness(SharedData.EVALUATION_TIME);
        	 		for(int j = 0; j < mutations; j++) {
        				c.mutate();
        			}
        	 		chromosomes.add(c);
        	 	}
	    	}
	    	catch(Exception e){
	    	    e.printStackTrace();
	    	}
    	 	return chromosomes;
	}
	
	/**
	 * Get the next population based on the current feasible infeasible population
	 * @param fPopulation	array of the current feasible chromosomes
	 * @param iPopulation	array of the current infeasible chromosomes
	 * @return				array of the new chromosomes at the new population
	 */
	private ArrayList<Chromosome> getNextPopulation(ArrayList<Chromosome> fPopulation, ArrayList<Chromosome> iPopulation){
		ArrayList<Chromosome> newPopulation = new ArrayList<Chromosome>();

		//collect some statistics about the current generation
		ArrayList<Double> fitnessArray = new ArrayList<Double>();
		for(int i=0;i<fPopulation.size();i++){
			fitnessArray.add(fPopulation.get(i).getFitness().get(0));
		}

		Collections.sort(fitnessArray);
		if(fitnessArray.size() > 0){
			bestFitness.add(fitnessArray.get(fitnessArray.size() - 1));
		}
		else{
			bestFitness.add((double) 0);
		}
		numOfFeasible.add(fPopulation.size());
		numOfInFeasible.add(iPopulation.size());

		
		// CLEANSING PART
		// cleanse the population 
		for(Chromosome c : fPopulation) {
			c.cleanseChromosome();
		}
		for(Chromosome c: iPopulation) {
			c.cleanseChromosome();
		}
		
		while(newPopulation.size() < SharedData.POPULATION_SIZE){
			//choosing which population to work on with 50/50 probability
			//of selecting either any of them
			ArrayList<Chromosome> population = fPopulation;
			if(fPopulation.size() <= 0){
				population = iPopulation;
			}
			if(SharedData.random.nextDouble() < 0.5){
				population = iPopulation;
				if(iPopulation.size() <= 0){
					population = fPopulation;
				}
			}


			//select the parents using roulettewheel selection
			Chromosome parent1 = rankSelection(population);//rouletteWheelSelection(population);
			Chromosome parent2 = rankSelection(population);//rouletteWheelSelection(population);
			Chromosome child1 = parent1.clone();
			Chromosome child2 = parent2.clone();
			//do cross over
			if(SharedData.random.nextDouble() < SharedData.CROSSOVER_PROB){
				ArrayList<Chromosome> children = parent1.crossover(parent2);
				child1 = children.get(0);
				child2 = children.get(1);


				//do mutation to the children
				if(SharedData.random.nextDouble() < SharedData.MUTATION_PROB){
					child1.mutate();
				}
				if(SharedData.random.nextDouble() < SharedData.MUTATION_PROB){
					child2.mutate();
				}
			}

			//mutate the copies of the parents
			else if(SharedData.random.nextDouble() < SharedData.MUTATION_PROB){
				child1.mutate();
			}
			else if(SharedData.random.nextDouble() < SharedData.MUTATION_PROB){
				child2.mutate();
			}
			
			//add the new children to the new population
			newPopulation.add(child1);
			newPopulation.add(child2);
		}


		//calculate fitness of the new population chromosomes
		for(int i=0;i<newPopulation.size();i++){
		    	newPopulation.get(i).calculateFitness(SharedData.EVALUATION_TIME);
		    	if(newPopulation.get(i).getConstrainFitness() < 1){
				System.out.println("\tChromosome #" + (i+1) + " Constrain Fitness: " + newPopulation.get(i).getConstrainFitness());
        		}
        		else{
        			System.out.println("\tChromosome #" + (i+1) + " Fitness: " + newPopulation.get(i).getFitness());
        		}
		}

			//add the best chromosome(s) from old population to the new population
		Collections.sort(newPopulation);
		for(int i=SharedData.POPULATION_SIZE - SharedData.ELITISM_NUMBER;i<newPopulation.size();){
			newPopulation.remove(i);
		}

		if(fPopulation.isEmpty()){
			Collections.sort(iPopulation);
			for(int i=0;i<SharedData.ELITISM_NUMBER;i++){
				newPopulation.add(iPopulation.get(i));
			}
		}
		else{
			Collections.sort(fPopulation);
			for(int i=0;i<SharedData.ELITISM_NUMBER;i++){
				newPopulation.add(fPopulation.get(i));

			}
		}

		return newPopulation;
	}

	/**
	 * Performs rank selection on the given population
	 * @param population 	the population to be performed upon
	 * @return
	 */
	private Chromosome rankSelection(ArrayList<Chromosome> population) {
		double[] probabilities = new double[population.size()];
		probabilities[0] = 1.0;
		for(int i = 1; i < population.size(); i++) {
			probabilities[i] = probabilities[i-1] + i;
		}
		for(int i = 0; i < probabilities.length; i++) {
			probabilities[i] = probabilities[i] / probabilities[probabilities.length - 1];
		}

		double chosen = SharedData.random.nextDouble();
		for(int i = 0; i < probabilities.length; i++) {
			if(chosen < probabilities[i]) {
				return population.get(i);
			}
		}
		return population.get(0);

	}
	/**
	 * Generates the rules using evolution
	 * @param sl	the SL description
	 * @param time	the time allowed for the generator to loop
	 */
	@Override
	public String[][] generateRules(SLDescription sl, ElapsedCpuTimer time) {
		
		//initialize the statistics objects
 		bestFitness = new ArrayList<Double>();
		numOfFeasible = new ArrayList<Integer>();
		numOfInFeasible = new ArrayList<Integer>();

		System.out.println("Generation #0: ");
		ArrayList<Chromosome> fChromosomes = new ArrayList<Chromosome>();
		ArrayList<Chromosome> iChromosomes = new ArrayList<Chromosome>();
		ArrayList<Chromosome> allChromosomes = new ArrayList<Chromosome>();
		
		allChromosomes.addAll(getFirstPopulation(sl, "tracks.ruleGeneration.constructiveRuleGenerator.RuleGenerator", 
			(int)(SharedData.POPULATION_SIZE * SharedData.INIT_CONSTRUCT_PERCENT), 0));
		allChromosomes.addAll(getFirstPopulation(sl, "tracks.ruleGeneration.randomRuleGenerator.RuleGenerator", 
			(int)(SharedData.POPULATION_SIZE * SharedData.INIT_RANDOM_PERCENT), 0));
		allChromosomes.addAll(getFirstPopulation(sl, "tracks.ruleGeneration.constructiveRuleGenerator.RuleGenerator", 
			(int)(SharedData.POPULATION_SIZE * SharedData.INIT_MUT_PERCENT), SharedData.INIT_MUTATION_AMOUNT));


		//some variables to make sure not getting out of time
		double worstTime = SharedData.EVALUATION_TIME * SharedData.POPULATION_SIZE;
		double avgTime = worstTime;
		double totalTime = 0;
		int numberOfIterations = 0;
		
		// START EVO LOOP
		while(time.remainingTimeMillis() > 2 * avgTime && time.remainingTimeMillis() > worstTime){
			ElapsedCpuTimer timer = new ElapsedCpuTimer();
			System.out.println("Generation #" + (numberOfIterations + 1) + ": ");
			fChromosomes.clear();
			iChromosomes.clear();
			for(Chromosome c:allChromosomes){
				if(c.getConstrainFitness() < 1){
					iChromosomes.add(c);
				}
				else{
					fChromosomes.add(c);
				}
			}
			//get the new population and split it to a the feasible and infeasible populations
			ArrayList<Chromosome> chromosomes = getNextPopulation(fChromosomes, iChromosomes);
			numberOfIterations += 1;
			totalTime += timer.elapsedMillis();
			avgTime = totalTime / numberOfIterations;
			Collections.sort(chromosomes);
			System.out.println("Best Chromosome Fitness: " + chromosomes.get(0).getFitness());
		}


		//return the best infeasible chromosome
		if(fChromosomes.isEmpty()){
			for(int i=0;i<iChromosomes.size();i++){
				iChromosomes.get(i).calculateFitness(SharedData.EVALUATION_TIME);
			}

			Collections.sort(iChromosomes);
			System.out.println("Best Fitness: " + iChromosomes.get(0).getConstrainFitness());
			return iChromosomes.get(0).getRuleset();
		}

		//return the best feasible chromosome otherwise and print some statistics
		for(int i=0;i<fChromosomes.size();i++){
			fChromosomes.get(i).calculateFitness(SharedData.EVALUATION_TIME);
		}
		Collections.sort(fChromosomes);
		System.out.println("Best Chromosome Fitness: " + fChromosomes.get(0).getFitness());
		System.out.println(bestFitness);
		System.out.println(numOfFeasible);
		System.out.println(numOfInFeasible);
		return fChromosomes.get(0).getRuleset();
	}

}
