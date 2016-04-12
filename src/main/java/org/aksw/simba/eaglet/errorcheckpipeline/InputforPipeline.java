package org.aksw.simba.eaglet.errorcheckpipeline;

import java.io.IOException;
import java.util.List;

import org.aksw.gerbil.dataset.DatasetConfiguration;
import org.aksw.gerbil.dataset.impl.nif.NIFFileDatasetConfig;
import org.aksw.gerbil.datatypes.ExperimentType;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.simba.eaglet.documentprocessor.DocumentProcessor;
import org.aksw.simba.eaglet.entitytypemodify.EntityTypeChange;

public class InputforPipeline {
	private DatasetConfiguration DATASET = new NIFFileDatasetConfig("DBpedia",
			"C:/Users/Kunal/workspace/gerbil/Results_anontator_dbpedia/WAT-DBpediaSpotlight-s-A2KB.ttl", false,
			ExperimentType.A2KB);

	public InputforPipeline(int user) throws GerbilException, IOException {
		List<Document> documents = DATASET.getDataset(ExperimentType.A2KB).getInstances();
		PrePipeProcessor(documents);
		callPipe(documents, user);
	}

	public void PrePipeProcessor(List<Document> documents) throws GerbilException {
		
		for (Document doc : documents) {
			List<Marking> list = EntityTypeChange.changeType(doc);
			doc.setMarkings(list);
		}
		DocumentProcessor dp = new DocumentProcessor();
		dp.process(documents);
		

	}

	public void callPipe(List<Document> doc, int user) throws GerbilException, IOException {
		CheckerPipeline.startPipe(doc,user);
	}
}
