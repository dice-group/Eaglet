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

public class ErraticEntityError {
	private static final DatasetConfiguration DATASET = new NIFFileDatasetConfig("DBpedia",
			"C:/Users/Kunal/workspace/gerbil/gerbil_data/datasets/spotlight/dbpedia-spotlight-nif.ttl", false,
			ExperimentType.A2KB);

	static Set<Problem_Entity> entity_set = new HashSet<Problem_Entity>();
	static Set<Problem_Entity> lemma_set = new HashSet<Problem_Entity>();
	static String entity_name;
	static Problem_Entity funny_entity = new Problem_Entity();
	static String text;
	static Set<Problem_Entity> missedentity_set = new HashSet<Problem_Entity>();

	public static void main(String[] args) throws GerbilException {
		List<Document> documents = DATASET.getDataset(ExperimentType.A2KB).getInstances();
		LemmaCreator lc = new LemmaCreator();
		
		for (Document doc : documents) {
			text = doc.getText();
			List<NamedEntity> entities = doc.getMarkings(NamedEntity.class);
			for (NamedEntity entity : entities) {
				// creating the entity set for all documents.
				entity_name = lc.lemmatize_entity(
						text.substring(entity.getStartPosition(), entity.getStartPosition() + entity.getLength()));
				// lc.lemmatize_entity(text.substring(entity.getStartPosition(),
				// entity.getStartPosition() + entity.getLength()));
				// ystem.out.println( entity_name+"\n" +"]]]]]] ");
				Problem_Entity dummy_entity = new Problem_Entity();

				dummy_entity.setEntity_name(entity_name);
				dummy_entity.setStart_pos(entity.getStartPosition());
				dummy_entity.setEnd_pos(entity.getStartPosition() + entity.getLength());
				dummy_entity.setDoc(doc.getDocumentURI());
				entity_set.add(dummy_entity);

			}
			List<Problem_Entity> dummy = lc.lemmatize(doc);
			lemma_set.addAll(dummy);
			

		}
		/*
		for (Problem_Entity le : entity_set)
		{
			System.out.println(le.getEntity_name());
			System.out.println(le.getStart_pos());
			System.out.println(le.getLength());
		}*/

		// creating the lemma set document wise.

		// check for each member in lemma set with all the members of the entity
		// set.
		for (Problem_Entity le : lemma_set) {
			if (entity_set.contains(le)) {
				break;
			}
			//System.out.println(le.getEntity_name());
			for (Problem_Entity es : entity_set) {
				if ((le.getEntity_name().equals(es.getEntity_name())) && (le.getDoc().equals(es.getDoc()))) 
				{

					if ((le.getStart_pos() >= es.getStart_pos() && (le.getEnd_pos() <= es.getEnd_pos()))) {
						break;
					} else {
						missedentity_set.add(le);
					}
				} else if ((le.getEntity_name().equals(es.getEntity_name())) && !(le.getDoc().equals(es.getDoc()))) {
					missedentity_set.add(le);
				}

			}
		}

		printlist(missedentity_set);
	}

	public static void printlist(Set<Problem_Entity> list) {
		System.out.println("The missing entities are:");
		for (Problem_Entity x : list) {
			System.out.println("DOC ID " + x.getDoc());
			System.out.println("NAME: " + x.getEntity_name());
			System.out.println("LENGTH " + x.getLength());
			System.out.println("START POS " + x.getStart_pos());
			System.out.println("END POS " + x.getEnd_pos());
			System.out.println("==================================================================");
		}
	}
}
