package org.topbraid.shacl.constraints;

import java.net.URI;
import java.util.Collections;
import java.util.function.Function;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.shacl.model.SHParameterizableTarget;
import org.topbraid.shacl.vocabulary.DASH;
import org.topbraid.shacl.vocabulary.SH;

/**
 * An ExecutionLanguage that serves as fall-back, if a constraint or parameterizable does not define
 * any valid executable property such as sh:sparql.
 * This will simply report a failure that the constraint could not be evaluated.
 * 
 * @author Holger Knublauch
 */
public class FallbackExecutionLanguage implements ExecutionLanguage {

	
	@Override
	public boolean canExecuteConstraint(ConstraintExecutable executable) {
		return true;
	}

	
	@Override
	public boolean canExecuteTarget(Resource executable) {
		return true;
	}


	@Override
	public void executeConstraint(Dataset dataset, Resource shape,
			URI shapesGraphURI, ConstraintExecutable executable,
			RDFNode focusNode, Model results, Function<RDFNode,String> labelFunction) {
		Resource result = results.createResource(DASH.FailureResult);
		result.addProperty(SH.message, "No suitable validator found for constraint");
		result.addProperty(SH.sourceConstraint, executable.getConstraint());
		result.addProperty(SH.sourceShape, shape);
		if(executable instanceof ComponentConstraintExecutable) {
			result.addProperty(SH.sourceConstraintComponent, ((ComponentConstraintExecutable)executable).getComponent());
		}
		if(focusNode != null) {
			result.addProperty(SH.focusNode, focusNode);
		}
	}


	@Override
	public Iterable<RDFNode> executeTarget(Dataset dataset,
			Resource executable, SHParameterizableTarget parameterizableTarget) {
		return Collections.emptyList();
	}


	@Override
	public boolean isNodeInTarget(RDFNode focusNode, Dataset dataset,
			Resource executable, SHParameterizableTarget parameterizableTarget) {
		return false;
	}
}
