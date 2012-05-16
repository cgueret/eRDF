/**
 * 
 */
package nl.erdf.model.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import nl.erdf.datalayer.DataLayer;
import nl.erdf.model.ResourceProvider;
import nl.erdf.model.Solution;

import org.openrdf.model.Value;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class StatementPatternSetProvider implements ResourceProvider {
	// The graph pattern is a triple with variables in it
	private final List<StatementPatternProvider> providers = new ArrayList<StatementPatternProvider>();
	private final String context;

	/**
	 * @param context
	 */
	public StatementPatternSetProvider(String context) {
		this.context = context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.model.ResourceProvider#getResource(java.lang.String,
	 * nl.erdf.model.Solution, nl.erdf.datalayer.DataLayer)
	 */
	public Value getResource(String variable, Solution solution, DataLayer dataLayer) {
		Random rand = new Random();
		return providers.get(rand.nextInt(providers.size())).getResource(variable, solution, dataLayer);
	}

	/**
	 * @param provider
	 */
	public void add(StatementPatternProvider provider) {
		providers.add(provider);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "StatementPatternSetProvider [providers=" + providers + "]";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.model.ResourceProvider#getVariables()
	 */
	public Set<String> getVariables() {
		Set<String> result = new HashSet<String>();
		for (StatementPatternProvider provider : providers)
			result.addAll(provider.getVariables());
		return result;
	}

	/**
	 * @return the context
	 */
	public String getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.model.ResourceProvider#getNumberResources(java.lang.String,
	 * nl.erdf.model.Solution, nl.erdf.datalayer.DataLayer)
	 */
	public long getNumberResources(String variableName, Solution solution, DataLayer dataLayer) {
		long total = 0;
		for (StatementPatternProvider provider : providers)
			total += provider.getNumberResources(variableName, solution, dataLayer);
		return total;
	}

}
