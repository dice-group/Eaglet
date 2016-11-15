package org.aksw.simba.eaglet.web;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import org.aksw.gerbil.io.nif.DocumentListParser;
import org.aksw.gerbil.io.nif.DocumentListWriter;
import org.aksw.gerbil.io.nif.DocumentParser;
import org.aksw.gerbil.io.nif.utils.NIFTransferPrefixMapping;
import org.aksw.gerbil.io.nif.utils.NIFUriHelper;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.data.DocumentImpl;
import org.aksw.gerbil.transfer.nif.data.StartPosBasedComparator;
import org.aksw.simba.eaglet.annotator.AdaptedAnnotationParser;
import org.aksw.simba.eaglet.database.EagletDatabaseStatements;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections.DecisionValue;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections.ErrorType;
import org.aksw.simba.eaglet.vocab.EAGLET;
import org.apache.commons.io.IOUtils;
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

import java.io.*;
import java.util.*;

/**
 * The controller class for the EAGLET web service.
 *
 * @author Kunal
 * @author Michael R&ouml;der
 */
@Controller
public class EagletController {

	private static final Logger LOGGER = LoggerFactory.getLogger(EagletController.class);

	private static final String DATASET_FILES[] = new String[] { "/data1/Workspace/Eaglet/example.ttl" };

	private static final boolean USE_DOCUMENT_WHITELIST = true;
	private static final String WHITELIST_SOURCE_DIR = "eaglet_data/result_user/Result Kunal";

	@Autowired
	private EagletDatabaseStatements database;
	private DocumentListParser parser = new DocumentListParser(new DocumentParser(new AdaptedAnnotationParser()));
	private List<Document> documents;
	private int counter;

	/**
	 * Constructor
	 */
	public EagletController() {
		this.documents = loadDocuments();
		this.counter = 0;
	}

	/**
	 *
	 * Constructor
	 *
	 * @param documents
	 *            : List of documents
	 */

	public EagletController(List<Document> documents) {
		this.documents = documents;
	}

	/**
	 * The method to get the user from the database
	 *
	 * @param userName
	 * @return Userid
	 */
	public int getUser(String userName) {
		int userId;
		if (database.getUser(userName) == -1) {
			database.addUser(userName);
		}
		userId = database.getUser(userName);

		return userId;
	}

	/**
	 * The method handles the next set of documents based on User information.
	 *
	 * @param userName
	 * @return ResponseEntity
	 */
	@RequestMapping(value = "/next", produces = "application/json;charset=utf-8")
	public ResponseEntity<String> nextDocument(@RequestParam(value = "username") String userName) {
		LOGGER.info("Got a message to /next!");
		int userId = getUser(userName);

		// get the next document
		Document document = getNextDocument(userId);
		if (document == null) {
			// TODO return that this was the last document
			return new ResponseEntity<String>("redirect:thankyou.html", null, HttpStatus.OK);
		}
		// transform the document and its markings into a JSON String
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Content-Type", "application/json;charset=utf-8");
		return new ResponseEntity<String>(transformDocToJson(document), responseHeaders, HttpStatus.OK);
	}

	private Document getNextDocument(int userId) {
		Set<String> alreadySeenDocuments = new HashSet<String>(database.getDocumentUser(userId));
		for (Document document : documents) {
			if (!alreadySeenDocuments.contains(document.getDocumentURI())) {
				return document;
			}
		}
		return null;
	}

	/**
	 * The method transforms the document into Json for parsing in the
	 * webservice.
	 *
	 * @param document
	 * @return Json string
	 */

