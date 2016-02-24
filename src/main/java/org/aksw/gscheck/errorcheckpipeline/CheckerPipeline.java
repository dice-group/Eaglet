package org.aksw.gscheck.errorcheckpipeline;

import java.util.List;

import org.aksw.gerbil.dataset.DatasetConfiguration;
import org.aksw.gerbil.dataset.impl.nif.NIFFileDatasetConfig;
import org.aksw.gerbil.datatypes.ExperimentType;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.data.NamedEntity;
import org.aksw.gscheck.corrections.EntityTypeChange;
import org.aksw.gscheck.corrections.NamedEntityCorrections;
import org.aksw.gscheck.error.CombinedTaggingError;
import org.aksw.gscheck.error.ErraticEntityError;
import org.aksw.gscheck.error.LongDescriptionError;
import org.aksw.gscheck.error.OverLappingError;
import org.aksw.gscheck.error.SubsetMarkingError;

public class CheckerPipeline {

	private static final DatasetConfiguration DATASET = new NIFFileDatasetConfig("DBpedia",
			"gerbil_data/datasets/spotlight/dbpedia-spotlight-nif.ttl", false, ExperimentType.A2KB);

	public static void PiPeStructure() throws GerbilException {
		CombinedTaggingError .CombinedTagger();
		ErraticEntityError.ErraticEntityProb();
		LongDescriptionError.LongDescription();
		OverLappingError.overlapcheck();
		SubsetMarkingError.subsetmark();
	}

	public void PreProcessor() throws GerbilException {
		List<Document> documents = DATASET.getDataset(ExperimentType.A2KB).getInstances();

		for (Document doc : documents) {
			List<Marking> list =EntityTypeChange.changeType(doc);
			doc.setMarkings(list);
		}
		
		

	}

	public static void main(String[] args) throws GerbilException {
		//PiPeStructure();
	}

}
