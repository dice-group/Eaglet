package org.aksw.gscheck.error;

import java.util.List;

import org.aksw.gerbil.dataset.DatasetConfiguration;
import org.aksw.gerbil.dataset.impl.nif.NIFFileDatasetConfig;
import org.aksw.gerbil.datatypes.ExperimentType;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.data.NamedEntity;

public class CombinedTaggingError {
	private static final DatasetConfiguration DATASET = new NIFFileDatasetConfig("DBpedia",
			"C:/Users/Kunal/workspace/gerbil/gerbil_data/datasets/spotlight/dbpedia-spotlight-nif.ttl", false,
			ExperimentType.A2KB);

	static String text;

	public static void main(String[] args) throws GerbilException {
		List<Document> documents = DATASET.getDataset(ExperimentType.A2KB).getInstances();
		for (Document doc : documents) {
			text = doc.getText();
			List<NamedEntity> entities = doc.getMarkings(NamedEntity.class);
			for (NamedEntity entity : entities) {
			}

		}
	}

}