	private String transformDocToJson(Document document) {
		JSONObject doc = new JSONObject();
		doc.append("text", document.getText());
		doc.append("uri", document.getDocumentURI());
		JSONArray array = new JSONArray();
		JSONObject ne;
		List<NamedEntityCorrections> necs = document.getMarkings(NamedEntityCorrections.class);
		necs.sort(new StartPosBasedComparator());
		for (NamedEntityCorrections nec : necs) {
			ne = new JSONObject();
			ne.append("start", nec.getStartPosition());
			ne.append("length", nec.getLength());
			ne.append("partner", nec.getPartner());
			ne.append("result", nec.getResult());
			ne.append("doc", nec.getDoc());
			ne.append("uris", nec.getUris());
			ne.append("name",
					document.getText().substring(nec.getStartPosition(), nec.getStartPosition() + nec.getLength())
							.toUpperCase());
			ne.append("error", nec.getError().toString());
			array.put(ne);
		}
		doc.append("markings", array);
		return doc.toString();
	}

	/**
	 * The method transforms the JSon string returned by the user into a list of
	 * markings to be written into the NIF file.
	 *
	 * @param userInput
	 * @return List of Markings
	 */
	private List<Marking> transformEntityFromJson(String userInput) {
		List<Marking> userAcceptedEntities = new ArrayList<Marking>();
		JSONArray markings = new JSONArray(userInput);

		for (int i = 0; i < markings.length(); i++) {
			Set<String> uris = new HashSet<String>();
			uris.add(markings.getJSONObject(i).getString("uri").trim());
			List<ErrorType> error = new ArrayList<ErrorType>();
			String errortype = markings.getJSONObject(i).getString("error");
			error.add(parseErroResult(errortype));
			Marking entity = new NamedEntityCorrections(markings.getJSONObject(i).getInt("start"), markings
					.getJSONObject(i).getInt("length"), uris, error, parseDecisionType(markings.getJSONObject(i)
					.getString("decision")));
			userAcceptedEntities.add(entity);
		}
		return userAcceptedEntities;
	}

