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
import org.aksw.gscheck.errorutils.AnnotatorResult;

public class MissingEntityCompletion {
	public static void main(String[] args) throws GerbilException {
		File folder = new File("C:/Users/Kunal/workspace/gerbil/Results_anontator_dbpedia");
		File[] listOfFiles = folder.listFiles();
		AnnotatorResult Ar = new AnnotatorResult();
		for (File file : listOfFiles) {
			if (file.isFile()) {
				String annotatorFilenName = file.getParent() + "/" + file.getName();
				// System.out.println(annotatorFilenName);
				System.out.println(file.getName());
				ArrayList<NamedEntity> result_annotator = AnnotatorResult.loadAnnotator(annotatorFilenName,
						file.getName());
				ArrayList<NamedEntity> result_set = CompareWithGS(result_annotator);

				AnnotatorResult.printlist(result_set);

			}

		}

	}

	public static ArrayList<NamedEntity> CompareWithGS(ArrayList<NamedEntity> annotator_entity) throws GerbilException {
		ArrayList<NamedEntity> result_set = new ArrayList<NamedEntity>();
		final DatasetConfiguration DATASET = new NIFFileDatasetConfig("DBpedia",
				"C:/Users/Kunal/workspace/gerbil/Results_anontator_dbpedia/WAT-DBpediaSpotlight-s-A2KB.ttl", false,
				ExperimentType.A2KB);
		ArrayList<NamedEntity> gs_entity_set = new ArrayList<NamedEntity>();
		List<Document> documents = DATASET.getDataset(ExperimentType.A2KB).getInstances();
		for (Document doc : documents) {
			List<NamedEntity> entities = doc.getMarkings(NamedEntity.class);
			gs_entity_set.addAll(entities);

		}

		for (NamedEntity en : annotator_entity) {
			if (!(gs_entity_set).contains(en)) {
				result_set.add(en);
			}
		}

		return result_set;
	}
}
