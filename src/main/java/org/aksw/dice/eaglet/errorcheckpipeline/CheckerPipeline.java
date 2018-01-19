package org.aksw.dice.eaglet.errorcheckpipeline;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.aksw.dice.eaglet.annotator.AdaptedAnnotationParser;
import org.aksw.dice.eaglet.annotator.AnnotatorResult;
import org.aksw.dice.eaglet.completion.MissingEntityCompletion;
import org.aksw.dice.eaglet.config.EAGLETConfig;
import org.aksw.dice.eaglet.entitytypemodify.NamedEntityCorrections;
import org.aksw.dice.eaglet.entitytypemodify.NamedEntityCorrections.ErrorType;
import org.aksw.dice.eaglet.error.CombinedTaggingError;
import org.aksw.dice.eaglet.error.InconsistentMarkingError;
import org.aksw.dice.eaglet.error.ErrorChecker;
import org.aksw.dice.eaglet.error.LongDescriptionError;
import org.aksw.dice.eaglet.error.OverLappingError;
import org.aksw.dice.eaglet.error.PositioningError;
import org.aksw.dice.eaglet.error.UriError;
import org.aksw.dice.eaglet.vocab.EAGLET;
import org.aksw.gerbil.annotator.A2KBAnnotator;
import org.aksw.gerbil.annotator.AnnotatorConfiguration;
import org.aksw.gerbil.config.GerbilConfiguration;
import org.aksw.gerbil.datatypes.ExperimentType;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.io.nif.DocumentListParser;
import org.aksw.gerbil.io.nif.DocumentListWriter;
import org.aksw.gerbil.io.nif.DocumentParser;
import org.aksw.gerbil.io.nif.utils.NIFTransferPrefixMapping;
import org.aksw.gerbil.io.nif.utils.NIFUriHelper;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.web.config.AnnotatorsConfig;
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
	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(CheckerPipeline.class);

	public static final String PIPE_CONFIG_KEY = "org.aksw.dice.eaglet.errorcheckpipeline.CheckerPipeline.pipe";
	public static final String POST_USER_PIPE_CONFIG_KEY = "org.aksw.dice.eaglet.errorcheckpipeline.CheckerPipeline.postUserPipe";
	public static final String ANNOTATORS_LIST_KEY = "org.aksw.dice.eaglet.errorcheckpipeline.CheckerPipeline.annotators";

	/**
	 * This method call all the annotators results. Utility method for pipeline.
	 *
	 * @param path
	 * @return List of A2KB Annotators Object.
	 * @throws GerbilException
	 * 
	 * @deprecated Relies on annotation result files that are not very likely to be
	 *             available. {@link #loadAnnotators()} is used instead.
	 */
	@Deprecated
	public List<A2KBAnnotator> callAnnotator(String path) throws GerbilException {
		AnnotatorResult ar = new AnnotatorResult(path);
		List<A2KBAnnotator> annotators = ar.getAnnotators();
		return annotators;
	}

	/**
	 * This method sets up the basic structure of the pipe. PIPE STRUCTURE 1
	 *
	 *
	 * @return List of ErrorChecker Object.
	 * @throws GerbilException
	 */

	public List<ErrorChecker> setupPipe() throws GerbilException {
		return createPipe(EAGLETConfig.getInstance().getStringArray(PIPE_CONFIG_KEY));
	}

	@SuppressWarnings("unchecked")
	private List<ErrorChecker> createPipe(String[] pipeConfig) {
		if (pipeConfig == null) {
			LOGGER.warn("Got an empty pipe configuration.");
			return Collections.EMPTY_LIST;
		}
		List<ErrorChecker> checkers = new ArrayList<ErrorChecker>();
		for (int i = 0; i < pipeConfig.length; i++) {
			switch (pipeConfig[i]) {
			case "MissingEntityCompletion": {
				List<A2KBAnnotator> annotators = loadAnnotators();
				if (annotators.isEmpty()) {
					LOGGER.error(
							"Couldn't add MissingEntityCompletion modul cause the list of annotation system is empty.");
				} else {
					checkers.add(new MissingEntityCompletion(annotators));
				}
				break;
			}
			case "LongDescriptionError": {
				checkers.add(new LongDescriptionError());
				break;
			}
			case "PositioningError": {
				checkers.add(new PositioningError());
				break;
			}
			case "OverLappingError": {
				checkers.add(new OverLappingError());
				break;
			}
			case "CombinedTaggingError": {
				checkers.add(new CombinedTaggingError());
				break;
			}
			case "UriError": {
				checkers.add(new UriError());
				break;
			}
			case "ErraticMarkingError": {
				checkers.add(new InconsistentMarkingError());
				break;
			}
			default: {
				LOGGER.error("Got an unknown pipeline element as part of the configuration \"{}\". It will be ignored.",
						pipeConfig[i]);
				break;
			}
			}
		}

		return checkers;
	}

	@SuppressWarnings("unchecked")
	private List<A2KBAnnotator> loadAnnotators() {
		String annotatorNames[] = GerbilConfiguration.getInstance().getStringArray(ANNOTATORS_LIST_KEY);
		if (annotatorNames == null) {
			LOGGER.warn("Got an empty list of annotation sysetms.");
			return Collections.EMPTY_LIST;
		}
		List<A2KBAnnotator> annotators = new ArrayList<A2KBAnnotator>();
		List<AnnotatorConfiguration> availableAnnotators = AnnotatorsConfig.annotators()
				.getAdaptersForExperiment(ExperimentType.A2KB);
		for (int i = 0; i < annotatorNames.length; i++) {
			for (AnnotatorConfiguration annoConfig : availableAnnotators) {
				if (annoConfig.getName().equals(annotatorNames[i])) {
					try {
						annotators.add((A2KBAnnotator) annoConfig.getAnnotator(ExperimentType.A2KB));
					} catch (GerbilException e) {
						LOGGER.error("Couldn't create annotator \"" + annotatorNames[i] + "\". It won't be available.",
								e);
					}
					// jump out of the inner loop since we have found our annotator
					break;
				}
			}
		}

		return annotators;
	}

	/**
	 * This method sets up the portion of the pipe after the user evaluation. PIPE
	 * STRUCTURE 2
	 *
	 *
	 * @return List of ErrorChecker Object.
	 * @throws GerbilException
	 */
	public List<ErrorChecker> setupPostUserEvaluationPipe() throws GerbilException {
		return createPipe(GerbilConfiguration.getInstance().getStringArray(POST_USER_PIPE_CONFIG_KEY));
	}

	/**
	 * This method takes care of running the Pipe. It calls the other required
	 * function and begins the pipe and writes the output to a file.
	 *
	 * @param documents
	 * @throws GerbilException
	 * @throws IOException
	 */
	public void runPipe(List<Document> documents, String name) throws GerbilException, IOException {
		List<ErrorChecker> pipe = null;
		pipe = this.setupPipe();
		try {

			for (ErrorChecker checker : pipe) {
				checker.check(documents);
			}
		} catch (Exception e) {
			LOGGER.error("Got an exception while running the pipe.", e);
		}
		// start pipeline

		this.writeDataInFile(documents, name);
	}

	/**
	 * This method takes care of running the Pipe. It calls the other required
	 * function and begins the pipe and writes the output to a file.
	 *
	 * @param documents
	 * @throws GerbilException
	 * @throws IOException
	 */
	public void runPipeAfterEval(List<Document> documents, String outPath) throws GerbilException, IOException {
		List<ErrorChecker> pipe = null;
		pipe = this.setupPostUserEvaluationPipe();
		try {

			for (ErrorChecker checker : pipe) {
				checker.check(documents);
			}
		} catch (Exception e) {
			LOGGER.error("Got an exception while running the pipe.", e);
		}
		// start pipeline

		this.writeFinalOuput(documents, outPath);
	}

	/**
	 * This method writes the data into the file once the documents are processed by
	 * the pipeline after user eval.
	 *
	 * @param documents
	 * @param name
	 * @throws GerbilException
	 * @throws IOException
	 */

	public void writeFinalOuput(List<Document> documents, String outPath) throws GerbilException, IOException {
		Model nifModel = generateModel(documents);
		File resultfile = new File(outPath);
		if (!resultfile.exists()) {
			resultfile.getParentFile().mkdirs();
			resultfile.createNewFile();
		}
		FileOutputStream fout = new FileOutputStream(resultfile);
		fout.flush();
		nifModel.write(fout, "TTL");
		fout.close();
		LOGGER.info("Final Results generated");
	}

	/**
	 * This method writes the data into the file once the documents are processed by
	 * the pipeline.
	 *
	 * @param documents
	 * @param name
	 * @throws GerbilException
	 * @throws IOException
	 */

	public void writeDataInFile(List<Document> documents, String name) throws GerbilException, IOException {
		Model nifModel = generateModel(documents);
		File resultfile = new File(
				"eaglet_data" + File.separator + "result_pipe" + File.separator + name + "-result.ttl");
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
			for (NamedEntityCorrections correction : document.getMarkings(NamedEntityCorrections.class)) {
				annotationResource = nifModel.getResource(NIFUriHelper.getNifUri(document.getDocumentURI(),
						correction.getStartPosition(), correction.getStartPosition() + correction.getLength()));
				nifModel.add(annotationResource, EAGLET.hasCheckResult,
						EAGLET.getCorrectionSuggested(correction.getCorrectionSuggested()));
				nifModel.add(annotationResource, EAGLET.hasErrorType, EAGLET.getErrorType(correction.getError()));
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
								&& (entity.getStartPosition() + entity.getLength() == endsbj)) {
							subject = entity;
						}
						if ((entity.getStartPosition() == startobj)
								&& (entity.getStartPosition() + entity.getLength() == endobj)) {
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
