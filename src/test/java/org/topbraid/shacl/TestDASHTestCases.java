package org.topbraid.shacl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileUtils;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.topbraid.shacl.testcases.TestCase;
import org.topbraid.shacl.testcases.TestCaseType;
import org.topbraid.shacl.testcases.TestCaseTypes;
import org.topbraid.shacl.vocabulary.DASH;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;

@RunWith(Parameterized.class)
public class TestDASHTestCases {

	@Parameters(name="{0}")
	public static Collection<Object[]> data() throws Exception {

		List<TestCase> testCases = new LinkedList<TestCase>();
		File rootFolder = new File("src/test/resources");
		collectTestCases(rootFolder, testCases);
		
		List<Object[]> results = new LinkedList<Object[]>();
		for(TestCase testCase : testCases) {
			results.add(new Object[]{ testCase });
		}
	    return results;
	}

	
	private static void collectTestCases(File folder, List<TestCase> testCases) throws Exception {
		for(File f : folder.listFiles()) {
			if(f.isDirectory()) {
				collectTestCases(f, testCases);
			}
			else if(f.isFile() && f.getName().endsWith(".ttl")) {
				Model testModel = JenaUtil.createDefaultModel();
				InputStream is = new FileInputStream(f);
				testModel.read(is, "urn:dummy", FileUtils.langTurtle);
				testModel.add(SHACLSystemModel.getSHACLModel());
				StmtIterator stmtIterator = testModel.listStatements(null, OWL.imports, ResourceFactory.createResource(DASH.BASE_URI));
				if(stmtIterator.hasNext()) {
					Resource ontology = stmtIterator.next().getSubject();
					for (TestCaseType type : TestCaseTypes.getTypes()) {
						testCases.addAll(type.getTestCases(testModel, ontology));
					}
				}
			}
		}
	}
	
	
	private TestCase testCase;
	
	public TestDASHTestCases(TestCase testCase) {
		this.testCase = testCase;
	}
	
	
	@Test
	public void testTestCase() {
		Model results = JenaUtil.createMemoryModel();
		try {
			testCase.run(results);
		}
		catch(Exception ex) {
			testCase.createFailure(results, "Exception during test case execution: " + ex);
			ex.printStackTrace();
		}
		for(Statement s : results.listStatements(null, RDF.type, DASH.FailureTestCaseResult).toList()) {
			String message = JenaUtil.getStringProperty(s.getSubject(), SH.message);
			if(message == null) {
				message = "(No " + SH.PREFIX + ":" + SH.message.getLocalName() + " found in failure)";
			}
			Assert.fail(message);
		}
	}
}
