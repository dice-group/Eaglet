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
import org.aksw.gscheck.errorutils.DocumentProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;

public class LongDescriptionError {
	private static final Logger LOGGER = LoggerFactory.getLogger(ErraticEntityError.class);

	private static final DatasetConfiguration DATASET = new NIFFileDatasetConfig("DBpedia",
			"gerbil_data/datasets/spotlight/dbpedia-spotlight-nif.ttl", false, ExperimentType.A2KB);
	static DocumentProcessor dp = new DocumentProcessor();

	public static void LongDescription() throws GerbilException {
		List<Document> documents = DATASET.getDataset(ExperimentType.A2KB).getInstances();
		LOGGER.info("LONG DESCRIPTION MODULE RUNNING");

		for (Document doc : documents) {
			String text = doc.getText();
			List<NamedEntity> entities = doc.getMarkings(NamedEntity.class);
			List<CoreLabel> POSBlackList = dp.LongEntity_Extracter_util(text);
			Collections.sort(entities, new StartPosBasedComparator());

			for (NamedEntity entity : entities) {
				String entity_text = text.substring(entity.getStartPosition(),
						entity.getLength() + entity.getStartPosition());
				String[] arr = entity_text.split(" ");
				for (String dummy : arr) {
					for (CoreLabel bentity : POSBlackList) {
						if (bentity.get(TextAnnotation.class).equals(dummy)) {
							System.out.println(dummy + "-----> "
									+ text.substring(entity.getStartPosition(),
											entity.getLength() + entity.getStartPosition())
									+ " " + bentity.get(TextAnnotation.class) + " " + entity.getStartPosition() + " "
									+ entity.getLength());
						}
					}
				}

			}

		}

	}

}
