/**
 * 
 */
package nl.erdf.util;

import java.util.HashSet;
import java.util.Set;

import org.openrdf.query.algebra.Add;
import org.openrdf.query.algebra.And;
import org.openrdf.query.algebra.ArbitraryLengthPath;
import org.openrdf.query.algebra.Avg;
import org.openrdf.query.algebra.BNodeGenerator;
import org.openrdf.query.algebra.BindingSetAssignment;
import org.openrdf.query.algebra.Bound;
import org.openrdf.query.algebra.Clear;
import org.openrdf.query.algebra.Coalesce;
import org.openrdf.query.algebra.Compare;
import org.openrdf.query.algebra.CompareAll;
import org.openrdf.query.algebra.CompareAny;
import org.openrdf.query.algebra.Copy;
import org.openrdf.query.algebra.Count;
import org.openrdf.query.algebra.Create;
import org.openrdf.query.algebra.Datatype;
import org.openrdf.query.algebra.DeleteData;
import org.openrdf.query.algebra.Difference;
import org.openrdf.query.algebra.Distinct;
import org.openrdf.query.algebra.EmptySet;
import org.openrdf.query.algebra.Exists;
import org.openrdf.query.algebra.Extension;
import org.openrdf.query.algebra.ExtensionElem;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.FunctionCall;
import org.openrdf.query.algebra.Group;
import org.openrdf.query.algebra.GroupConcat;
import org.openrdf.query.algebra.GroupElem;
import org.openrdf.query.algebra.IRIFunction;
import org.openrdf.query.algebra.If;
import org.openrdf.query.algebra.In;
import org.openrdf.query.algebra.InsertData;
import org.openrdf.query.algebra.Intersection;
import org.openrdf.query.algebra.IsBNode;
import org.openrdf.query.algebra.IsLiteral;
import org.openrdf.query.algebra.IsNumeric;
import org.openrdf.query.algebra.IsResource;
import org.openrdf.query.algebra.IsURI;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.Label;
import org.openrdf.query.algebra.Lang;
import org.openrdf.query.algebra.LangMatches;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.Like;
import org.openrdf.query.algebra.Load;
import org.openrdf.query.algebra.LocalName;
import org.openrdf.query.algebra.MathExpr;
import org.openrdf.query.algebra.Max;
import org.openrdf.query.algebra.Min;
import org.openrdf.query.algebra.Modify;
import org.openrdf.query.algebra.Move;
import org.openrdf.query.algebra.MultiProjection;
import org.openrdf.query.algebra.Namespace;
import org.openrdf.query.algebra.Not;
import org.openrdf.query.algebra.Or;
import org.openrdf.query.algebra.Order;
import org.openrdf.query.algebra.OrderElem;
import org.openrdf.query.algebra.Projection;
import org.openrdf.query.algebra.ProjectionElem;
import org.openrdf.query.algebra.ProjectionElemList;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.QueryModelVisitor;
import org.openrdf.query.algebra.QueryRoot;
import org.openrdf.query.algebra.Reduced;
import org.openrdf.query.algebra.Regex;
import org.openrdf.query.algebra.SameTerm;
import org.openrdf.query.algebra.Sample;
import org.openrdf.query.algebra.Service;
import org.openrdf.query.algebra.SingletonSet;
import org.openrdf.query.algebra.Slice;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.Str;
import org.openrdf.query.algebra.Sum;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Union;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.ZeroLengthPath;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.sparql.SPARQLParser;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class PatternsExtractor implements QueryModelVisitor<Exception> {
	private final Set<StatementPattern> patterns = new HashSet<StatementPattern>();

	/**
	 * 
	 */
	protected PatternsExtractor() {
	}

	/**
	 * @param queryString
	 * @return the set of patterns
	 */
	public static Set<StatementPattern> fromSPARQL(String queryString) {
		try {
			SPARQLParser parser = new SPARQLParser();
			ParsedQuery query = parser.parseQuery(queryString, null);
			PatternsExtractor extractor = new PatternsExtractor();
			TupleExpr t = query.getTupleExpr();
			t.visitChildren(extractor);
			return extractor.getPatterns();
		} catch (Exception e) {
			return new HashSet<StatementPattern>();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .QueryRoot)
	 */
	public void meet(QueryRoot node) throws Exception {
		node.visitChildren(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .Add)
	 */
	public void meet(Add add) throws Exception {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .And)
	 */
	public void meet(And node) throws Exception {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .ArbitraryLengthPath)
	 */
	public void meet(ArbitraryLengthPath node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .Avg)
	 */

	public void meet(Avg node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .BindingSetAssignment)
	 */

	public void meet(BindingSetAssignment node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .BNodeGenerator)
	 */

	public void meet(BNodeGenerator node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .Bound)
	 */

	public void meet(Bound node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .Clear)
	 */

	public void meet(Clear clear) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .Coalesce)
	 */

	public void meet(Coalesce node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .Compare)
	 */

	public void meet(Compare node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .CompareAll)
	 */

	public void meet(CompareAll node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .CompareAny)
	 */

	public void meet(CompareAny node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .Copy)
	 */

	public void meet(Copy copy) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .Count)
	 */

	public void meet(Count node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .Create)
	 */

	public void meet(Create create) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .Datatype)
	 */

	public void meet(Datatype node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .DeleteData)
	 */

	public void meet(DeleteData deleteData) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .Difference)
	 */

	public void meet(Difference node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .Distinct)
	 */

	public void meet(Distinct node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .EmptySet)
	 */

	public void meet(EmptySet node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .Exists)
	 */

	public void meet(Exists node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .Extension)
	 */

	public void meet(Extension node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .ExtensionElem)
	 */

	public void meet(ExtensionElem node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .Filter)
	 */

	public void meet(Filter node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .FunctionCall)
	 */

	public void meet(FunctionCall node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .Group)
	 */

	public void meet(Group node) throws Exception {
		node.visitChildren(this);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .GroupConcat)
	 */

	public void meet(GroupConcat node) throws Exception {
		node.visit(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .GroupElem)
	 */

	public void meet(GroupElem node) throws Exception {
		node.visit(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .If)
	 */

	public void meet(If node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .In)
	 */

	public void meet(In node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .InsertData)
	 */

	public void meet(InsertData insertData) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .Intersection)
	 */

	public void meet(Intersection node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .IRIFunction)
	 */

	public void meet(IRIFunction node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .IsBNode)
	 */

	public void meet(IsBNode node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .IsLiteral)
	 */

	public void meet(IsLiteral node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .IsNumeric)
	 */

	public void meet(IsNumeric node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .IsResource)
	 */

	public void meet(IsResource node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .IsURI)
	 */

	public void meet(IsURI node) throws Exception {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .Join)
	 */

	public void meet(Join node) throws Exception {
		node.visitChildren(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .Label)
	 */

	public void meet(Label node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .Lang)
	 */

	public void meet(Lang node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .LangMatches)
	 */

	public void meet(LangMatches node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .LeftJoin)
	 */

	public void meet(LeftJoin node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .Like)
	 */

	public void meet(Like node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .Load)
	 */

	public void meet(Load load) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .LocalName)
	 */

	public void meet(LocalName node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .MathExpr)
	 */

	public void meet(MathExpr node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .Max)
	 */

	public void meet(Max node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .Min)
	 */

	public void meet(Min node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .Modify)
	 */
	public void meet(Modify modify) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .Move)
	 */

	public void meet(Move move) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .MultiProjection)
	 */

	public void meet(MultiProjection node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .Namespace)
	 */

	public void meet(Namespace node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .Not)
	 */

	public void meet(Not node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .Or)
	 */

	public void meet(Or node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .Order)
	 */

	public void meet(Order node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .OrderElem)
	 */

	public void meet(OrderElem node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .Projection)
	 */

	public void meet(Projection node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .ProjectionElem)
	 */

	public void meet(ProjectionElem node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .ProjectionElemList)
	 */

	public void meet(ProjectionElemList node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .Reduced)
	 */

	public void meet(Reduced node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .Regex)
	 */

	public void meet(Regex node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .SameTerm)
	 */

	public void meet(SameTerm node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .Sample)
	 */

	public void meet(Sample node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .Service)
	 */

	public void meet(Service node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .SingletonSet)
	 */

	public void meet(SingletonSet node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .Slice)
	 */

	public void meet(Slice node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .StatementPattern)
	 */

	public void meet(StatementPattern node) throws Exception {
		patterns.add(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .Str)
	 */

	public void meet(Str node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .Sum)
	 */

	public void meet(Sum node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .Union)
	 */

	public void meet(Union node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .ValueConstant)
	 */

	public void meet(ValueConstant node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .Var)
	 */

	public void meet(Var node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meet(org.openrdf.query.algebra
	 * .ZeroLengthPath)
	 */

	public void meet(ZeroLengthPath node) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.query.algebra.QueryModelVisitor#meetOther(org.openrdf.query
	 * .algebra.QueryModelNode)
	 */

	public void meetOther(QueryModelNode node) throws Exception {

	}

	/**
	 * @return the patterns
	 */
	public Set<StatementPattern> getPatterns() {
		return patterns;
	}
}
