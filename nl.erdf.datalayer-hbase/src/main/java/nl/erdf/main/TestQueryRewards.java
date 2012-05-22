/**
 * 
 */
package nl.erdf.main;

import java.io.FileNotFoundException;
import java.io.IOException;

import nl.erdf.constraints.impl.StatementPatternConstraint;
import nl.erdf.datalayer.DataLayer;
import nl.erdf.datalayer.hbase.NativeHBaseDataLayer;
import nl.erdf.model.Request;
import nl.erdf.model.Solution;
import nl.erdf.model.TripleSet;
import nl.erdf.model.Variable;
import nl.erdf.model.impl.StatementPatternProvider;
import nl.erdf.optimizer.Evaluate;

import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class TestQueryRewards {
	static final Logger logger = LoggerFactory.getLogger(TestQueryRewards.class);
	private final Request request;
	private final DataLayer datalayer;

	public TestQueryRewards() throws FileNotFoundException, IOException {
		// Create a data layer
		datalayer = NativeHBaseDataLayer.getInstance("default");

		// Create the request
		request = new Request(datalayer);

		// Query :
		// SELECT DISTINCT ?person ?field WHERE {
		// ?person <http://dbpedia.org/ontology/birthPlace>
		// <http://dbpedia.org/resource/Netherlands>.
		// ?person a <http://dbpedia.org/ontology/Artist>.
		// ?person <http://dbpedia.org/ontology/field> ?field.
		// }

		// We want artists
		StatementPattern artistStmt = new StatementPattern();
		artistStmt.setSubjectVar(new Var("person"));
		artistStmt.setPredicateVar(new Var("ar1", new URIImpl("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")));
		artistStmt.setObjectVar(new Var("ar2", new URIImpl("http://dbpedia.org/ontology/Artist")));

		// We want to know their field of work
		StatementPattern fieldStmt = new StatementPattern();
		fieldStmt.setSubjectVar(new Var("person"));
		fieldStmt.setPredicateVar(new Var("f1", new URIImpl("http://dbpedia.org/ontology/field")));
		fieldStmt.setObjectVar(new Var("field"));

		// We filter on birthPlace
		StatementPattern birthPlaceStmt = new StatementPattern();
		birthPlaceStmt.setSubjectVar(new Var("person"));
		birthPlaceStmt.setPredicateVar(new Var("bp1", new URIImpl("http://dbpedia.org/ontology/birthPlace")));
		birthPlaceStmt.setObjectVar(new Var("bp2", new URIImpl("http://dbpedia.org/resource/Netherlands")));

		// Declare the statements as triples to instantiate to get solutions
		request.addStatementPattern(birthPlaceStmt);
		request.addStatementPattern(artistStmt);
		request.addStatementPattern(fieldStmt);

		// Add the statements as constraints
		request.addConstraint(new StatementPatternConstraint(birthPlaceStmt));
		request.addConstraint(new StatementPatternConstraint(artistStmt));
		request.addConstraint(new StatementPatternConstraint(fieldStmt));

		// The two statements can be used as data sources
		request.addResourceProvider(new StatementPatternProvider(birthPlaceStmt));
		request.addResourceProvider(new StatementPatternProvider(artistStmt));
		request.addResourceProvider(new StatementPatternProvider(fieldStmt));

		Solution s = new Solution();
		s.add(new Variable("person"));
		s.add(new Variable("field"));
		Evaluate e = new Evaluate(request, datalayer, new TripleSet(), null);

		s.getVariable("person").setValue(new URIImpl("http://dbpedia.org/resource/Antonie_van_Leeuwenhoek"));
		e.evaluate(s);
		logger.info(s.toString());

		s.getVariable("person").setValue(new URIImpl("http://dbpedia.org/resource/Johannes_Vermeer"));
		e.evaluate(s);
		logger.info(s.toString());

		s.getVariable("person").setValue(new URIImpl("http://dbpedia.org/resource/M._C._Escher"));
		e.evaluate(s);
		logger.info(s.toString());

		s.getVariable("person").setValue(new URIImpl("http://dbpedia.org/resource/Vincent_van_Gogh"));
		e.evaluate(s);
		logger.info(s.toString());
	}

	/**
	 * @param args
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException {
		new TestQueryRewards();
	}
}
