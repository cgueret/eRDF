package nl.erdf.datalayer.sparql;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.graph.Triple;

import nl.erdf.datalayer.DataLayer;
import nl.erdf.model.Binding;
import nl.erdf.model.Constraint;
import nl.erdf.model.Request;
import nl.erdf.model.Solution;

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
		for (Node_Variable variable : variables())
			solution.add(new Binding(variable, Node.NULL));
		return solution;
	}


	/**
	 * @return the number of constraints
	 */
	public int size() {
		return constraints.size();
	}
}
