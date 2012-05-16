/**
 * 
 */
package nl.erdf.model.impl;

import java.util.HashSet;
import java.util.Set;

import nl.erdf.datalayer.DataLayer;
import nl.erdf.model.ResourceProvider;
import nl.erdf.model.Solution;
import nl.erdf.model.Triple;
import nl.erdf.util.Convert;

import org.openrdf.model.Value;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.Var;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class StatementPatternProvider implements ResourceProvider {
	// The graph pattern is a triple with variables in it
	private final StatementPattern pattern;

	/**
	 * @param pattern
	 */
	public StatementPatternProvider(StatementPattern pattern) {
		this.pattern = pattern;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * nl.erdf.model.ResourceProvider#getResource(org.openrdf.query.algebra.Var,
	 * nl.erdf.model.Solution, nl.erdf.datalayer.DataLayer)
	 */
	public Value getResource(String variableName, Solution solution, DataLayer dataLayer) {
		// Instantiate the pattern
		Triple t = Convert.toTriple(pattern, solution);

		// Set back the requested variable to null
		if (pattern.getSubjectVar().getName().equals(variableName))
			t = new Triple(null, t.getPredicate(), t.getObject());
		if (pattern.getPredicateVar().getName().equals(variableName))
			t = new Triple(t.getSubject(), null, t.getObject());
		if (pattern.getObjectVar().getName().equals(variableName))
			t = new Triple(t.getSubject(), t.getPredicate(), null);

		// Get a value and return it
		Value resource = dataLayer.getResource(t);
		return resource;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.model.ResourceProvider#getNumberResources(java.lang.String,
	 * nl.erdf.model.Solution, nl.erdf.datalayer.DataLayer)
	 */
	public long getNumberResources(String variableName, Solution solution, DataLayer dataLayer) {
		// Instantiate the pattern
		Triple t = Convert.toTriple(pattern, solution);

		// Set back the requested variable to null
		if (pattern.getSubjectVar().getName().equals(variableName))
			t = new Triple(null, t.getPredicate(), t.getObject());
		if (pattern.getPredicateVar().getName().equals(variableName))
			t = new Triple(t.getSubject(), null, t.getObject());
		if (pattern.getObjectVar().getName().equals(variableName))
			t = new Triple(t.getSubject(), t.getPredicate(), null);

		return dataLayer.getNumberOfResources(t);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "StatementPatternProvider [pattern=" + pattern + "]";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.model.ResourceProvider#getVariables()
	 */
	public Set<String> getVariables() {
		Set<String> result = new HashSet<String>();
		for (Var var : pattern.getVarList())
			if (!var.isAnonymous())
				result.add(var.getName());
		return result;
	}

	/**
	 * @return the pattern
	 */
	public StatementPattern getStatement() {
		return pattern;
	}

}
