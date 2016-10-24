package org.topbraid.shacl;

import java.net.URI;
import java.util.UUID;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.compose.MultiUnion;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileUtils;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.Lang;
import org.topbraid.shacl.arq.SHACLFunctions;
import org.topbraid.shacl.constraints.ModelConstraintValidator;
import org.topbraid.shacl.util.ModelPrinter;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.util.JenaUtil;

import java.util.ArrayList;
import java.util.Map;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileReader;

public class SimpleValidator {

	/**
	 * Loads an example SHACL file and validates all constraints.
	 * This file can also be used as a starting point for your own custom applications.
	 */
	public static void main(String[] args) throws Exception {
		
		// Load the main data model
		Model stdinModel = JenaUtil.createMemoryModel();
		
		InputStreamReader cin = new InputStreamReader(System.in);
		stdinModel.read(cin, "urn:dummy", FileUtils.langTurtle);

		ArrayList<Model> argModels = new ArrayList();
		for (String path : args) {
			Model argModel = JenaUtil.createMemoryModel();
			BufferedReader argReader = new BufferedReader(new FileReader(path));
			argModel.read(argReader, "urn:dummy-" + path, FileUtils.langTurtle);

			argModels.add(argModel);
		}

		// Load the shapes Model. 
		// The stdinModel too can have shape definitions, hence they all get concatenated below.
		// In case of a validation success (exit code 0), the data model (but no SHACL or other arguments passed in).
		// This is so that the tool can be used in a larger pipeline (passing through its input). 
		Model shaclModel = SHACLSystemModel.getSHACLModel();
		
		ArrayList<Model> assessedModels = new ArrayList(argModels);
		assessedModels.add(shaclModel);
		assessedModels.add(stdinModel);

		Graph[] assessedGraphs = assessedModels.stream()
									.map(model -> model.getGraph())
									.toArray(size -> new Graph[size]);

		MultiUnion unionGraph = new MultiUnion(assessedGraphs);
		Model shapesModel = ModelFactory.createModelForGraph(unionGraph);

		// Make sure all sh:Functions are registered
		SHACLFunctions.registerFunctions(shapesModel);
		
		// Create Dataset that contains both the main query model and the shapes model
		// (here, using a temporary URI for the shapes graph)
		URI shapesGraphURI = URI.create("urn:x-shacl-shapes-graph:" + UUID.randomUUID().toString());
		Dataset dataset = ARQFactory.get().getDataset(stdinModel);
		dataset.addNamedModel(shapesGraphURI.toString(), shapesModel);
		
		// Run the validator
		Model results = ModelConstraintValidator.get().validateModel(dataset, shapesGraphURI, null, true, null, null);
				
		if (!results.isEmpty()) {
			System.out.println(ModelPrinter.get().print(results));
			System.exit(1);
		}

		RDFDataMgr.write(System.out, stdinModel, Lang.TTL);
	}
}