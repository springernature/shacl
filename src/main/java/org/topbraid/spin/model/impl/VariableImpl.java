/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.impl;

import java.util.HashSet;
import java.util.Set;

import org.topbraid.spin.model.TriplePattern;
import org.topbraid.spin.model.Variable;
import org.topbraid.spin.model.print.PrintContext;
import org.topbraid.spin.vocabulary.SP;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;


public class VariableImpl extends AbstractSPINResourceImpl implements Variable {
    
	public VariableImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}
	
	
	private void addTriplePatterns(Property predicate, Set<TriplePattern> results) {
		StmtIterator it = getModel().listStatements(null, predicate, this);
		while(it.hasNext()) {
			Resource subject = it.nextStatement().getSubject();
			results.add(subject.as(TriplePattern.class));
		}
	}

	
	@Override
    public String getName() {
		return getString(SP.varName);
	}


	@Override
    public Set<TriplePattern> getTriplePatterns() {
		Set<TriplePattern> results = new HashSet<TriplePattern>();
		addTriplePatterns(SP.subject, results);
		addTriplePatterns(SP.predicate, results);
		addTriplePatterns(SP.object, results);
		return results;
	}
	
	
	@Override
    public boolean isBlankNodeVar() {
		String name = getName();
		if(name != null) {
			return name.startsWith("?");
		}
		else {
			return false;
		}
	}


	@Override
    public void print(PrintContext p) {
		String name = getName();
		if(name.startsWith("?")) {
			p.print("_:");
			p.print(name.substring(1));
		}
		else {
			p.printVariable(name);
		}
	}
}
