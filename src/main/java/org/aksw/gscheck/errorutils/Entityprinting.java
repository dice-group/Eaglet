package org.aksw.gscheck.errorutils;
import java.util.List;

import org.aksw.gerbil.dataset.DatasetConfiguration;
import org.aksw.gerbil.dataset.impl.nif.NIFFileDatasetConfig;
import org.aksw.gerbil.datatypes.ExperimentType;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.data.NamedEntity;

public class Entityprinting {

	private static final DatasetConfiguration DATASET = new NIFFileDatasetConfig("DBpedia",
			"C:/Users/Kunal/workspace/gerbil/Results_anontator_dbpedia/WAT-DBpediaSpotlight-s-A2KB.ttl", false,
			ExperimentType.A2KB);

	public static void main(String[] args) throws GerbilException {

		List<Document> documents = DATASET.getDataset(ExperimentType.A2KB).getInstances();
		String text;
		String example;
		int start_index;
		int end_index;
		for (Document doc : documents) {
			text = doc.getText();
			List<NamedEntity> entities = doc.getMarkings(NamedEntity.class);
			for (NamedEntity entity : entities) {
				example = entity.getUri();
				example = example.substring(example.lastIndexOf("/") + 1);
				// example=example.substring(0, example.indexOf(",")-1);
				if ((entity.getStartPosition() - 40) > 0) {
					start_index = entity.getStartPosition() - 40;
				} else
					start_index = 0;

				String before = text.substring(start_index, entity.getStartPosition());

				if (entity.getStartPosition() + entity.getLength() + 40 > text.length() - 1) {
					end_index = text.length();
				} else
					end_index = entity.getStartPosition() + entity.getLength() + 40;
				String after = text.substring(entity.getStartPosition() + entity.getLength(), end_index);
				String entity_text = text.substring(entity.getStartPosition(),
						entity.getStartPosition() + entity.getLength());

				System.out.println("\"" + doc.getDocumentURI() + "_" + doc.getText().hashCode() + "\"" + ","
						+ entity.getStartPosition() + "," + entity.getLength() + "," + "\"" + example + "\"" + ","
						+ "\"" + before + "|\"" + "," + "\"" + entity_text + "\"" + "," + "\"|" + after + "\"");

				// System.out.println(doc.getDocumentURI() + "," +
				// entity.getStartPosition() + "," + entity.getLength()
				// + ", " + example.substring(example.lastIndexOf("/") + 1));

			}

		}

	}
}
