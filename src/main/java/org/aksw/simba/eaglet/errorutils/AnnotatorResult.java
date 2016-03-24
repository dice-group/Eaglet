package org.aksw.simba.eaglet.errorutils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.aksw.gerbil.dataset.Dataset;
import org.aksw.gerbil.dataset.DatasetConfiguration;
import org.aksw.gerbil.dataset.impl.nif.NIFFileDatasetConfig;
import org.aksw.gerbil.datatypes.ExperimentType;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.semantic.kb.SimpleWhiteListBasedUriKBClassifier;
import org.aksw.gerbil.semantic.kb.UriKBClassifier;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.data.NamedEntity;
import org.aksw.simba.eaglet.corrections.NamedEntityCorrections;

public class AnnotatorResult {

	private static final DatasetConfiguration GOLD_STD = new NIFFileDatasetConfig("DBpedia",
			"C:/Users/Kunal/workspace/gerbil/gerbil_data/datasets/spotlight/dbpedia-spotlight-nif.ttl", false,
			ExperimentType.A2KB);

	private static final UriKBClassifier URI_KB_CLASSIFIER = new SimpleWhiteListBasedUriKBClassifier(
			"http://dbpedia.org/resource/");
	private static final ExperimentType EXPERIMENT_TYPE = ExperimentType.A2KB;

	public static void printlist(ArrayList<NamedEntity> result_set) {
		for (NamedEntity x : result_set) {
			System.out.println("Entity ID " + x.getUri());
			System.out.println("ENTITY START POS " + x.getStartPosition());
			System.out.println("==================================================================");
		}

	}

	public static ArrayList<NamedEntityCorrections> loadAnnotator(String annotatorFileName, String AnnotatorName)
			throws GerbilException {
		Dataset dataset = (new NIFFileDatasetConfig("ANNOTATOR", annotatorFileName, false, EXPERIMENT_TYPE))
				.getDataset(EXPERIMENT_TYPE);
		ArrayList<NamedEntityCorrections> entity_set = new ArrayList<NamedEntityCorrections>();

		List<Document> documents = dataset.getInstances();
		// System.out.println(documents.get(0).getDocumentURI());
		for (Document doc : documents) {
			List<NamedEntityCorrections> entities = doc.getMarkings(NamedEntityCorrections.class);

			entity_set.addAll(entities);
		}

		return entity_set;
	}

}