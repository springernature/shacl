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

    Model shapesModel = null;

    public Validator(InputStream shapeFile) throws IOException {
        // Load File with Validation Shapes
        byte[] validationModel = IOUtils.toString(shapeFile, Charset.forName("UTF-8")).getBytes();
        // Create Model for Validation Shapes
        Model datasetTypeModel = JenaUtil.createMemoryModel();
        BufferedReader validationModelReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(validationModel)));
        datasetTypeModel.read(validationModelReader, "urn:dummy-validator", FileUtils.langTurtle);
        // Create Shacl Model
        Model shaclModel = SHACLSystemModel.getSHACLModel();
        // Unite Models
        Graph[] assessedGraphs = new Graph[2];
        assessedGraphs[0] = shaclModel.getGraph();
        assessedGraphs[1] = datasetTypeModel.getGraph();
        shapesModel = ModelFactory.createModelForGraph(new MultiUnion(assessedGraphs));
        SHACLFunctions.registerFunctions(ModelFactory.createModelForGraph(shapesModel.getGraph())); //todo: not sure if rdfFile will contain Functions...
    }

    public void validate(InputStream rdfFileContents) throws Exception {
        //Load RDF file
        Model rdfFileModel = JenaUtil.createMemoryModel();
        InputStreamReader rdfFileReader = new InputStreamReader(rdfFileContents, "UTF-8");
        rdfFileModel.read(rdfFileReader, "urn:dummy", FileUtils.langTurtle);

        // Create Dataset that contains both the main query model and the shapes model
        URI shapesGraphURI = URI.create("urn:x-shacl-shapes-graph:" + UUID.randomUUID().toString());
        Dataset dataset = ARQFactory.get().getDataset(rdfFileModel);
        dataset.addNamedModel(shapesGraphURI.toString(), shapesModel);

        // Validate
        Model results = ModelConstraintValidator.get().validateModel(dataset, shapesGraphURI, null, true, null, null);
        if (!results.isEmpty()) {
            throw new Exception(ModelPrinter.get().print(results));
        }
        return;
    }


}
