package nl.erdf.datalayer.sparql;

import java.io.File;

import nl.erdf.constraints.TripleConstraint;
import nl.erdf.model.Constraint;
import nl.erdf.model.Request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementAssign;
import com.hp.hpl.jena.sparql.syntax.ElementBind;
import com.hp.hpl.jena.sparql.syntax.ElementDataset;
import com.hp.hpl.jena.sparql.syntax.ElementExists;
import com.hp.hpl.jena.sparql.syntax.ElementFetch;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementMinus;
import com.hp.hpl.jena.sparql.syntax.ElementNamedGraph;
import com.hp.hpl.jena.sparql.syntax.ElementNotExists;
import com.hp.hpl.jena.sparql.syntax.ElementOptional;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementService;
import com.hp.hpl.jena.sparql.syntax.ElementSubQuery;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.ElementUnion;
import com.hp.hpl.jena.sparql.syntax.ElementVisitor;

/**
 * @author tolgam
 * 
 */
public class SPARQLParser implements ElementVisitor {
	// Logger instance
	private final static Logger logger = LoggerFactory.getLogger(SPARQLParser.class);

	// The request to work on
	private final Request request;

	/**
	 * @param request
	 */
	public SPARQLParser(Request request) {
		this.request = request;
	}

	/**
	 * @param fileName
	 */
	public void parseFromFile(File fileName) {
		logger.info("Parsing query " + fileName.getAbsolutePath());

		// Register the variables to output
		Query query = QueryFactory.read(fileName.getAbsolutePath());

		// Parse the body of the query
		Element pattern = query.getQueryPattern();
		pattern.visit(this);
	}

	/**
	 * @param queryString
	 */
	public void parseFromString(String queryString) {
		// Register the variables to output
		Query query = QueryFactory.create(queryString);

		// Parse the body of the query
		Element pattern = query.getQueryPattern();
		pattern.visit(this);
	}

	/**
	 * @param triple
	 */
	private void handleTriple(Triple triple) {
		// Reject triple with a literal as subject
		if (triple.getSubject() instanceof Literal) {
			logger.warn("Malformed triple");
			return;
		}

		// Count the number of variables and deal with the result
		if (triple.getSubject().isVariable() && triple.getPredicate().isVariable() && triple.getObject().isVariable()) {
			logger.warn("Don't know what to search in a fully defined <S,P,O>");
			return;
		}

		// Reject long literals (abstract, description, ...)
		if (triple.getObject() instanceof Literal) {
			String lex = ((Literal) triple.getObject()).getLexicalForm();
			if (lex.length() > 300) {
				logger.warn("Long literal ignored (" + lex.length() + " chars)");
				return;
			}
		}

		// Reject weird constraints
		if (triple.getSubject().equals(triple.getObject())) {
			logger.warn("I don't use any <?S,P,?S>");
			return;
		}

		Constraint constraint = new TripleConstraint(triple);
		request.addConstraint(constraint);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.hp.hpl.jena.sparql.syntax.ElementVisitor#visit(com.hp.hpl.jena.sparql
	 * .syntax.ElementTriplesBlock)
	 */
	public void visit(ElementTriplesBlock el) {
		// Parse all the triples of that bloc
		for (Triple triple : el.getPattern()) {
			handleTriple(triple);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.hp.hpl.jena.sparql.syntax.ElementVisitor#visit(com.hp.hpl.jena.sparql
	 * .syntax.ElementGroup)
	 */
	public void visit(ElementGroup el) {
		// Visit all the sub elements
		for (Element subElement : el.getElements()) {
			subElement.visit(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.hp.hpl.jena.sparql.syntax.ElementVisitor#visit(com.hp.hpl.jena.sparql
	 * .syntax.ElementOptional)
	 */
	public void visit(ElementOptional optional) {
		// Visit the content of the optional element
		optional.getOptionalElement().visit(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.hp.hpl.jena.sparql.syntax.ElementVisitor#visit(com.hp.hpl.jena.sparql
	 * .syntax.ElementPathBlock)
	 */
	public void visit(ElementPathBlock pathBlock) {
		// Call the handler for all the triples in the path block
		for (TriplePath triplePath : pathBlock.getPattern()) {
			handleTriple(triplePath.asTriple());
		}
	}

	public void visit(ElementFilter arg0) {
		// Filters are ignored. That's something eRDF can't deal with directly
		// during the optimisation process
	}

	public void visit(ElementAssign arg0) {
		logger.warn("Element not handled :" + arg0.getClass());
	}

	public void visit(ElementDataset arg0) {
		logger.warn("Element not handled :" + arg0.getClass());
	}

	public void visit(ElementFetch arg0) {
		logger.warn("Element not handled :" + arg0.getClass());
	}

	public void visit(ElementNamedGraph arg0) {
		logger.warn("Element not handled :" + arg0.getClass());
	}

	public void visit(ElementService arg0) {
		logger.warn("Element not handled :" + arg0.getClass());
	}

	public void visit(ElementSubQuery arg0) {
		logger.warn("Element not handled :" + arg0.getClass());
	}

	public void visit(ElementUnion arg0) {
		logger.warn("Element not handled :" + arg0.getClass());
	}

	public void visit(ElementExists arg0) {
		logger.warn("Element not handled :" + arg0.getClass());
	}

	public void visit(ElementNotExists arg0) {
		logger.warn("Element not handled :" + arg0.getClass());
	}

	public void visit(ElementBind arg0) {
		logger.warn("Element not handled :" + arg0.getClass());
	}

	public void visit(ElementMinus arg0) {
		logger.warn("Element not handled :" + arg0.getClass());
	}

}
