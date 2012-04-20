/**
 * 
 */
package nl.erdf.model;

import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class SolutionTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Variable v1 = new Variable("v1");
		Variable v2 = new Variable("v2");
		Solution a = new Solution();
		a.add(v1);
		a.add(v2);
		Solution b = new Solution();
		b.add(v1);
		b.add(v2);

		ValueFactory f = new ValueFactoryImpl();
		a.getVariable("v1").setValue(f.createURI("http://dbpedia.org/resource/Desiderius_Erasmus"));
		a.getVariable("v2").setValue(null);
		b.getVariable("v1").setValue(f.createURI("http://dbpedia.org/resource/Desiderius_Erasmus"));
		b.getVariable("v2").setValue(null);
		System.out.println(b.equals(a));
		System.out.println(a.equals(b));

		Solution c = b.clone();
		System.out.println(b.equals(c));
		b.getVariable("v1").setValue(f.createURI("http://dbpedia.org/resource/Desiderius_Erasmusdsd"));
		System.out.println(b.equals(c));
	}

}
