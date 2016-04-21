package org.topbraid.shacl.constraints;

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDFS;
import org.topbraid.shacl.model.SHACLConstraint;
import org.topbraid.shacl.model.SHACLFactory;
import org.topbraid.shacl.util.SHACLUtil;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.progress.ProgressMonitor;
import org.topbraid.spin.system.SPINLabels;
import org.topbraid.spin.util.JenaUtil;

/**
 * An engine that can validate all constraints defined for a given Model,
 * where the Model is expected to be the default graph of a given Dataset.
 * The engine will look at SHACL declarations inside of the Model to figure
 * out which shapes to validate.
 * 
 * @author Holger Knublauch
 */
public class ModelConstraintValidator extends AbstractConstraintValidator {
	
	public static final String FILTER_VAR_NAME = "FILTER_SHAPE";

	private static ModelConstraintValidator singleton = new ModelConstraintValidator();
	
	public static ModelConstraintValidator get() {
		return singleton;
	}
	
	public static void set(ModelConstraintValidator value) {
		singleton = value;
	}

	
	/**
	 * Validates all resources in a given Model, which is expected to be the default
	 * graph of a given Dataset.
	 * @param dataset  the Dataset to validate
	 * @param shapesGraphURI  the URI of the shapes graph in the dataset
	 * @param minSeverity  the minimum severity, e.g. sh:Error or null for all
	 * @param validateShapes  true to also check schema-level resources (sh:parameter, sh:context etc)
	 * @param monitor  an optional ProgressMonitor
	 * @return a Model containing violation results - empty if OK
	 */
	public Model validateModel(Dataset dataset, URI shapesGraphURI, Resource minSeverity, boolean validateShapes, ProgressMonitor monitor) throws InterruptedException {
		
		if(dataset.getDefaultModel() == null) {
			throw new IllegalArgumentException("Dataset requires a default model");
		}
		
		Model shapesModel = dataset.getNamedModel(shapesGraphURI.toString());
		
		if(monitor != null) {
			monitor.subTask("Preparing execution plan");
		}
		
		List<Property> constraintProperties = SHACLUtil.getAllConstraintProperties(validateShapes);
		Map<Resource,List<SHACLConstraint>> map = buildShape2ConstraintsMap(shapesModel, dataset.getDefaultModel(), constraintProperties, validateShapes);
		if(monitor != null) {
			monitor.subTask("");
		}
		
		if(monitor != null) {
			monitor.beginTask("Validating constraints for " + map.size() + " shapes...", map.size());
		}
		
		Model results = JenaUtil.createMemoryModel();
		for(Resource shape : map.keySet()) {
			for(SHACLConstraint constraint : map.get(shape)) {
				validateConstraintForShape(dataset, shapesGraphURI, minSeverity, constraint, shape, results, monitor);
				if(monitor != null) {
					monitor.worked(1);
					if(monitor.isCanceled()) {
						throw new InterruptedException();
					}
				}
			}
		}
		
		return results;
	}
	
	
	private Map<Resource,List<SHACLConstraint>> buildShape2ConstraintsMap(Model shapesModel, Model dataModel, List<Property> constraintProperties, boolean validateShapes) {
		Map<Resource,List<SHACLConstraint>> map = new HashMap<Resource,List<SHACLConstraint>>();
		for(Property constraintProperty : constraintProperties) {
			for(Statement s : shapesModel.listStatements(null, constraintProperty, (RDFNode)null).toList()) {
				if(s.getObject().isResource()) {
					Resource shape = s.getSubject();
					if(hasScope(shape, dataModel, validateShapes)) {
						List<SHACLConstraint> list = map.get(shape);
						if(list == null) {
							list = new LinkedList<SHACLConstraint>();
							map.put(shape, list);
						}
						Resource c = s.getResource(); 
						Resource type = JenaUtil.getType(c);
						if(type == null) {
							type = SHACLUtil.getResourceDefaultType(c);
						}
						if(type != null) {
							if(JenaUtil.hasIndirectType(c, SH.SPARQLConstraint)) {
								list.add(SHACLFactory.asSPARQLConstraint(c));
							}
							else if(JenaUtil.hasSuperClass(type, SH.Constraint)) {
								// Only execute property constraints if sh:predicate is present
								if(SH.constraint.equals(constraintProperty) || c.hasProperty(SH.predicate)) {
									list.add(SHACLFactory.asParameterizableConstraint(c));
								}
							}
						}
					}
				}
			}
		}
		return map;
	}
	
	
	// Used to filter out scopeless shapes that are only used as part of other shapes
	private boolean hasScope(Resource shape, Model dataModel, boolean validateShapes) {
		if(JenaUtil.hasIndirectType(shape, RDFS.Class)) {
			return true;
		}
		else if(shape.hasProperty(SH.scope)) {
			return true;
		}
		else if(shape.hasProperty(SH.scopeClass)) {
			return true;
		}
		else if(shape.hasProperty(SH.scopeNode)) {
			return true;
		}
		else if(validateShapes && shape.hasProperty(SH.context)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	
	private void validateConstraintForShape(Dataset dataset, URI shapesGraphURI, Resource minSeverity, SHACLConstraint constraint, Resource shape, Model results, ProgressMonitor monitor) {
		for(ConstraintExecutable executable : constraint.getExecutables()) {
			Resource severity = executable.getSeverity();
			if(SHACLUtil.hasMinSeverity(severity, minSeverity)) {
				if(monitor != null) {
					monitor.subTask("Validating Shape " + SPINLabels.get().getLabel(shape));
				}
				ExecutionLanguage lang = ExecutionLanguageSelector.get().getLanguageForConstraint(executable);
				notifyValidationStarting(shape, executable, null, lang, results);
				lang.executeConstraint(dataset, shape, shapesGraphURI, executable, null, results);
				notifyValidationFinished(shape, executable, null, lang, results);
			}
		}
	}
}
