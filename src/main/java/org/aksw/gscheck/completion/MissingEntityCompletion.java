package org.aksw.gscheck.completion;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.aksw.gerbil.dataset.DatasetConfiguration;
import org.aksw.gerbil.dataset.impl.nif.NIFFileDatasetConfig;
import org.aksw.gerbil.datatypes.ExperimentType;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.data.NamedEntity;
import org.aksw.gscheck.corrections.NamedEntityCorrections;
import org.aksw.gscheck.corrections.NamedEntityCorrections.Check;
import org.aksw.gscheck.errorutils.AnnotatorResult;

public class MissingEntityCompletion {
	public static void main(String[] args) throws GerbilException {
		File folder = new File("C:/Users/Kunal/workspace/gerbil/Results_anontator_dbpedia");
		File[] listOfFiles = folder.listFiles();
		
		for (File file : listOfFiles) {
			if (file.isFile()) {
				String annotatorFilenName = file.getParent() + "/" + file.getName();
				// System.out.println(annotatorFilenName);
				System.out.println(file.getName());
				ArrayList<NamedEntityCorrections> result_annotator = AnnotatorResult.loadAnnotator(annotatorFilenName,
						file.getName());
				ArrayList<NamedEntityCorrections> result_set = CompareWithGS(result_annotator);

				//AnnotatorResult.printlist(result_set);

			}

		}

	}

	public static ArrayList<NamedEntityCorrections> CompareWithGS(ArrayList<NamedEntityCorrections> annotator_entity) throws GerbilException {
		ArrayList<NamedEntityCorrections> result_set = new ArrayList<NamedEntityCorrections>();
		final DatasetConfiguration DATASET = new NIFFileDatasetConfig("DBpedia",
				"C:/Users/Kunal/workspace/gerbil/Results_anontator_dbpedia/WAT-DBpediaSpotlight-s-A2KB.ttl", false,
				ExperimentType.A2KB);
		ArrayList<NamedEntityCorrections> gs_entity_set = new ArrayList<NamedEntityCorrections>();
		List<Document> documents = DATASET.getDataset(ExperimentType.A2KB).getInstances();
		for (Document doc : documents) {
			List<NamedEntityCorrections> entities = doc.getMarkings(NamedEntityCorrections.class);
			gs_entity_set.addAll(entities);

		}

		for (NamedEntityCorrections en : annotator_entity) {
			if (!(gs_entity_set).contains(en)) {
				en.setResult(Check.INSERTED);
				result_set.add(en);
			}
		}

		return result_set;
	}
}
