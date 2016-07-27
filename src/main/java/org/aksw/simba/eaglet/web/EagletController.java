package org.aksw.simba.eaglet.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.gerbil.io.nif.DocumentListParser;
import org.aksw.gerbil.io.nif.DocumentListWriter;
import org.aksw.gerbil.io.nif.DocumentParser;
import org.aksw.gerbil.io.nif.utils.NIFUriHelper;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.NIFTransferPrefixMapping;
import org.aksw.gerbil.transfer.nif.data.DocumentImpl;
import org.aksw.gerbil.transfer.nif.data.StartPosBasedComparator;
import org.aksw.simba.eaglet.annotator.AdaptedAnnotationParser;
import org.aksw.simba.eaglet.database.EagletDatabaseStatements;
import org.aksw.simba.eaglet.entitytypemodify.EntityCheck;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections.ErrorType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

@Controller
public class EagletController {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(EagletController.class);

	private static final String DATASET_FILES[] = new String[] { "eaglet_data/result_pipe/DBpediaSpotlight-result-nif.ttl" };

	// private static final String DATASET_FILES[] = new String[] {
	// "eaglet_data/result_pipe/kore50-nif-result-nif.ttl" };
	// private static final String DATASET_FILES[] = new String[] {
	// "eaglet_data/result_user/KORE50/mergedCorpus.ttl" };

	@Autowired
	private EagletDatabaseStatements database;

	private List<Document> documents;
	// TODO make me thread safe
	private int counter;

	public EagletController() {
		this.documents = loadDocuments();
		this.counter = 0;
	}

	public EagletController(List<Document> documents) {
		this.documents = documents;
	}

	public int getUser(String userName) {
		int userId;
		if (database.getUser(userName) == -1) {
			database.addUser(userName);
		}
		userId = database.getUser(userName);

		return userId;
	}

	@RequestMapping(value = "/next", produces = "application/json;charset=utf-8")
	public ResponseEntity<String> nextDocument(
			@RequestParam(value = "username") String userName) {
		LOGGER.info("Got a message to /next!");
		int userId = getUser(userName);

		// get the next document
		Document document = getNextDocument(userId);
		if (document == null) {
			// TODO return that this was the last document
			return new ResponseEntity<String>("redirect:thankyou.html", null,
					HttpStatus.OK);
		}
		// transform the document and its markings into a JSON String
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Content-Type", "application/json;charset=utf-8");
		return new ResponseEntity<String>(transformDocToJson(document),
				responseHeaders, HttpStatus.OK);
	}

	private Document getNextDocument(int userId) {
		Set<String> alreadySeenDocuments = new HashSet<String>(
				database.getDocumentUser(userId));
		for (Document document : documents) {
			if (!alreadySeenDocuments.contains(document.getDocumentURI())) {
				return document;
			}
		}
		return null;
	}

	private String transformDocToJson(Document document) {
		JSONObject doc = new JSONObject();
		doc.append("text", document.getText());
		doc.append("uri", document.getDocumentURI());
		JSONArray array = new JSONArray();
		JSONObject ne;
		List<NamedEntityCorrections> necs = document
				.getMarkings(NamedEntityCorrections.class);
		necs.sort(new StartPosBasedComparator());
		for (NamedEntityCorrections nec : necs) {
			ne = new JSONObject();
			ne.append("start", nec.getStartPosition());
			ne.append("length", nec.getLength());
			ne.append("partner", nec.getPartner());
			ne.append("result", nec.getResult());
			ne.append("doc", nec.getDoc());
			ne.append("uris", nec.getUris());
			ne.append(
					"name",
					document.getText()
							.substring(nec.getStartPosition(),
									nec.getStartPosition() + nec.getLength())
							.toUpperCase());
			ne.append("error", nec.getError());
			array.put(ne);
		}
		List<EntityCheck> ecs = document.getMarkings(EntityCheck.class);
		ecs.sort(new StartPosBasedComparator());
		for (EntityCheck nec : ecs) {
			ne = new JSONObject();
			ne.append("start", nec.getStartPosition());
			ne.append("length", nec.getLength());
			ne.append("uris", nec.getUris());
			ne.append(
					"name",
					document.getText()
							.substring(nec.getStartPosition(),
									nec.getStartPosition() + nec.getLength())
							.toUpperCase());
			array.put(ne);
		}
		doc.append("markings", array);
		return doc.toString();
	}

	private List<Marking> transformEntityFromJson(String userInput) {
		List<Marking> userAcceptedEntities = new ArrayList<Marking>();
		JSONArray markings = new JSONArray(userInput);

		for (int i = 0; i < markings.length(); i++) {
			Set<String> uris = new HashSet<String>();
			uris.add(markings.getJSONObject(i).getString("uri"));
			List<ErrorType> error = new ArrayList<ErrorType>();
			String errortype = markings.getJSONObject(i).getString("error");
			error.add(parseErroResult(errortype));
			Marking entity = new NamedEntityCorrections(markings.getJSONObject(
					i).getInt("start"), markings.getJSONObject(i).getInt(
					"length"), uris, error);
			userAcceptedEntities.add(entity);
		}
		return userAcceptedEntities;
	}

