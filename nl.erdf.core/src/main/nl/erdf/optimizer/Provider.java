package nl.erdf.optimizer;

import nl.erdf.datalayer.DataLayer;
import nl.erdf.datalayer.QueryPattern;
import nl.erdf.model.Solution;
import nl.erdf.model.Variable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;

/**
 * @author tolgam
 * 
 */
public class Provider {
	/** Logger */
	protected final static Logger logger = LoggerFactory.getLogger(Provider.class);

	/** The data layer to get data from */
	protected final DataLayer datalayer;

	/** Subject,predicate and object */
	private final Node s;
	private final Node p;
	private final Node o;

	/**
	 * @param datalayer
	 * @param s
	 * @param p
	 * @param o
	 */
	public Provider(DataLayer datalayer, Node s, Node p, Node o) {
		this.datalayer = datalayer;
		this.s = s;
		this.p = p;
		this.o = o;
	}

	/**
	 * @param variable
	 * @return
	 */
	public boolean appliesFor(Variable variable) {
		if (s instanceof Variable)
			if (((Variable) s).equals(variable))
				return true;
		if (p instanceof Variable)
			if (((Variable) p).equals(variable))
				return true;
		if (o instanceof Variable)
			if (((Variable) o).equals(variable))
				return true;
		return false;
	}

	/**
	 * @param variable
	 * @param solution
	 * @return
	 */
	public QueryPattern getQuery(Variable variable, Solution solution) {
		// Instantiate the pattern into a full triple
		Node subject = s;
		if (s instanceof Variable)
			subject = solution.getBinding((Variable) s).getValue();

		Node predicate = p;
		if (p instanceof Variable)
			predicate = solution.getBinding((Variable) p).getValue();

		Node object = o;
		if (o instanceof Variable)
			object = solution.getBinding((Variable) o).getValue();

		// Replace the target variable by a return and create a query
		QueryPattern query = null;
		if ((s instanceof Variable) && (variable.equals(s)))
			query = new QueryPattern(QueryPattern.RETURN, predicate, object);
		if ((p instanceof Variable) && (variable.equals(p)))
			query = new QueryPattern(subject, QueryPattern.RETURN, object);
		if ((o instanceof Variable) && (variable.equals(o)))
			query = new QueryPattern(subject, predicate, QueryPattern.RETURN);

		return query;
	}

	/**
	 * @param variable
	 * @param solution
	 * @return
	 */
	public double getExpectedReward(Variable variable, Solution solution) {
		double reward = 0;

		if (s instanceof Variable)
			reward += solution.getBinding((Variable) s).getRelativeReward();
		else
			reward += 1;

		if (o instanceof Variable)
			reward += solution.getBinding((Variable) o).getRelativeReward();
		else
			reward += 1;

		return reward / 2;
	}
}
