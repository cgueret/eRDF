package nl.erdf.model.wod;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import nl.erdf.datalayer.DataLayer;
import nl.erdf.model.Binding;
import nl.erdf.model.Constraint;
import nl.erdf.model.Request;
import nl.erdf.model.Solution;
import nl.erdf.model.Variable;

/**
 * This class is a specific implementation of a request focused on using
 * semantic web data. Bindings are initalised with a fake blank URI
 * 
 * @author tolgam
 * 
 */
public class SPARQLRequest extends Request {

	/**
	 * @param datalayer
	 */
	public SPARQLRequest(DataLayer datalayer) {
		super(datalayer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see main.model.Request#getSolutionPrototype()
	 */
	@Override
	public Solution getSolutionPrototype() {
		Solution solution = new Solution();
		for (Variable variable : variables())
			solution.add(new Binding(variable, Node.NULL));
		return solution;
	}

	/**
	 * Convert a given solution into a triple set. If filter is true, only valid
	 * triples are returned
	 * 
	 * @param solution
	 *           the solution to use to instantiate the request
	 * @param filter
	 *           if true, only valid triples are returned
	 * @return a {TripleSet} containing valid triples
	 */
	public TripleSet getTripleSet(Solution solution, boolean filter) {
		TripleSet triples = new TripleSet();

		for (Constraint constraint : constraints()) {
			Node s = constraint.getPart(0);
			if (s instanceof Variable)
				s = solution.getBinding((Variable) s).getValue();

			Node p = constraint.getPart(1);
			if (p instanceof Variable)
				p = solution.getBinding((Variable) p).getValue();

			Node o = constraint.getPart(2);
			if (o instanceof Variable)
				o = solution.getBinding((Variable) o).getValue();

			Triple triple = Triple.create(s, p, o);
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
	 *           the solution to use to instantiate the request
	 * @return a {TripleSet} containing valid triples
	 */
	public TripleSet getTripleSet(Solution solution) {
		return getTripleSet(solution, true);
	}

	/**
	 * @return
	 */
	public int size() {
		return constraints.size();
	}
}
