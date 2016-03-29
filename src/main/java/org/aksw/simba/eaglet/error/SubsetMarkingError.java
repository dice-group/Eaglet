package org.aksw.simba.eaglet.error;

import java.util.*;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections.Check;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubsetMarkingError implements ErrorChecker {
	private static final Logger LOGGER = LoggerFactory.getLogger(SubsetMarkingError.class);
	/*
	 * private static final DatasetConfiguration DATASET = new
	 * NIFFileDatasetConfig("DBpedia",
	 * "C:/Users/Kunal/workspace/gerbil/gerbil_data/datasets/spotlight/dbpedia-spotlight-nif.ttl",
	 * false, ExperimentType.A2KB);
	 */

	static String text;
	static String entity_name;

	public void subsetmark(List<Document> documents) throws GerbilException {
		LOGGER.info(" SUBSET MARKING MODULE RUNNING");

		// DATASET.getDataset(ExperimentType.A2KB).getInstances();

		for (Document doc : documents) { // getting list of documents
			text = doc.getText();
			List<NamedEntityCorrections> entities = doc.getMarkings(NamedEntityCorrections.class);
			for (NamedEntityCorrections entity : entities) {
				// If there are letters in front check for a whitespace
				if (entity.getStartPosition() > 0) {
					if (!Character.isWhitespace(text.charAt(entity.getStartPosition() - 1))) {
						entity.setResult(Check.DELETED);
					}
				}
				// If there are letters behind check for letters or digits
					if (entity.getStartPosition() + entity.getLength() < text.length()) {
						if (Character.isLetterOrDigit(text.charAt(entity.getStartPosition() + entity.getLength()))) {
							entity.setResult(Check.DELETED);
						}
					}
			}
		}

	}

	@Override
	public void check(List<Document> documents) throws GerbilException {
		// TODO Auto-generated method stub
		this.subsetmark(documents);

	}

	/*
	 * public static void printlist(Set<Problem_Entity> pe2) { for
	 * (Problem_Entity x : pe2) { System.out.println("DOC ID " + x.getDoc());
	 * System.out.println("ENTITY NAME: " + x.getEntity_name());
	 * System.out.println("ENTITY LENGTH " + x.getLength()); System.out.println(
	 * "ENTITY TEXT " + x.getProblem_text()); System.out.println(
	 * "ENTITY START POS " + x.getStart_pos()); System.out.println(
	 * "ENTITY TEXT " + x.getEntity_text()); System.out.println(
	 * "=================================================================="); }
	 */

}
