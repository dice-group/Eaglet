package org.aksw.gscheck.error;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.gerbil.dataset.DatasetConfiguration;
import org.aksw.gerbil.dataset.impl.nif.NIFFileDatasetConfig;
import org.aksw.gerbil.datatypes.ExperimentType;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.data.NamedEntity;

public class erratic {
	private static final DatasetConfiguration DATASET = new NIFFileDatasetConfig("DBpedia",
			"C:/Users/Kunal/workspace/gerbil/gerbil_data/datasets/spotlight/dbpedia-spotlight-nif.ttl", false,
			ExperimentType.A2KB);

	static Set<problem_entity> entity_set = new HashSet<problem_entity>();
	static Set<problem_entity> lemma_set = new HashSet<problem_entity>();
	static String entity_name;
	static problem_entity funny_entity = new problem_entity();
	static String text;
	static Set<problem_entity> missedentity_set = new HashSet<problem_entity>();

	public static void main(String[] args) throws GerbilException {
		List<Document> documents = DATASET.getDataset(ExperimentType.A2KB).getInstances();
		lemmacreator lc = new lemmacreator();
		problem_entity dummy_entity = new problem_entity();

		for (Document doc : documents) {
			text = doc.getText();
			List<NamedEntity> entities = doc.getMarkings(NamedEntity.class);
			for (NamedEntity entity : entities) {
				// creating the entity set for all documents.
				entity_name = entity.getUri();
				entity_name = entity_name.substring(entity.getUri().lastIndexOf("/") + 1);
				dummy_entity.setEntity_name(entity_name);
				dummy_entity.setStart_pos(entity.getStartPosition());
				dummy_entity.setEnd_pos(entity.getStartPosition() + entity.getLength());
				dummy_entity.setDoc(doc.getDocumentURI());
				entity_set.add(dummy_entity);
			}

			// creating the lemma set document wise.
			List<problem_entity> dummy = lc.lemmatize(doc);
			lemma_set.addAll(dummy);
		}
		// check for each member in lemma set with all the members of the entity
		// set.
		for (problem_entity le : lemma_set) {
			for (problem_entity es : entity_set) {
				if ((es.getEntity_name().equals(le.getEntity_name())) && (es.getDoc().equals(le.getDoc()))) {
					if ((es.getStart_pos() != le.getStart_pos()) && (es.getEnd_pos() != le.getEnd_pos())) {
						missedentity_set.add(le);
					}
				} else if ((es.getEntity_name().equals(le.getEntity_name())) && (!es.getDoc().equals(le.getDoc()))) {
					missedentity_set.add(le);
				}
			}
		}
		printlist(missedentity_set);
	}

	public static void printlist(Set<problem_entity> list) {
		System.out.println("The missing entities are:");
		for (problem_entity x : list) {
			System.out.println("DOC ID " + x.getDoc());
			System.out.println("NAME: " + x.getEntity_name());
			System.out.println("LENGTH " + x.getLength());
			System.out.println("START POS " + x.getStart_pos());
			System.out.println("END POS" + x.getEnd_pos());
			System.out.println("==================================================================");
		}
	}
}
