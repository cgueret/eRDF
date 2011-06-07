/**
 * 
 */
package nl.erdf.datalayer.sparql.ng;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.tdb.TDBFactory;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class JenaTDBStore implements TripleStore {
	/** The TDB model */
	private final Model model = TDBFactory.createModel("jena-tdb-cache");

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * nl.erdf.datalayer.sparql.TripleStore#getResources(com.hp.hpl.jena.graph
	 * .Triple)
	 */
	@Override
	public Set<Triple> getTriples(Triple triple) {
		Set<Triple> triples = new HashSet<Triple>();

		Resource s = null;
		if (!triple.getSubject().equals(Node.ANY) && !triple.getSubject().isVariable())
			s = ResourceFactory.createResource(triple.getSubject().getURI());
		Property p = null;
		RDFNode o = null;

		StmtIterator statements = model.listStatements(s, p, o);
		for (Statement stmt = statements.next(); statements.hasNext(); stmt = statements.next())
			triples.add(stmt.asTriple());

		return triples;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * nl.erdf.datalayer.sparql.TripleStore#addTriple(com.hp.hpl.jena.graph.
	 * Triple)
	 */
	@Override
	public void addTriple(Triple triple) {
		model.add(model.asStatement(triple));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.datalayer.sparql.ng.TripleStore#shutdown()
	 */
	@Override
	public void shutdown() {
		model.commit();
		model.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.datalayer.sparql.ng.TripleStore#clear()
	 */
	@Override
	public void clear() {
		model.removeAll();
		model.commit();
	}

}
