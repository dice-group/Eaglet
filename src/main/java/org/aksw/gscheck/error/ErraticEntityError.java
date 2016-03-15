package org.aksw.gscheck.error;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.gerbil.dataset.DatasetConfiguration;
import org.aksw.gerbil.dataset.impl.nif.NIFFileDatasetConfig;
import org.aksw.gerbil.datatypes.ExperimentType;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;

import org.aksw.gscheck.corrections.NamedEntityCorrections;
import org.aksw.gscheck.errorutils.NamedEntityCorrections;
import org.aksw.simba.gscheck.documentprocessor.DocumentProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErraticEntityError implements ErrorChecker {
	private static final Logger LOGGER = LoggerFactory.getLogger(ErraticEntityError.class);
	/*
	 * private static final DatasetConfiguration DATASET = new
	 * NIFFileDatasetConfig("DBpedia",
	 * "C:/Users/Kunal/workspace/gerbil/gerbil_data/datasets/spotlight/dbpedia-spotlight-nif.ttl",
	 * false, ExperimentType.A2KB);
	 */

	static Set<NamedEntityCorrections> entity_set = new HashSet<NamedEntityCorrections>();
	static Set<NamedEntityCorrections> lemma_set = new HashSet<NamedEntityCorrections>();
	static String entity_name;

	static String text;
	static Set<NamedEntityCorrections> missedentity_set = new HashSet<NamedEntityCorrections>();
	static DocumentProcessor dp = new DocumentProcessor();

	public void ErraticEntityProb(List<Document> documents) throws GerbilException {
		LOGGER.info(" ERRATIC ENTITY MODULE RUNNING");
		// List<Document> documents =
		// DATASET.getDataset(ExperimentType.A2KB).getInstances();

		for (Document doc : documents) {
			text = doc.getText();

			List<NamedEntityCorrections> entities = doc.getMarkings(NamedEntityCorrections.class);
			
				// creating the entity set for all documents.
				List<NamedEntityCorrections> dummy = dp.lemmatize(doc);
			lemma_set.addAll(dummy);

		
		/*
		 * for (Problem_Entity le : entity_set) {
		 * System.out.println(le.getEntity_name());
		 * System.out.println(le.getStart_pos());
		 * System.out.println(le.getLength()); }
		 */

		// creating the lemma set document wise.

		// check for each member in lemma set with all the members of the entity
		// set.
		for (NamedEntityCorrections le : lemma_set) {
			if (entity_set.contains(le)) {
				break;
			}
			// System.out.println(le.getEntity_name());
			for (NamedEntityCorrections es : entities) 
			{
				if ((le.getEntity_name().equals(es.getEntity_name())) && (le.getDoc().equals(es.getDoc()))) {

					if ((le.getStartPosition() >= es.getStartPosition() && (le.getEnd_pos() <= es.getEnd_pos()))) {
						break;
					} else {
						missedentity_set.add(le);
					}
				} else if ((le.getEntity_name().equals(es.getEntity_name())) && !(le.getDoc().equals(es.getDoc()))) {
					missedentity_set.add(le);
				}

			}
		}

	}

	/*public static void printlist(Set<Problem_Entity> list) {

		System.out.println("The missing entities are:");
		for (Problem_Entity x : list) {
			System.out.println("DOC ID " + x.getDoc());
			System.out.println("NAME: " + x.getEntity_name());
			System.out.println("LENGTH " + x.getLength());
			System.out.println("END POS " + x.getEnd_pos());
			System.out.println("==================================================================");
		}
	}
*/
	@Override
	public void check(List<Document> documents) throws GerbilException {
		// TODO Auto-generated method stub
		this.ErraticEntityProb(documents);
	}
}
