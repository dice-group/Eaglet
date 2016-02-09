package org.aksw.gscheck.error;

import java.util.List;

import org.aksw.gerbil.dataset.DatasetConfiguration;
import org.aksw.gerbil.dataset.impl.nif.NIFFileDatasetConfig;
import org.aksw.gerbil.datatypes.ExperimentType;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.data.NamedEntity;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;

public class LongDescriptionError {

	private static final DatasetConfiguration DATASET = new NIFFileDatasetConfig("DBpedia",
			"gerbil_data/datasets/spotlight/dbpedia-spotlight-nif.ttl", false, ExperimentType.A2KB);

	public static void main(String[] args) throws GerbilException {
		List<Document> documents = DATASET.getDataset(ExperimentType.A2KB).getInstances();

		DocumentProcessor dp = new DocumentProcessor();
		List<NamedEntity> entities;
		for (Document doc : documents) {
			String text = doc.getText();
			List<CoreLabel> eligible_makrings = dp.LongEntity_Extracter_util(text);

			entities = doc.getMarkings(NamedEntity.class);

			for (NamedEntity entity : entities) {
				String[] arr = text.substring(entity.getStartPosition(), entity.getStartPosition() + entity.getLength())
						.split(" ");

				for (String x : arr) {
					for (CoreLabel z : eligible_makrings) {
						if (z.get(TextAnnotation.class).equals(x)) {
							System.out.println(z.get(TextAnnotation.class) + " " + z.get(PartOfSpeechAnnotation.class)
									+ " " + z.beginPosition() + " " + z.endPosition() + " ---->" + text.substring(
											entity.getStartPosition(), entity.getStartPosition() + entity.getLength()));

						}
					}
				}

			}
		}
	}

}
