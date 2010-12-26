package nl.erdf.optimizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.SortedSet;

import nl.erdf.datalayer.DataLayer;
import nl.erdf.datalayer.QueryPattern;
import nl.erdf.model.Binding;
import nl.erdf.model.Constraint;
import nl.erdf.model.Request;
import nl.erdf.model.Solution;
import nl.erdf.model.Variable;
import nl.erdf.optimizer.Roulette.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;

/**
 * @author tolgam
 * 
 */
public class Generate {
	/** Logger */
	protected final Logger logger = LoggerFactory.getLogger(Generate.class);
	/** List of providers to use for the generation of new candidate solutions */
	protected final Collection<Provider> providers = new ArrayList<Provider>();
	/** Random number generator */
	private static final Random twister = new Random();
	private final DataLayer datalayer;

	/**
	 * @param datalayer
	 * @param request
	 *           the request to solve
	 */
	public Generate(DataLayer datalayer, Request request) {
		this.datalayer = datalayer;

		// Turn all the constraints from the request into providers
		for (Constraint constraint : request.constraints()) {
			Provider provider = new Provider(datalayer, constraint.getPart(0), constraint.getPart(1),
					constraint.getPart(2));
			providers.add(provider);
		}
		logger.info("Number of providers: " + providers.size());
	}

	/**
	 * Create a set of new candidate solutions. The set created is duplicate free
	 * and a duplicate count is set for all individuals
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

			// If more than 1, select a variable to mutate
			if (solution.bindings().size() > 1) {
				double max = 0;
				Roulette rouletteVariable = new Roulette();
				for (Binding b : solution.bindings()) {
					double val = b.getMaximumReward() - b.getReward();
					rouletteVariable.add(b, val);
					if (val > max)
						max = val;
				}

				// FIXME: Gives blank bindings more importance
				for (Entry entry : rouletteVariable.content())
					if (((Binding) entry.object).getValue().equals(Node.NULL))
						entry.value = max / 2;
				rouletteVariable.prepare();
				binding = ((Binding) rouletteVariable.nextElement());
				// logger.info("Mutate " + binding.getVariable());
			}

			// Pick up one of the providers able to mutate that variable
			// then, get a resource from the datalayer and assign it
			Variable variable = binding.getVariable();
			Provider provider = getProvider(variable, solution);
			if (provider != null) {
				QueryPattern query = provider.getQuery(variable, solution);
				logger.debug("Use provider " + query);
				Node resource = datalayer.getRandomResource(twister, query);
				binding.setValue(resource);
				logger.debug("New value " + resource);
			} else {
				logger.debug("No provider");
			}

			// Add to the target population
			if (!target.contains(solution))
				target.add(solution);
		}

		logger.debug("Created " + target.size() + " new individuals (maximum " + (offspringSize + population.size())
				+ ")");
	}

	private Provider getProvider(Variable variable, Solution solution) {
		// Get a list of all the suitable providers and their selectivity
		List<Provider> p = new ArrayList<Provider>();
		float maxS = -1;
		for (Provider provider : providers) {
			if (provider.appliesFor(variable)) {
				p.add(provider);
				QueryPattern query = provider.getQuery(variable, solution);
				long selectivity = datalayer.getNumberOfResources(query);
				if (selectivity > maxS)
					maxS = selectivity;
			}
		}

		// Build a roulette
		Roulette rouletteProvider = new Roulette();
		for (Provider provider : p) {
			QueryPattern query = provider.getQuery(variable, solution);
			long selectivity = datalayer.getNumberOfResources(query);

			// Initial expectation depends on the reward
			double expectation = provider.getExpectedReward(variable, solution);

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

		if (!rouletteProvider.hasMoreElements())
			return null;

		rouletteProvider.prepare();
		return (Provider) rouletteProvider.nextElement();
	}
}
