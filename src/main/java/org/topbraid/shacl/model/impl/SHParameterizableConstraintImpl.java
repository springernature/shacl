package org.topbraid.shacl.model.impl;

import java.util.LinkedList;
import java.util.List;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.topbraid.shacl.constraints.ComponentConstraintExecutable;
import org.topbraid.shacl.constraints.ConstraintExecutable;
import org.topbraid.shacl.model.SHConstraintComponent;
import org.topbraid.shacl.model.SHFactory;
import org.topbraid.shacl.model.SHParameterizableConstraint;
import org.topbraid.shacl.vocabulary.DASH;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;

public abstract class SHParameterizableConstraintImpl extends SHParameterizableInstanceImpl implements SHParameterizableConstraint {
	
	public SHParameterizableConstraintImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	
	@Override
	public List<ConstraintExecutable> getExecutables() {
		List<ConstraintExecutable> results = new LinkedList<ConstraintExecutable>();
		Resource context = getContext();
		
		for(Resource comp : JenaUtil.getAllInstances(SH.ConstraintComponent.inModel(context.getModel()))) {
			SHConstraintComponent component = SHFactory.asConstraintComponent(comp);
			if(component.getParameters().size() == 1) {
				Property parameter = component.getParameters().get(0).getPredicate();
				for(Statement parameterValueS : this.listProperties(parameter).toList()) {
					results.add(new ComponentConstraintExecutable(this, component, context, parameterValueS.getObject()));
				}
			}
			else {
				ComponentConstraintExecutable executable = new ComponentConstraintExecutable(this, component, context);
				if(executable.isComplete()) {
					results.add(executable);
				}
			}
		}
		return results;
	}


	@Override
	public boolean isDeactivated() {
		return hasProperty(SH.filterShape, DASH.None);
	}
}