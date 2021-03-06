package org.topbraid.shacl.model.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.topbraid.shacl.model.SHParameter;
import org.topbraid.shacl.model.SHParameterizable;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaDatatypes;
import org.topbraid.spin.util.JenaUtil;

public class SHParameterizableImpl extends SHResourceImpl implements SHParameterizable {
	
	public SHParameterizableImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	
	@Override
	public List<SHParameter> getParameters() {
		List<SHParameter> results = new LinkedList<SHParameter>();
		StmtIterator it = null;
		JenaUtil.setGraphReadOptimization(true);
		try {
			Set<Resource> classes = JenaUtil.getAllSuperClasses(this);
			classes.add(this);
			for(Resource cls : classes) {
				it = cls.listProperties(SH.parameter);
				while(it.hasNext()) {
					Resource param = it.next().getResource();
					results.add(param.as(SHParameter.class));
				}
			}
		}
		finally {
			if (it != null) {
				it.close();
			}
			JenaUtil.setGraphReadOptimization(false);
		}
		return results;
	}

	
	@Override
	public Map<String, SHParameter> getParametersMap() {
		Map<String,SHParameter> results = new HashMap<String,SHParameter>();
		for(SHParameter parameter : getParameters()) {
			Property property = parameter.getPredicate();
			if(property != null) {
				results.put(property.getLocalName(), parameter);
			}
		}
		return results;
	}

	
	@Override
	public String getLabelTemplate() {
		return JenaUtil.getStringProperty(this, SH.labelTemplate);
	}
	
	
	@Override
	public List<SHParameter> getOrderedParameters() {
		List<SHParameter> results = getParameters();
		Collections.sort(results, new Comparator<SHParameter>() {
			@Override
            public int compare(SHParameter param1, SHParameter param2) {
				Property p1 = param1.getPredicate();
				Property p2 = param2.getPredicate();
				if(p1 != null && p2 != null) {
					Integer index1 = param1.getOrder();
					Integer index2 = param2.getOrder();
					if(index1 != null) {
						if(index2 != null) {
							int comp = index1.compareTo(index2);
							if(comp != 0) {
								return comp;
							}
						}
						else {
							return -1;
						}
					}
					else if(index2 != null) {
						return 1;
					}
					return p1.getLocalName().compareTo(p2.getLocalName());
				}
				else {
					return 0;
				}
			}
		});
		return results;
	}


	@Override
	public boolean isOptionalParameter(Property predicate) {
		for(SHParameter param : getParameters()) {
			if(param.hasProperty(SH.predicate, predicate) && param.hasProperty(SH.optional, JenaDatatypes.TRUE)) {
				return true;
			}
		}
		return false;
	}
}
