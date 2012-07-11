package nl.erdf.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import nl.erdf.constraints.Constraint;
import nl.erdf.constraints.Reward;
import nl.erdf.datalayer.DataLayer;
import nl.erdf.util.Convert;

import org.openrdf.query.algebra.StatementPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christophe Guéret <cgueret@few.vu.nl>
 * 
 */
public class Request {
	// Logger
	protected static final Logger logger = LoggerFactory.getLogger(Request.class);

	// Collection of statement patterns associated with that request
	private final Set<StatementPattern> patterns = new HashSet<StatementPattern>();

	// Constraints indexed by the variables they contain
	private final Map<String, List<Constraint>> constraints = new HashMap<String, List<Constraint>>();

	// Resource providers indexed by the variables they contain
	private final Map<String, List<ResourceProvider>> providers = new HashMap<String, List<ResourceProvider>>();

	// Data layer
	private final DataLayer dataLayer;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Request [statementPatterns=" + patterns + ", constraints=" + constraints + ", providers=" + providers
				+ ", dataLayer=" + dataLayer + "]";
	}

	/**
	 * @param datalayer
	 * 
	 */
	public Request(DataLayer datalayer) {
		this.dataLayer = datalayer;
	}

	/**
	 * A solution prototype is a default solution to the request. This function
	 * should be used to initialise the bindings to some default values
	 * 
	 * @return the solution prototype
	 */
	public Solution getSolutionPrototype() {
		Solution solution = new Solution();
		for (String variable : constraints.keySet())
			solution.add(new Variable(variable));
		return solution;
	}

	/**
	 * @return the constraints
	 */
	public Collection<Constraint> constraints() {
		Set<Constraint> res = new HashSet<Constraint>();
		for (Collection<Constraint> c : constraints.values())
			res.addAll(c);
		return res;
	}

	/**
	 * @return the variables
	 */
	public Set<String> constraintsVariables() {
		return constraints.keySet();
	}

	/**
	 * @param variable
	 * @return the maximum reward this variable can get
	 */
	public double getMaximumReward(String variable) {
		return constraints.get(variable).size() * Reward.HIGH;
	}

	/**
	 * @return the maximum reward this query can get
	 */
	public double getMaximumReward() {
		double total = 0;
		for (String variable : constraints.keySet())
			total += getMaximumReward(variable);
		return total;
	}

	/**
	 * Add a new constraint to the request. At the same time, link all the
	 * variable concerned by the constraint to that constraint.
	 * 
	 * @param constraint
	 */
	public void addConstraint(Constraint constraint) {
		for (String var : constraint.getVariables()) {
			List<Constraint> cstrs = null;
			if (!constraints.containsKey(var)) {
				cstrs = new ArrayList<Constraint>();
				constraints.put(var, cstrs);
			} else {
				cstrs = constraints.get(var);
			}
			cstrs.add(constraint);
		}
	}

	/**
	 * Add a new resource provider to the request. At the same time, link all
	 * the variable concerned by the provider to that provider.
	 * 
	 * @param provider
	 */
	public void addResourceProvider(ResourceProvider provider) {
		for (String var : provider.getVariables()) {
			List<ResourceProvider> prov = null;
			if (!providers.containsKey(var)) {
				prov = new ArrayList<ResourceProvider>();
				providers.put(var, prov);
			} else {
				prov = providers.get(var);
			}
			prov.add(provider);
		}
	}

	/**
	 * @param statementPattern
	 */
	public void addStatementPattern(StatementPattern statementPattern) {
		patterns.add(statementPattern);
	}

	/**
	 * Convert a given solution into a triple set. If filter is true, only valid
	 * triples are returned
	 * 
	 * @param solution
	 *            the solution to use to instantiate the request
	 * @param filter
	 *            if true, only valid triples are returned
	 * @return a {Set<Triple>} containing valid triples
	 */
	public Set<Triple> getTripleSet(Solution solution, boolean filter) {
		Set<Triple> triples = new HashSet<Triple>();

		for (StatementPattern pattern : patterns) {
			Triple triple = Convert.toTriple(pattern, solution);
			if (!filter || dataLayer.isValid(triple))
				triples.add(triple);
		}

		return triples;
	}

	/**
	 * Convert a given solution into a triple set. Equivalent to
	 * getTripleSet(solution, true)
	 * 
	 * @param solution
	 *            the solution to use to instantiate the request
	 * @return a {TripleSet} containing valid triples
	 */
	public Set<Triple> getTripleSet(Solution solution) {
		return getTripleSet(solution, true);
	}

	/**
	 * @param variableName
	 * @return a list of providers
	 */
	public List<ResourceProvider> getResourceProvidersFor(String variableName) {
		if (!providers.containsKey(variableName))
			return new ArrayList<ResourceProvider>();
		return providers.get(variableName);
	}

	/**
	 * @param variableName
	 * @return a lit of constraints
	 */
	public List<Constraint> getConstraintsFor(String variableName) {
		if (!constraints.containsKey(variableName))
			return new ArrayList<Constraint>();
		return constraints.get(variableName);
	}

	/**
	 * @return
	 */
	public int getNbConstraints() {
		Set<Constraint> s = new HashSet<Constraint>();
		for (Entry<String, List<Constraint>> c : constraints.entrySet())
			s.addAll(c.getValue());
		return s.size();
	}

	/**
	 * @return
	 */
	public int getNbVariables() {
		return constraints.keySet().size();
	}

	/**
	 * 
	 */
	public void printProviders() {
		for (Entry<String, List<ResourceProvider>> p : providers.entrySet())
			for (ResourceProvider pp : p.getValue())
				logger.info(p.getKey() + " <- " + pp);
	}
}
