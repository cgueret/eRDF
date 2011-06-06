package nl.erdf.optimizer;

import java.util.Collection;
import java.util.Random;
import java.util.SortedSet;

import nl.erdf.datalayer.DataLayer;
import nl.erdf.datalayer.QueryPattern;
import nl.erdf.model.Binding;
import nl.erdf.model.Request;
import nl.erdf.model.ResourceProvider;
import nl.erdf.model.Solution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Variable;

/**
 * @author tolgam
 * 
 */
public class Generate {
	/** Logger */
	final Logger logger = LoggerFactory.getLogger(Generate.class);

	/** Random number generator */
	private static final Random twister = new Random();
	private final DataLayer datalayer;
	private final Request request;

	/**
	 * @param datalayer
	 * @param request
	 *            the request to solve
	 */
	public Generate(DataLayer datalayer, Request request) {
		this.datalayer = datalayer;
		this.request = request;
	}

	/**
	 * Create a set of new candidate solutions. The set created is duplicate
	 * free and a duplicate count is set for all individuals
	 * 
	 * @param population
	 * @param target
	 * @param offspringSize
	 * 
	 */
	public void createPopulation(SortedSet<Solution> population, Collection<Solution> target, int offspringSize) {
		// Prepare an array version of the source population for easy rotate
		Solution[] sources = new Solution[population.size()];
		int sourceIndex = 0;
		for (Solution solution : population)
			sources[sourceIndex++] = solution;
		sourceIndex = 0;

		// Generate a duplicate free list of Resources to use
		for (int i = 0; i < offspringSize; i++) {
			// Get the next parent source and duplicate it
			// logger.info("Clone source " + sourceIndex);
			Solution solution = (Solution) sources[sourceIndex].clone();
			sourceIndex = (sourceIndex + 1) % population.size();

			// By default, mutate the first binding
			Binding binding = (Binding) solution.bindings().toArray()[0];
			// logger.info(binding.toString());

			// If more than 1, select a variable to mutate
			if (solution.bindings().size() > 1) {
				double max = 0;
				Roulette rouletteVariable = new Roulette();
				for (Binding b : solution.bindings()) {
					double val = request.getMaximumReward(b.getVariable()) - b.getReward();
					val = 1; // FIXME hack
					rouletteVariable.add(b, val);
					if (val > max)
						max = val;
				}

				// Gives blank bindings more importance
				// for (Entry entry : rouletteVariable.content())
				// if (((Binding) entry.object).getValue().equals(Node.NULL))
				// entry.value = max / 2;
				rouletteVariable.prepare();
				binding = ((Binding) rouletteVariable.nextElement());
			}
			logger.info("Mutate " + binding.getVariable());

			// Pick up one of the providers able to mutate that variable
			// then, get a resource from the datalayer and assign it
			Node_Variable variable = binding.getVariable();
			ResourceProvider provider = getProvider(variable, solution);
			if (provider != null) {
				QueryPattern query = provider.getQuery(variable, solution);
				logger.info("Use provider " + query);
				Node resource = datalayer.getRandomResource(twister, query);
				binding.setValue(resource);
				logger.info("New value " + resource);
			} else {
				logger.info("No provider");
			}

			// Add to the target population
			if (!target.contains(solution))
				target.add(solution);
		}

		logger.info("Created " + target.size() + " new individuals (maximum " + (offspringSize + population.size())
				+ ")");
	}

	private ResourceProvider getProvider(Node_Variable variable, Solution solution) {
		// Find the highest selectivity
		float maxS = -1;
		for (ResourceProvider provider : request.getProvidersFor(variable)) {
			QueryPattern query = provider.getQuery(variable, solution);
			if (!query.contains(Node.NULL)) {
				long selectivity = datalayer.getNumberOfResources(query);
				selectivity = 1;// FIXME hack
				if (selectivity > maxS)
					maxS = selectivity;
			}
		}

		// Build a roulette
		Roulette rouletteProvider = new Roulette();
		for (ResourceProvider provider : request.getProvidersFor(variable)) {
			QueryPattern query = provider.getQuery(variable, solution);
			if (!query.contains(Node.NULL)) {
				long selectivity = datalayer.getNumberOfResources(query);

				// Initial expectation depends on the reward
				double expectation = provider.getExpectedReward(request, variable, solution);

				// Adjust with the selectivity
				double minProb = 0.1;
				double maxProb = 0.9;
				double expo = (maxS == 1 ? 0 : (Math.log(minProb) - Math.log(maxProb)) / Math.log(maxS));
				if (selectivity > 0)
					expectation *= maxProb * Math.pow(selectivity, expo);
				else if (selectivity < 0)
					expectation *= minProb;
				else if (selectivity == 0)
					expectation = 0;

				// Add to the roulette
				rouletteProvider.add(provider, expectation);
			}
		}

		if (!rouletteProvider.hasMoreElements())
			return null;

		rouletteProvider.prepare();
		return (ResourceProvider) rouletteProvider.nextElement();
	}
}
