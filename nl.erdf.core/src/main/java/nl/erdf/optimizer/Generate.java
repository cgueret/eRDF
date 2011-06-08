package nl.erdf.optimizer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import nl.erdf.datalayer.DataLayer;
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

	private class Pair {
		final Node_Variable variable;
		final ResourceProvider provider;

		Pair(Node_Variable variable, ResourceProvider provider) {
			this.variable = variable;
			this.provider = provider;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((provider == null) ? 0 : provider.hashCode());
			result = prime * result + ((variable == null) ? 0 : variable.hashCode());
			return result;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Pair other = (Pair) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (provider == null) {
				if (other.provider != null)
					return false;
			} else if (!provider.equals(other.provider))
				return false;
			if (variable == null) {
				if (other.variable != null)
					return false;
			} else if (!variable.equals(other.variable))
				return false;

			return true;
		}

		private Generate getOuterType() {
			return Generate.this;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "Pair [variable=" + variable + ", provider=" + provider + "] ";
		}
	}

	private final DataLayer datalayer;
	private final Request request;

	// Assign an expected reward to each provider
	private final Map<Pair, Double> providerRewards = new HashMap<Pair, Double>();

	// Keeps track of the usage of the providers
	private final Map<Solution, Pair> providerUsage = new HashMap<Solution, Pair>();

	/**
	 * @param datalayer
	 * @param request
	 *            the request to solve
	 */
	public Generate(DataLayer datalayer, Request request) {
		this.datalayer = datalayer;
		this.request = request;

		// Init the rewards
		for (Node_Variable variable : request.variables())
			for (ResourceProvider provider : request.getProvidersFor(variable))
				for (Node_Variable prodvar : provider.getVariables())
					providerRewards.put(new Pair(prodvar, provider), 1.0d);

		// for (Entry<Pair, Double> a : providerRewards.entrySet())
		// logger.info(a.getKey() + "  " + a.getValue());
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
		// Clear the usage stats for the providers
		providerUsage.clear();

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
			//logger.info("Mutate " + binding.getVariable());

			// Pick up one of the providers able to mutate that variable
			// then, get a resource from the datalayer and assign it
			Node_Variable variable = binding.getVariable();
			ResourceProvider provider = getProvider(variable, solution);
			if (provider != null) {
				// logger.info("Use provider " + provider);
				Node resource = provider.getResource(variable, solution, datalayer);
				binding.setValue(resource);
				//logger.info("New value " + resource);

				// Add to the target population
				if (!target.contains(solution)) {
					providerUsage.put(solution, new Pair(variable, provider));
					target.add(solution);
				}
			}
		}

		// logger.info("Created " + target.size() + " new individuals (maximum "
		// + (offspringSize + population.size())
		// + ")");
	}

	private ResourceProvider getProvider(Node_Variable variable, Solution solution) {
		// logger.info("Find provider for " + variable);

		// Find the highest selectivity
		// float maxS = -1;
		// for (ResourceProvider provider : request.getProvidersFor(variable)) {
		// QueryPattern query = provider.getQuery(variable, solution);
		// if (!query.contains(Node.NULL)) {
		// long selectivity = datalayer.getNumberOfResources(query);
		// long selectivity = 1;// FIXME Hack
		// if (selectivity > maxS)
		// maxS = selectivity;
		// }
		// }

		double max = 0;
		for (ResourceProvider provider : request.getProvidersFor(variable)) {
			if (providerRewards.get(new Pair(variable, provider)) > max) {
				max = providerRewards.get(new Pair(variable, provider));
			}
		}

		// Build a roulette
		Roulette rouletteProvider = new Roulette();
		for (ResourceProvider provider : request.getProvidersFor(variable)) {
			// QueryPattern query = provider.getQuery(variable, solution);
			// if (!query.contains(Node.NULL)) {
			// long selectivity = datalayer.getNumberOfResources(query);
			// long selectivity = 1; // FIXME Hack

			// Initial expectation depends on the reward
			// double expectation = provider.getExpectedReward(request,
			// variable, solution);
			double expectation = providerRewards.get(new Pair(variable, provider));
			if (max > 0)
				expectation = expectation / max;
			//expectation = 1;

			// Adjust with the selectivity
			/*
			 * double minProb = 0.1; double maxProb = 0.9; double expo = (maxS
			 * == 1 ? 0 : (Math.log(minProb) - Math.log(maxProb)) /
			 * Math.log(maxS)); if (selectivity > 0) expectation *= maxProb *
			 * Math.pow(selectivity, expo); else if (selectivity < 0)
			 * expectation *= minProb; else if (selectivity == 0) expectation =
			 * 0;
			 */

			// Add to the roulette
			rouletteProvider.add(provider, expectation);
			// logger.info(expectation + " " + provider);
			// }
		}

		if (!rouletteProvider.hasMoreElements())
			return null;

		rouletteProvider.prepare();
		return (ResourceProvider) rouletteProvider.nextElement();
	}

	/**
	 * @param solutions
	 */
	public void updateProviderRewards(Set<Solution> solutions) {
		for (Solution solution : solutions) {
			Pair pair = providerUsage.get(solution);
			if (pair != null) {
				providerRewards.put(pair, providerRewards.get(pair) + solution.getBinding(pair.variable).getReward());
				// logger.info(pair.variable +"=" +
				// solution.getBinding(pair.variable).getValue() + " got " +
				// solution.getBinding(pair.variable).getReward() + " using " +
				// pair.provider);
			}
		}

	}
}
