/**
 * 
 */
package nl.erdf.datalayer.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import nl.erdf.datalayer.DataLayer;
import nl.erdf.model.Triple;

import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.sail.memory.MemoryStore;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class MemoryDataLayer implements DataLayer {
	// Random number generator
	private final static Random rand = new Random();

	// The Sesame repository
	private final SailRepository repository;

	/**
	 * @param t
	 * @return
	 */
	private List<Statement> getTriples(Triple t) {
		SailRepositoryConnection c = null;
		try {
			c = repository.getConnection();
			RepositoryResult<Statement> res = c.getStatements(t.getSubject(), t.getPredicate(), t.getObject(), true);
			return res.asList();
		} catch (RepositoryException e) {
			e.printStackTrace();
		} finally {
			try {
				if (c != null)
					c.close();
			} catch (RepositoryException e) {
				e.printStackTrace();
			}
		}

		return new ArrayList<Statement>();
	}

	/**
	 * @throws RepositoryException
	 * 
	 */
	public MemoryDataLayer() throws RepositoryException {
		repository = new SailRepository(new MemoryStore());
		repository.initialize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * nl.erdf.datalayer.DataLayer#getNumberOfResources(nl.erdf.model.impl.Triple
	 * )
	 */
	public long getNumberOfResources(Triple t) {
		if (t.getNumberNulls() > 1)
			return 0;

		return getTriples(t).size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.datalayer.DataLayer#getResource(nl.erdf.model.impl.Triple)
	 */
	public Value getResource(Triple t) {
		if (t.getNumberNulls() > 1)
			return null;

		List<Statement> stmts = getTriples(t);

		if (stmts.size() > 0) {
			Statement s = stmts.get(rand.nextInt(stmts.size()));
			if (t.getSubject() == null)
				return s.getSubject();
			if (t.getPredicate() == null)
				return s.getPredicate();
			if (t.getObject() == null)
				return s.getObject();
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.datalayer.DataLayer#isValid(nl.erdf.model.impl.Triple)
	 */
	public boolean isValid(Triple t) {
		List<Statement> stmts = getTriples(t);
		return (stmts.size() != 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.datalayer.DataLayer#clear()
	 */
	public void clear() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.datalayer.DataLayer#shutdown()
	 */
	public void shutdown() {
		try {
			repository.shutDown();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.datalayer.DataLayer#waitForLatencyBuffer()
	 */
	public void waitForLatencyBuffer() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.datalayer.DataLayer#add(nl.erdf.model.impl.Triple)
	 */
	public void add(Statement statement) {

		SailRepositoryConnection c = null;
		try {
			c = repository.getConnection();
			c.add(statement);
			c.commit();
		} catch (RepositoryException e) {
			e.printStackTrace();
		} finally {
			try {
				if (c != null)
					c.close();
			} catch (RepositoryException e) {
				e.printStackTrace();
			}
		}
	}
}
