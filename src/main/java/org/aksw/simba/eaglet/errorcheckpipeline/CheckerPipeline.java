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
import org.aksw.simba.eaglet.annotator.AnnotatorResult;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections;
import org.aksw.simba.eaglet.error.CombinedTaggingError;
import org.aksw.simba.eaglet.error.ErraticMarkingError;
import org.aksw.simba.eaglet.error.ErrorChecker;
import org.aksw.simba.eaglet.error.LongDescriptionError;
import org.aksw.simba.eaglet.error.OverLappingError;
import org.aksw.simba.eaglet.error.SubsetMarkingError;
import org.aksw.simba.eaglet.error.UriError;
import org.aksw.simba.eaglet.vocab.EAGLET;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * The class defines the pipeline structure and functionalities.
 *
 * @author Kunal
 * @author Michael
 */
public class CheckerPipeline {
	/** Value - {@value} , LOGGER used for log information. */
	private static final org.slf4j.Logger LOGGER = LoggerFactory
			.getLogger(CheckerPipeline.class);

	/**
	 * This method call all the annotators results. Utility method for pipeline.
	 *
	 * @param path
	 * @return List of A2KB Annotators Object.
	 * @throws GerbilException
	 */
	public List<A2KBAnnotator> callAnnotator(String path)
			throws GerbilException {
		AnnotatorResult ar = new AnnotatorResult(path);
		List<A2KBAnnotator> annotators = ar.getAnnotators();
		return annotators;
	}

	/**
	 * This method defines the structure of the pipe and starts the error
	 * checker pipeline.
	 *
	 * @param documents
	 * @param name
	 * @throws GerbilException
	 * @throws IOException
	 */
	public void startPipe(List<Document> documents, String name)
			throws GerbilException, IOException {
		// prepare the pipeline
		List<ErrorChecker> checkers = new ArrayList<ErrorChecker>();
		// checkers.add(new MissingEntityCompletion(annotators));
		checkers.add(new LongDescriptionError());
		checkers.add(new SubsetMarkingError());
		checkers.add(new ErraticMarkingError());
		checkers.add(new OverLappingError());
		checkers.add(new CombinedTaggingError());
		checkers.add(new UriError());
		// start pipeline
		for (ErrorChecker checker : checkers) {
			checker.check(documents);
		}
		Model nifModel = generateModel(documents);
		File resultfile = new File("eaglet_data" + File.separator
				+ "result_pipe" + File.separator + name + "-result-nif.ttl");
		if (!resultfile.exists()) {
			resultfile.getParentFile().mkdirs();
			resultfile.createNewFile();
		}
		FileOutputStream fout = new FileOutputStream(resultfile);
		fout.flush();
		nifModel.write(fout, "TTL");
		fout.close();
		LOGGER.info("PIPELINE RESULTS GENERATED");
	}

	/**
	 * This method generates the NIF model for writing to the annotation file.
	 *
	 * @param documents
	 * @return NIF Model
	 */
	public static Model generateModel(List<Document> documents) {
		Model nifModel = ModelFactory.createDefaultModel();
		nifModel.setNsPrefixes(NIFTransferPrefixMapping.getInstance());
		DocumentListWriter writer = new DocumentListWriter();
		writer.writeDocumentsToModel(nifModel, documents);
		Resource annotationResource;
		NamedEntityCorrections partner;
		for (Document document : documents) {
			for (NamedEntityCorrections correction : document
					.getMarkings(NamedEntityCorrections.class)) {
				annotationResource = nifModel.getResource(NIFUriHelper
						.getNifUri(
								document.getDocumentURI(),
								correction.getStartPosition(),
								correction.getStartPosition()
										+ correction.getLength()));
				nifModel.add(annotationResource, EAGLET.hasCheckResult,
						EAGLET.getCheckResult(correction.getResult()));
				nifModel.add(annotationResource, EAGLET.hasErrorType,
						EAGLET.getCheckResult(correction.getResult()));

				partner = correction.getPartner();
				if ((partner != null)) {
					nifModel.add(annotationResource, EAGLET.hasPairPartner,
							nifModel.getResource(NIFUriHelper.getNifUri(
									document.getDocumentURI(),
									partner.getStartPosition(),
									partner.getStartPosition()
											+ partner.getLength())));
				}
			}
		}
		return nifModel;
	}

	/**
	 * This method reads the documents from the TTL files.
	 *
	 * @param path
	 * @return List of Documents
	 * @throws IOException
	 */
	public static List<Document> readDocuments(String path) throws IOException {
		// Read the RDF MOdel
		Model nifModel = ModelFactory.createDefaultModel();
		nifModel.setNsPrefixes(NIFTransferPrefixMapping.getInstance());
		FileInputStream fin = new FileInputStream(new File(path));
		nifModel.read(fin, "", "TTL");
		fin.close();

		DocumentListParser parser = new DocumentListParser(new DocumentParser(
				new AdaptedAnnotationParser()));
		List<Document> documents = parser.parseDocuments(nifModel);
		StmtIterator iterator = nifModel.listStatements(null,
				EAGLET.hasPairPartner, (RDFNode) null);
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
					int startsbj = Character.getNumericValue(subjectUri
							.charAt(subjectUri.length() - 3));
					int endsbj = Character.getNumericValue(subjectUri
							.charAt(subjectUri.length() - 1));
					int startobj = Character.getNumericValue(objectUri
							.charAt(subjectUri.length() - 3));
					int endobj = Character.getNumericValue(objectUri
							.charAt(subjectUri.length() - 1));
					List<NamedEntityCorrections> entity_set = doc
							.getMarkings(NamedEntityCorrections.class);
					for (NamedEntityCorrections entity : entity_set) {
						if ((entity.getStartPosition() == startsbj)
								&& (entity.getStartPosition()
										+ entity.getLength() == endsbj)) {
							subject = entity;
						}
						if ((entity.getStartPosition() == startobj)
								&& (entity.getStartPosition()
										+ entity.getLength() == endobj)) {
							object = entity;
						}
					}
					subject.setPartner(object);

				}
			}

		}
		return documents;
	}
}
