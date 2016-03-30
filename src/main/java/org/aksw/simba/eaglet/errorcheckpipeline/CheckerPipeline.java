package org.aksw.simba.eaglet.errorcheckpipeline;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.aksw.gerbil.annotator.A2KBAnnotator;
import org.aksw.gerbil.dataset.DatasetConfiguration;
import org.aksw.gerbil.dataset.impl.nif.NIFFileDatasetConfig;
import org.aksw.gerbil.datatypes.ExperimentType;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.io.nif.DocumentListWriter;
import org.aksw.gerbil.io.nif.utils.NIFUriHelper;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.MeaningSpan;
import org.aksw.gerbil.transfer.nif.NIFTransferPrefixMapping;
import org.aksw.simba.eaglet.completion.GoldStandardCompletion;
import org.aksw.simba.eaglet.completion.MissingEntityCompletion;
import org.aksw.simba.eaglet.documentprocessor.DocumentProcessor;
import org.aksw.simba.eaglet.entitytypemodify.EntityTypeChange;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections;
import org.aksw.simba.eaglet.error.CombinedTaggingError;
import org.aksw.simba.eaglet.error.ErrorChecker;
import org.aksw.simba.eaglet.error.LongDescriptionError;
import org.aksw.simba.eaglet.error.OverLappingError;
import org.aksw.simba.eaglet.error.SubsetMarkingError;
import org.aksw.simba.eaglet.errorutils.AnnotatorResult;
import org.aksw.simba.eaglet.vocab.EAGLET;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import com.hp.hpl.jena.rdf.model.Resource;

public class CheckerPipeline {

	private static final DatasetConfiguration DATASET = new NIFFileDatasetConfig("DBpedia",
			"gerbil_data/datasets/spotlight/dbpedia-spotlight-nif.ttl", false, ExperimentType.A2KB);

	public void PrePipeProcessor() throws GerbilException {
		List<Document> documents = DATASET.getDataset(ExperimentType.A2KB).getInstances();

		for (Document doc : documents) {
			List<Marking> list = EntityTypeChange.changeType(doc);
			doc.setMarkings(list);
		}

	}

	public static void main(String[] args) throws GerbilException, IOException {
		// TODO init pipeline
		AnnotatorResult ar= new AnnotatorResult("C:/Users/Kunal/workspace/gerbil/Results_anontator_dbpedia");
		List<A2KBAnnotator> annotators = ar.getAnnotators();
		GoldStandardCompletion  Complete=  new MissingEntityCompletion(annotators);
		List<ErrorChecker> checkers = new ArrayList<ErrorChecker>();
		checkers.add(new CombinedTaggingError());
		checkers.add(new OverLappingError());
		checkers.add(new LongDescriptionError());
		checkers.add(new OverLappingError());
		checkers.add(new SubsetMarkingError());
		// PiPeStructure();
		
		//TODO: Switch Case for both modules
		
		List<Document> documents = DATASET.getDataset(ExperimentType.A2KB).getInstances();
		for (Document doc : documents) {
			List<Marking> list = EntityTypeChange.changeType(doc);
			doc.setMarkings(list);
		}
		// preprocess documents
		DocumentProcessor dp = new DocumentProcessor();
		dp.process(documents);
		// start pipeline

		for (ErrorChecker checker : checkers) {
			checker.check(documents);
		}

		// write documents
		Model nifModel = generateModel(documents);
		FileOutputStream fout = new FileOutputStream(
				"C:/Users/Kunal/workspace/gs_check/gerbil_data/datasets/spotlight/dbpedia-spotlight-nif.ttl");
		nifModel.write(fout, "TTL");
		fout.close();
	}

	public static Model generateModel(List<Document> documents) {
		Model nifModel = ModelFactory.createDefaultModel();
		nifModel.setNsPrefixes(NIFTransferPrefixMapping.getInstance());
		DocumentListWriter writer = new DocumentListWriter();
		writer.writeDocumentsToModel(nifModel, documents);
		Resource annotationResource;
		NamedEntityCorrections partner;
		for (Document document : documents) {
			for (NamedEntityCorrections correction : document.getMarkings(NamedEntityCorrections.class)) {
				annotationResource = nifModel.getResource(NIFUriHelper.getNifUri(document.getDocumentURI(),
						correction.getStartPosition(), correction.getStartPosition() + correction.getLength()));
				nifModel.add(annotationResource, EAGLET.hasCheckResult, EAGLET.getCheckResult(correction.getResult()));
				partner = correction.getPartner();
				if ((partner != null)) {

					nifModel.add(annotationResource, EAGLET.hasPairPartner,
							nifModel.getResource(NIFUriHelper.getNifUri(document.getDocumentURI(),
									partner.getStartPosition(), partner.getStartPosition() + partner.getLength())));

				}
			}
		}
		return nifModel;
	}

}
