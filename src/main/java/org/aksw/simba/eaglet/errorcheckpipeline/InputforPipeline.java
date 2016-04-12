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
	private String name = "eaglet_data/Results_anontator_dbpedia/WAT-DBpediaSpotlight-s-A2KB.ttl";
	private DatasetConfiguration DATASET = new NIFFileDatasetConfig("DBpedia", name, false, ExperimentType.A2KB);

	public InputforPipeline() throws GerbilException, IOException {
		List<Document> documents = DATASET.getDataset(ExperimentType.A2KB).getInstances();

		PrePipeProcessor(documents);
		name = name.substring(name.lastIndexOf('/'));
		name = name.replaceAll(".ttl", "");
		callPipe(documents, name);
	}

	public void PrePipeProcessor(List<Document> documents) throws GerbilException {

		for (Document doc : documents) {
			List<Marking> list = EntityTypeChange.changeType(doc);
			doc.setMarkings(list);
		}
		DocumentProcessor dp = new DocumentProcessor();
		dp.process(documents);

	}

	public void callPipe(List<Document> doc, String datasetname) throws GerbilException, IOException {
		CheckerPipeline.startPipe(doc, datasetname);
	}

	public static void main(String[] args) throws GerbilException, IOException {
		new InputforPipeline();

	}
}