	@RequestMapping(value = "/submitResults", method = RequestMethod.POST)
	public String submitResults(
			@RequestParam(value = "documenturi") String document,
			@RequestParam(value = "markings") String userInput,
			@RequestParam(value = "username") String userName)
			throws IOException {
		int userId = getUser(userName);
		// TODO parse document from JSON
		List<Marking> changes = transformEntityFromJson(userInput);
		// TODO generate file name
		Document result = null;
		String filename = null;
		for (Document doc : documents) {
			if (doc.getDocumentURI().equals(document)) {
				result = doc;
			}

		}
		if (result != null) {
			String name = result.getDocumentURI().replaceAll("http://", "");
			name = name.replaceAll("/", "_");
			filename = "result-" + name + userName;
			filename = name.substring(0, 20);
			counter++;
		}

		Document newdoc = new DocumentImpl(result.getText(),
				result.getDocumentURI(), changes);
		// FileOutputStream fout = new
		// FileOutputStream("eaglet_data/result_user/" + filename + "-nif.ttl");
		// serialize the document into a file
		Model nifModel = generateModifiedModel(newdoc);

		File resultfile = new File("eaglet_data" + File.separator
				+ "result_user" + File.separator + userId + File.separator
				+ filename + "_" + counter + "-nif.ttl");
		if (!resultfile.exists()) {
			resultfile.getParentFile().mkdirs();
			resultfile.createNewFile();
		}
		FileOutputStream fout = new FileOutputStream(resultfile);
		fout.flush();
		nifModel.write(fout, "TTL");
		fout.close();
		// store the user ID - file name - document URI triple into the
		// database
		database.addDocument(userId, document, filename);
		return "redirect:next?username=" + userName;
	}

	public static Model generateModifiedModel(Document document) {
		Model nifModel = ModelFactory.createDefaultModel();
		nifModel.setNsPrefixes(NIFTransferPrefixMapping.getInstance());
		DocumentListWriter writer = new DocumentListWriter();
		writer.writeDocumentsToModel(nifModel, Arrays.asList(document));
		Resource annotationResource;
		for (EntityCheck correction : document.getMarkings(EntityCheck.class)) {
			annotationResource = nifModel.getResource(NIFUriHelper.getNifUri(
					document.getDocumentURI(), correction.getStartPosition(),
					correction.getStartPosition() + correction.getLength()));
			System.out.println(correction.getUris().toString() + " -> "
					+ correction.isCorrect());

			/*
			 * nifModel.add(annotationResource, EAGLET.isMarkedCorrect,
			 * EAGLET.isMarkedAdded, EAGLET.isMarkedMissing,
			 * EAGLET.isMarkedWrong,
			 * nifModel.createTypedLiteral(correction.isCorrect()),
			 * nifModel.createTypedLiteral(correction.isWrong()),
			 * nifModel.createTypedLiteral(correction.isAdded()),
			 * nifModel.createTypedLiteral(correction.isMissing()));
			 */
		}
		return nifModel;
	}

	protected static List<Document> loadDocuments() {
		List<Document> loadedDocuments = new ArrayList<Document>();
		DocumentListParser parser = new DocumentListParser(new DocumentParser(
				new AdaptedAnnotationParser()));
		for (int i = 0; i < DATASET_FILES.length; ++i) {
			try {
				Model nifModel = ModelFactory.createDefaultModel();
				nifModel.setNsPrefixes(NIFTransferPrefixMapping.getInstance());
				FileInputStream fin = new FileInputStream(new File(
						DATASET_FILES[i]));
				nifModel.read(fin, "", "TTL");
				fin.close();
				loadedDocuments.addAll(parser.parseDocuments(nifModel));
			} catch (Exception e) {
				LOGGER.error("Couldn't load the dataset!", e);
			}
		}
		return loadedDocuments;
	}

	private ErrorType parseErroResult(String errortype) {
		if (errortype.toUpperCase().equals("OVERLAPPING")) {
			return ErrorType.OVERLAPPING;
		} else if (errortype.toUpperCase().equals("COMBINED".toUpperCase())) {
			return ErrorType.COMBINED;
		} else if (errortype.toUpperCase().equals("ERRATIC")) {
			return ErrorType.ERRATIC;
		} else if (errortype.toUpperCase().equals("WRONG	")) {
			return ErrorType.WRONGPOSITION;
		} else if (errortype.toUpperCase().equals("LONGDESC")) {
			return ErrorType.LONGDESC;
		} else if (errortype.toUpperCase().equals("INVALIDURIERR")) {
			return ErrorType.INVALIDURIERR;
		} else if (errortype.toUpperCase().equals("DISAMBIGURIERR")) {
			return ErrorType.DISAMBIGURIERR;
		} else if (errortype.toUpperCase().equals("OUTDATEDURIERR")) {
			return ErrorType.OUTDATEDURIERR;

		} else {

			return null;
		}
	}
}
