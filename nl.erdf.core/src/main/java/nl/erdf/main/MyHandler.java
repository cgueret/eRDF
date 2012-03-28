/**
 * 
 */
package nl.erdf.main;

import nl.erdf.datalayer.DataLayer;
import nl.erdf.model.impl.Triple;

import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class MyHandler implements RDFHandler {
	final DataLayer dataLayer;

	/**
	 * @param dataLayer
	 */
	public MyHandler(DataLayer dataLayer) {
		this.dataLayer = dataLayer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openrdf.rio.RDFHandler#endRDF()
	 */
	public void endRDF() throws RDFHandlerException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openrdf.rio.RDFHandler#handleComment(java.lang.String)
	 */
	public void handleComment(String arg0) throws RDFHandlerException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openrdf.rio.RDFHandler#handleNamespace(java.lang.String,
	 * java.lang.String)
	 */
	public void handleNamespace(String arg0, String arg1) throws RDFHandlerException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.rio.RDFHandler#handleStatement(org.openrdf.model.Statement)
	 */
	public void handleStatement(Statement statement) throws RDFHandlerException {
		dataLayer.add(new Triple(statement.getSubject(), statement.getPredicate(), statement.getObject()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openrdf.rio.RDFHandler#startRDF()
	 */
	public void startRDF() throws RDFHandlerException {
	}

}
