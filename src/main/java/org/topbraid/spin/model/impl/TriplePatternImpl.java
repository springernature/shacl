/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.impl;

import org.topbraid.spin.model.TriplePattern;
import org.topbraid.spin.model.visitor.ElementVisitor;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;


public class TriplePatternImpl extends TripleImpl implements TriplePattern {

	public TriplePatternImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}


	@Override
    public void visit(ElementVisitor visitor) {
		visitor.visit(this);
	}
}
