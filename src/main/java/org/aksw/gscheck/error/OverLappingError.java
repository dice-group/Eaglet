package org.aksw.gscheck.error;

import java.util.Collections;
import java.util.List;

import org.aksw.gerbil.dataset.DatasetConfiguration;
import org.aksw.gerbil.dataset.impl.nif.NIFFileDatasetConfig;
import org.aksw.gerbil.datatypes.ExperimentType;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.data.NamedEntity;
import org.aksw.gerbil.transfer.nif.data.StartPosBasedComparator;

public class OverLappingError {

	private static final DatasetConfiguration DATASET = new NIFFileDatasetConfig("DBpedia",
			"gerbil_data/datasets/spotlight/dbpedia-spotlight-nif.ttl", false, ExperimentType.A2KB);

	public static void main(String[] args) throws GerbilException {
		List<Document> documents = DATASET.getDataset(ExperimentType.A2KB).getInstances();

		
		for (Document doc : documents) {
			String text = doc.getText();
			List<NamedEntity> entities = doc.getMarkings(NamedEntity.class);
			Collections.sort(entities, new StartPosBasedComparator());
			for (int i = 0; i < entities.size() - 1; i++) {
				if ((entities.get(i).getStartPosition() + entities.get(i).getLength()) >= entities.get(i + 1)
						.getStartPosition()) {
					System.out.println(text.substring(entities.get(i).getStartPosition(),
							entities.get(i).getStartPosition() + entities.get(i).getLength()) + " "
							+ entities.get(i).getStartPosition() + " " + entities.get(i).getLength()
							+ " is colliding with "
							+ text.substring(entities.get(i + 1).getStartPosition(),
									entities.get(i + 1).getStartPosition() + entities.get(i + 1).getLength())
							+ " " + entities.get(i + 1).getStartPosition() + " "
							+ entities.get(i + 1).getLength());
				}
			}

		}

	}

}
