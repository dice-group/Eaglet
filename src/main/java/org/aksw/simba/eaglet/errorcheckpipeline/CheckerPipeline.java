package org.aksw.simba.eaglet.errorcheckpipeline;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.aksw.gerbil.annotator.A2KBAnnotator;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.io.nif.DocumentListParser;
import org.aksw.gerbil.io.nif.DocumentListWriter;
import org.aksw.gerbil.io.nif.DocumentParser;
import org.aksw.gerbil.io.nif.utils.NIFUriHelper;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.NIFTransferPrefixMapping;
import org.aksw.simba.eaglet.annotator.AdaptedAnnotationParser;
import org.aksw.simba.eaglet.completion.GoldStandardCompletion;
import org.aksw.simba.eaglet.completion.MissingEntityCompletion;
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
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class CheckerPipeline {

	public static List<A2KBAnnotator> callAnnotator(String path) throws GerbilException {
		AnnotatorResult ar = new AnnotatorResult(path);
		List<A2KBAnnotator> annotators = ar.getAnnotators();
		return annotators;
	}

	public static void startPipe(List<Document> documents) throws GerbilException, IOException {
		List<A2KBAnnotator> annotators = callAnnotator("eaglet/Results_anontator_dbpedia");

		GoldStandardCompletion Complete = new MissingEntityCompletion(annotators);

		// prepare the pipeline

		List<ErrorChecker> checkers = new ArrayList<ErrorChecker>();
		checkers.add(new CombinedTaggingError());
		checkers.add(new OverLappingError());
		checkers.add(new LongDescriptionError());
		checkers.add(new OverLappingError());
		checkers.add(new SubsetMarkingError());

		// TODO: Switch Case for both modules

		// start pipeline

		for (ErrorChecker checker : checkers) {
			checker.check(documents);
		}

		// write documents
		Model nifModel = generateModel(documents);
		FileOutputStream fout = new FileOutputStream(
				"eaglet_data/result_pipe/sample-result-nif.ttl");
		nifModel.write(fout, "TTL");
		fout.close();
		// TODO: Send to server
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

	public static List<Document> readDocuments(String fileName) throws IOException {
		// Read the RDF MOdel
		Model nifModel = ModelFactory.createDefaultModel();
		nifModel.setNsPrefixes(NIFTransferPrefixMapping.getInstance());
		FileInputStream fin = new FileInputStream(new File(fileName));
		nifModel.read(fin, "", "TTL");
		fin.close();

		DocumentListParser parser = new DocumentListParser(new DocumentParser(new AdaptedAnnotationParser()));
		List<Document> documents = parser.parseDocuments(nifModel);
		StmtIterator iterator = nifModel.listStatements(null, EAGLET.hasPairPartner, (RDFNode) null);
		Statement s;
		String subjectUri, objectUri, documentUri;
		NamedEntityCorrections subject = null, object = null;
		while (iterator.hasNext()) {
			s = iterator.next();
			subjectUri = s.getSubject().getURI();
			objectUri = s.getObject().asResource().getURI();
			documentUri = NIFUriHelper.getDocumentUriFromNifUri(subjectUri);
			// TODO search the document from the list
			for (Document doc : documents) {
				if (documentUri.equals(doc.getDocumentURI())) {
					int startsbj = Character.getNumericValue(subjectUri.charAt(subjectUri.length() - 3));
					int endsbj = Character.getNumericValue(subjectUri.charAt(subjectUri.length() - 1));
					int startobj = Character.getNumericValue(objectUri.charAt(subjectUri.length() - 3));
					int endobj = Character.getNumericValue(objectUri.charAt(subjectUri.length() - 1));

					List<NamedEntityCorrections> entity_set = doc.getMarkings(NamedEntityCorrections.class);
					for (NamedEntityCorrections entity : entity_set) {
						if ((entity.getStartPosition() == startsbj)
								&& (entity.getStartPosition() + entity.getLength() == endsbj)) 
						{
							subject=entity;
						}
						if ((entity.getStartPosition() == startobj)
								&& (entity.getStartPosition() + entity.getLength() == endobj)) 
						{
							object=entity;
						}
					}
					subject.setPartner(object);

				}
			}
			
		}
		return documents;
	}
}