	/**
	 * The method handles the returning of Json string.
	 *
	 * @param document
	 * @param userInput
	 * @param userName
	 * @return Json String of entities.
	 * @throws IOException
	 */
	@RequestMapping(value = "/submitResults", method = RequestMethod.POST)
	public String submitResults(@RequestParam(value = "documenturi") String document,
			@RequestParam(value = "markings") String userInput, @RequestParam(value = "username") String userName)
			throws IOException {
		int userId = getUser(userName);
		List<Marking> changes = transformEntityFromJson(userInput);
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

		Document newdoc = new DocumentImpl(result.getText(), result.getDocumentURI(), changes);
		Model nifModel = generateModifiedModel(newdoc);

		File resultfile = new File("eaglet_data" + File.separator + "result_user" + File.separator + userId
				+ File.separator + filename + "_" + counter + "-nif.ttl");
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

	/**
	 * The method writes the output to the NIF file.
	 *
	 * @param document
	 * @return
	 */

	public static Model generateModifiedModel(Document document) {
		Model nifModel = ModelFactory.createDefaultModel();
		nifModel.setNsPrefixes(NIFTransferPrefixMapping.getInstance());
		DocumentListWriter writer = new DocumentListWriter();
		writer.writeDocumentsToModel(nifModel, Arrays.asList(document));
		Resource annotationResource;
		for (NamedEntityCorrections correction : document.getMarkings(NamedEntityCorrections.class)) {
			annotationResource = nifModel.getResource(NIFUriHelper.getNifUri(document.getDocumentURI(),
					correction.getStartPosition(), correction.getStartPosition() + correction.getLength()));
			System.out.println(correction.getUris().toString() + " -> " + correction.getUserDecision());
			nifModel.add(annotationResource, EAGLET.hasUserDecision,
					EAGLET.getUserDecision(correction.getUserDecision()));
		}
		return nifModel;
	}

	/**
	 * The method is responsible for returning list of documents based on
	 * userId.
	 *
	 * @return List of Documents
	 */
	public List<Document> loadDocuments() {
		List<Document> loadedDocuments = new ArrayList<Document>();
		List<Document> temp;
		for (int i = 0; i < DATASET_FILES.length; ++i) {
			temp = readDocuments(new File(DATASET_FILES[i]));
			if (temp != null) {
				loadedDocuments.addAll(temp);
			} else {
				LOGGER.error("Couldn't load the dataset!");
			}
		}

		if (USE_DOCUMENT_WHITELIST) {
			Set<String> whitelist = generateWhiteList();
			LOGGER.info("Whitelist contains {} document URIs.", whitelist.size());
			temp = new ArrayList<Document>();
			for (Document document : loadedDocuments) {
				if (whitelist.contains(document.getDocumentURI())) {
					temp.add(document);
				}
			}
			loadedDocuments = temp;
			LOGGER.info("There are {} documents matching the white list.", loadedDocuments.size());
		}
		return loadedDocuments;
	}

	private ErrorType parseErroResult(String errortype) {
		if (errortype.toUpperCase().equals("OVERLAPPING")) {
			return ErrorType.OVERLAPPING;
		} else if (errortype.toUpperCase().equals("COMBINED")) {
			return ErrorType.COMBINED;
		} else if (errortype.toUpperCase().equals("ERRATIC")) {
			return ErrorType.ERRATIC;
		} else if (errortype.toUpperCase().equals("WRONGPOSITION")) {
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

	private DecisionValue parseDecisionType(String errortype) {
		if (errortype.toUpperCase().equals("CORRECT")) {
			return DecisionValue.CORRECT;
		} else if (errortype.toUpperCase().equals("WRONG")) {
			return DecisionValue.WRONG;
		} else if (errortype.toUpperCase().equals("ADDED")) {
			return DecisionValue.ADDED;
		} else {

			return null;
		}
	}

	private Set<String> generateWhiteList() {
		Set<String> whitelist = new HashSet<String>();
		generateWhiteList(whitelist, new File(WHITELIST_SOURCE_DIR));
		return whitelist;
	}

	private void generateWhiteList(Set<String> whitelist, File file) {
		if (file.exists()) {
			if (file.isDirectory()) {
				for (File f : file.listFiles()) {
					generateWhiteList(whitelist, f);
				}
			} else {
				List<Document> documents = readDocuments(file);
				if (documents != null) {
					for (Document document : documents) {
						whitelist.add(document.getDocumentURI());
					}
				}
			}
		}
	}

	private List<Document> readDocuments(File file) {
		List<Document> documents = new ArrayList<Document>();
		FileInputStream fin = null;
		try {
			Model nifModel = ModelFactory.createDefaultModel();
			nifModel.setNsPrefixes(NIFTransferPrefixMapping.getInstance());
			fin = new FileInputStream(file);
			// BUG FIX NEEDED TO READ CORRUPTED FILES:
			String fileContent = IOUtils.toString(fin);
			fileContent = correctNIF(fileContent);
			nifModel.read(new StringReader(fileContent), "", "TTL");
			// nifModel.read(fin, "", "TTL");
			documents.addAll(parser.parseDocuments(nifModel));
		} catch (Exception e) {
			return null;
		} finally {
			IOUtils.closeQuietly(fin);
		}
		return documents;
	}

	public static final String correctNIF(String nif) {
		char[] chars = nif.toCharArray();
		StringBuilder builder = new StringBuilder(chars.length);
		boolean inUri = false;
		for (int i = 0; i < chars.length; ++i) {
			switch (chars[i]) {
			case '<':
				inUri = true;
				builder.append('<');
				break;
			case '>':
				inUri = false;
				builder.append('>');
				break;
			case ' ':
				if (!inUri) {
					builder.append(' ');
				}
				break;
			default:
				builder.append(chars[i]);
				break;
			}
		}
		return builder
				.toString()
				.replace("<null>", "<http://aksw.org/notInWiki/null>")
				.replace(
						"\"CORRECT\"^^<java:org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections$DecisionValue>",
						"<" + EAGLET.Correct.getURI() + ">")
				.replace(
						"\"ADDED\"^^<java:org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections$DecisionValue>",
						"<" + EAGLET.Added.getURI() + ">")
				.replace(
						"\"WRONG\"^^<java:org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections$DecisionValue>",
						"<" + EAGLET.Wrong.getURI() + ">");
	}

	public static void main(String[] args) {
		new EagletController();
	}
}
