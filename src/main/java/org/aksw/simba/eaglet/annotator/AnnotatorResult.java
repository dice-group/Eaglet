package org.aksw.simba.eaglet.annotator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.aksw.gerbil.annotator.A2KBAnnotator;
import org.aksw.gerbil.dataset.Dataset;
import org.aksw.gerbil.dataset.impl.nif.NIFFileDatasetConfig;
import org.aksw.gerbil.datatypes.ExperimentType;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections;

public class AnnotatorResult {

	private static final ExperimentType EXPERIMENT_TYPE = ExperimentType.A2KB;
	private List<A2KBAnnotator> annotators = new ArrayList<A2KBAnnotator>();
	String filename;

	public AnnotatorResult(String Filename) throws GerbilException {
		// TODO Auto-generated constructor stub
		this.filename = Filename;
		
		readannotatorlist();

	}

	public List<A2KBAnnotator> getAnnotators() {
		return annotators;
	}

	public void setAnnotators(List<A2KBAnnotator> annotators) {
		this.annotators = annotators;
	}

	public void readannotatorlist() throws GerbilException {
		File folder = new File(filename);
		File[] listOfFiles = folder.listFiles();
		for (File file : listOfFiles) {
			if (file.isFile()) {
				// String annotatorFilenName = file.getParent() + "/" +
				// file.getName();
				String annotatorFilenName = file.getAbsolutePath();
				//System.out.println(annotatorFilenName);
				annotators.add(AnnotatorResult.loadAnnotator(annotatorFilenName, file.getName()));
			}
		}
	}

	@Deprecated
	public static List<NamedEntityCorrections> loadAnnotatorResult(String annotatorFileName, String AnnotatorName)
			throws GerbilException {
		Dataset dataset = (new NIFFileDatasetConfig("ANNOTATOR", annotatorFileName, false, EXPERIMENT_TYPE))
				.getDataset(EXPERIMENT_TYPE);
		ArrayList<NamedEntityCorrections> entity_set = new ArrayList<NamedEntityCorrections>();
		List<Document> documents = dataset.getInstances();
		A2KBAnnotator alias_annotator = new TestA2KBAnnotator(documents);
		// System.out.println(documents.get(0).getDocumentURI());
		for (Document doc : documents) {
			List<NamedEntityCorrections> entities = doc.getMarkings(NamedEntityCorrections.class);
			entity_set.addAll(entities);
		}
		return entity_set;
	}

	public static A2KBAnnotator loadAnnotator(String annotatorFileName, String AnnotatorName) throws GerbilException {
		Dataset dataset = (new NIFFileDatasetConfig("ANNOTATOR", annotatorFileName, false, EXPERIMENT_TYPE))
				.getDataset(EXPERIMENT_TYPE);
		List<Document> documents = dataset.getInstances();
		return new TestA2KBAnnotator(documents);
	}

}