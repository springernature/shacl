package org.topbraid.spin.spr;

import java.util.LinkedList;
import java.util.List;

import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.util.JenaDatatypes;
import org.topbraid.spin.vocabulary.SPR;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;


/**
 * Static utilities on SPR tables.
 * 
 * @author Holger Knublauch
 */
public class SPRResultSets {
	
	private static Query cellQuery = ARQFactory.get().doCreateQuery(
			"SELECT (<" + SPR.cell.getURI() + ">(?table, ?row, ?col) AS ?result)\n" +
			"WHERE {\n" +
			"}");
	
	private static Query colCountQuery = ARQFactory.get().doCreateQuery(
			"SELECT (<" + SPR.colCount.getURI() + ">(?table) AS ?result)\n" +
			"WHERE {\n" +
			"}");
	
	private static Query colNameQuery = ARQFactory.get().doCreateQuery(
			"SELECT (<" + SPR.colName.getURI() + ">(?table, ?col) AS ?result)\n" +
			"WHERE {\n" +
			"}");
	
	private static Query rowCountQuery = ARQFactory.get().doCreateQuery(
			"SELECT (<" + SPR.rowCount.getURI() + ">(?table) AS ?result)\n" +
			"WHERE {\n" +
			"}");
	
	
	public static RDFNode getCell(Resource table, int row, int col) {
		Model model = table.getModel();
		QuerySolutionMap bindings = new QuerySolutionMap();
		bindings.add("table", table);
		bindings.add("row", JenaDatatypes.createInteger(row));
		bindings.add("col", JenaDatatypes.createInteger(col));
		try(QueryExecution qexec = ARQFactory.get().createQueryExecution(cellQuery, model, bindings)) {
		    ResultSet rs = qexec.execSelect();
		    if(rs.hasNext()) {
		        RDFNode result = rs.next().get("result");
		        return result;
		    }
		    else {
		        return null;
		    }
		}
	}

	
	public static int getColCount(Resource table) {
		return getIntFromFunction(table, colCountQuery);
	}
	
	
	public static String getColName(Resource table, int col) {
	    Model model = table.getModel();
	    QuerySolutionMap bindings = new QuerySolutionMap();
	    bindings.add("table", table);
	    bindings.add("col", JenaDatatypes.createInteger(col));
	    try(QueryExecution qexec = ARQFactory.get().createQueryExecution(colNameQuery, model, bindings)) {
	        ResultSet rs = qexec.execSelect();
	        if(rs.hasNext()) {
	            RDFNode result = rs.next().get("result");
	            if(result.isLiteral()) {
	                return ((Literal)result).getString();
	            }
	        } 
	        return null;
	    }
	}
	

	public static List<String> getColNames(Resource table) {
		List<String> results = new LinkedList<String>();
		int colCount = getColCount(table);
		for(int i = 0; i < colCount; i++) {
			results.add(getColName(table, i));
		}
		return results;
	}
	
	
	private static int getIntFromFunction(Resource table, Query query) {
		Model model = table.getModel();
		try(QueryExecution qexec = ARQFactory.get().createQueryExecution(query, model)) {
		    QuerySolutionMap bindings = new QuerySolutionMap();
		    bindings.add("table", table);
		    qexec.setInitialBinding(bindings);
		        ResultSet rs = qexec.execSelect();
		        if(rs.hasNext()) {
		            RDFNode result = rs.next().get("result");
		            if(result.isLiteral()) {
		                return ((Literal)result).getInt();
		            }
		        } 
		        return 0;
		}
		catch(Exception ex) {
			throw new IllegalArgumentException("Error trying to query spr: result set " + table, ex);
		}
	}

	
	public static int getRowCount(Resource table) {
		return getIntFromFunction(table, rowCountQuery);
	}
}
