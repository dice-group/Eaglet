package org.aksw.gscheck.error;

import java.util.*;
import org.aksw.gerbil.dataset.DatasetConfiguration;
import org.aksw.gerbil.dataset.impl.nif.NIFFileDatasetConfig;
import org.aksw.gerbil.datatypes.ExperimentType;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.data.NamedEntity;
import org.aksw.gscheck.corrections.NamedEntityCorrections;
import org.aksw.gscheck.corrections.NamedEntityCorrections.Check;
import org.aksw.gscheck.errorutils.Problem_Entity;
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
	static int start_index;
	static int end_index;
	static String text;
	static int textstart;
	static int textend;
	static String entity_name;

	public void subsetmark(List<Document> documents) throws GerbilException {
		LOGGER.info(" SUBSET MARKING MODULE RUNNING");

		// DATASET.getDataset(ExperimentType.A2KB).getInstances();

		Set<Problem_Entity> pe = new HashSet<Problem_Entity>();

		for (Document doc : documents) { // getting list of documents
			text = doc.getText();
			List<NamedEntityCorrections> entities = doc.getMarkings(NamedEntityCorrections.class);
			for (NamedEntityCorrections entity : entities) { // getting list of
																// entity

				start_index = entity.getStartPosition();
				end_index = entity.getStartPosition() + entity.getLength();
				if (start_index > 0) {
					if (text.charAt(start_index - 1) != ' ') // check before
																// condition
					// Executed only when start condition of entity are not met
					{

						entity_name = entity.getUri();
						entity_name = entity_name.substring(entity.getUri().lastIndexOf("/") + 1);
						if (entity.getStartPosition() + entity.getLength() + 40 > text.length() - 1) {
							textend = text.length();
						} else
							textend = entity.getStartPosition() + entity.getLength() + 40;

						if ((entity.getStartPosition() - 40) > 0) {
							textstart = entity.getStartPosition() - 40;
						} else
							textstart = 0;

						entity.setResult(Check.DELETED);
						/*
						 * funny_entity.setDoc(doc.getDocumentURI());
						 * funny_entity.setEntity_text(text.substring(entity.
						 * getStartPosition(), entity.getStartPosition() +
						 * entity.getLength()));
						 * funny_entity.setEntity_name(entity_name);
						 * funny_entity.setLength(entity.getLength());
						 * funny_entity.setProblem_text(text.substring(
						 * textstart, textend));
						 * funny_entity.setStart_pos(entity.getStartPosition());
						 * 
						 * pe.add(funny_entity);
						 */

					}
				}

				else if (end_index < text.length()) {
					// check for end condition. Executed only when end condition
					// of entity are not met
					if ((text.charAt(end_index + 1) != ' ') || (text.charAt(end_index + 1) != ',')
							|| (text.charAt(end_index + 1) != '.') || (text.charAt(end_index + 1) != ';')
							|| (text.charAt(end_index + 1) != ':') || (text.charAt(end_index + 1) != '-')) {

						entity_name = entity.getUri();
						entity_name = entity_name.substring(entity.getUri().lastIndexOf("/") + 1);
						if (entity.getStartPosition() + entity.getLength() + 40 > text.length() - 1) {
							textend = text.length();
						} else
							textend = entity.getStartPosition() + entity.getLength() + 40;

						if ((entity.getStartPosition() - 40) > 0) {
							textstart = entity.getStartPosition() - 40;
						} else
							textstart = 0;

						entity.setResult(Check.DELETED);

						/*
						 * funny_entity.setDoc(doc.getDocumentURI());
						 * funny_entity.setEntity_text(text.substring(entity.
						 * getStartPosition(), entity.getStartPosition() +
						 * entity.getLength()));
						 * funny_entity.setEntity_name(entity_name);
						 * funny_entity.setLength(entity.getLength());
						 * funny_entity.setProblem_text(text.substring(
						 * textstart, textend));
						 * funny_entity.setStart_pos(entity.getStartPosition());
						 * // adding the failed entity to list
						 * 
						 * pe.add(funny_entity);
						 */
					}
				}

			}

		}
		// printlist(pe);

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
