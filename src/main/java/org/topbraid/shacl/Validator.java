package org.topbraid.shacl;

import org.apache.commons.io.IOUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.compose.MultiUnion;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileUtils;
import org.topbraid.shacl.arq.SHACLFunctions;
import org.topbraid.shacl.constraints.ModelConstraintValidator;
import org.topbraid.shacl.util.ModelPrinter;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.util.JenaUtil;

import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.UUID;

public class Validator {
    static byte[] validationModel = null;
    static Model shapesModel = null;

    public Validator(InputStream shapeFile) throws IOException {
        validationModel = IOUtils.toString(shapeFile, Charset.forName("UTF-8")).getBytes();
        Model datasetTypeModel = JenaUtil.createMemoryModel();
        BufferedReader validationModelReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(validationModel)));
        datasetTypeModel.read(validationModelReader, "urn:dummy-validator", FileUtils.langTurtle);
        Model shaclModel = SHACLSystemModel.getSHACLModel();
        Graph[] assessedGraphs = new Graph[2];
        assessedGraphs[0] = shaclModel.getGraph();
        assessedGraphs[1] = datasetTypeModel.getGraph();
        shapesModel = ModelFactory.createModelForGraph(new MultiUnion(assessedGraphs));

        // Make sure all sh:Functions are registered
        SHACLFunctions.registerFunctions(ModelFactory.createModelForGraph(shapesModel.getGraph())); //todo: not sure if rdfFile will contain Functions...
    }

    public void validate(InputStream rdfFileContents) throws Exception {
        //Read Input RDF file
        Model rdfFileModel = JenaUtil.createMemoryModel();
        InputStreamReader rdfFileREader = new InputStreamReader(rdfFileContents, "UTF-8");
        rdfFileModel.read(rdfFileREader, "urn:dummy", FileUtils.langTurtle);

        // Create Dataset that contains both the main query model and the shapes model
        // (here, using a temporary URI for the shapes graph)
        URI shapesGraphURI = URI.create("urn:x-shacl-shapes-graph:" + UUID.randomUUID().toString());
        Dataset dataset = ARQFactory.get().getDataset(rdfFileModel);
        dataset.addNamedModel(shapesGraphURI.toString(), shapesModel);

        // Run the validator
        Model results = ModelConstraintValidator.get().validateModel(dataset, shapesGraphURI, null, true, null, null);
        if (!results.isEmpty()) {
            throw new Exception(ModelPrinter.get().print(results));
        }
        return;
    }


}
