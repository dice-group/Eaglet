package org.aksw.simba.eaglet.errorutils;
/**
 * This file is part of General Entity Annotator Benchmark.
 *
 * General Entity Annotator Benchmark is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * General Entity Annotator Benchmark is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with General Entity Annotator Benchmark.  If not, see <http://www.gnu.org/licenses/>.
 */


import java.util.ArrayList;
import java.util.List;

import org.aksw.gerbil.dataset.Dataset;
import org.aksw.gerbil.dataset.DatasetConfiguration;
import org.aksw.gerbil.dataset.impl.nif.NIFFileDatasetConfig;
import org.aksw.gerbil.datatypes.ExperimentTaskConfiguration;
import org.aksw.gerbil.datatypes.ExperimentType;
import org.aksw.gerbil.evaluate.EvaluatorFactory;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.matching.Matching;
import org.aksw.gerbil.semantic.kb.SimpleWhiteListBasedUriKBClassifier;
import org.aksw.gerbil.semantic.kb.UriKBClassifier;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.data.DocumentImpl;
import org.aksw.gerbil.transfer.nif.data.NamedEntity;
import org.aksw.simba.eaglet.annotator.TestA2KBAnnotator;
import org.aksw.simba.eaglet.database.SimpleLoggingResultStoringDAO4Debugging;


/**
 * This class tests the entity linking evaluation.
 * 
 * @author Michael R&ouml;der <roeder@informatik.uni-leipzig.de>
 * 
 */

public class FileBasedA2KBTest extends AbstractExperimentTaskTest {

    private static final DatasetConfiguration GOLD_STD = new NIFFileDatasetConfig("DBpedia",
            "C:/Users/Kunal/workspace/gerbil/gerbil_data/datasets/spotlight/dbpedia-spotlight-nif.ttl", false, ExperimentType.A2KB, null, null);
    
	private static final UriKBClassifier URI_KB_CLASSIFIER = new SimpleWhiteListBasedUriKBClassifier(
            "http://dbpedia.org/resource/");
    private static final ExperimentType EXPERIMENT_TYPE = ExperimentType.A2KB;

    
    static String filename_annotator= "C:/Users/Kunal/workspace/gerbil/Results_anontator_dbpedia/Wikipedia_Miner-DBpediaSpotlight-s-A2KB.ttl";
    public static void main(String[] args) {
		FileBasedA2KBTest fb_test= new FileBasedA2KBTest(filename_annotator, GOLD_STD,Matching.STRONG_ANNOTATION_MATCH);
		try {
			fb_test.testWithoutConfidence();
		} catch (GerbilException e) {
			
			e.printStackTrace();
		}
    	
	}

    private String annotatorFileName;
    private DatasetConfiguration dataset;
    private double expectedResults[];
    private double expectedResultWithoutConfidence[];
    private Matching matching;

    public FileBasedA2KBTest(String annotatorFileName, DatasetConfiguration dataset, Matching matching) {
        this.annotatorFileName = annotatorFileName;
        this.dataset = dataset;
        this.expectedResults = expectedResults;
        this.expectedResultWithoutConfidence = expectedResultWithoutConfidence;
        this.matching = matching;
    }

    public void testWithoutConfidence() throws GerbilException {
        int experimentTaskId = 1;
        SimpleLoggingResultStoringDAO4Debugging experimentDAO = new SimpleLoggingResultStoringDAO4Debugging();

        ExperimentTaskConfiguration configuration = new ExperimentTaskConfiguration(
                loadAnnotatorFile(annotatorFileName, true), dataset, EXPERIMENT_TYPE, matching);
        runTest(experimentTaskId, experimentDAO, new EvaluatorFactory(URI_KB_CLASSIFIER), configuration);
    }

    public TestA2KBAnnotator loadAnnotatorFile(String annotatorFileName, boolean eraseConfidenceValues)
            throws GerbilException {
        Dataset dataset = (new NIFFileDatasetConfig("ANNOTATOR", annotatorFileName, false, EXPERIMENT_TYPE, null, null))
                .getDataset(EXPERIMENT_TYPE);
        List<Document> instances;
        if (eraseConfidenceValues) {
            instances = new ArrayList<Document>(dataset.size());
            Document newDoc;
            for (Document originalDoc : dataset.getInstances()) {
                newDoc = new DocumentImpl();
                newDoc.setDocumentURI(originalDoc.getDocumentURI());
                newDoc.setText(originalDoc.getText());
                for (NamedEntity ne : originalDoc.getMarkings(NamedEntity.class)) {
                    newDoc.addMarking(new NamedEntity(ne.getStartPosition(), ne.getLength(), ne.getUris()));
                }
                instances.add(newDoc);
            }
        } else {
            instances = dataset.getInstances();
        }
        return new TestA2KBAnnotator(instances);
    }
}
