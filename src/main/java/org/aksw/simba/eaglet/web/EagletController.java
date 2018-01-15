package org.aksw.simba.eaglet.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.io.nif.DocumentListParser;
import org.aksw.gerbil.io.nif.DocumentListWriter;
import org.aksw.gerbil.io.nif.DocumentParser;
import org.aksw.gerbil.io.nif.utils.NIFTransferPrefixMapping;
import org.aksw.gerbil.io.nif.utils.NIFUriHelper;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.data.StartPosBasedComparator;
import org.aksw.simba.eaglet.annotator.AdaptedAnnotationParser;
import org.aksw.simba.eaglet.database.EagletDatabaseStatements;
import org.aksw.simba.eaglet.documentprocessor.DocumentProcessor;
import org.aksw.simba.eaglet.documentprocessor.StanfordParsedMarking;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections.DecisionValue;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections.ErrorType;
import org.aksw.simba.eaglet.error.ErraticMarkingUserInput;
import org.aksw.simba.eaglet.errorcheckpipeline.InputforPipeline;
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

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * The controller class for the EAGLET web service.
 *
 * @author Kunal
 * @author Michael R&ouml;der
 */
@Controller
public class EagletController {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(EagletController.class);
	private static final String DEFAULT_URI = "./eaglet_data/result_pipe/OKE 2015 Task 1 gold standard sample-result-nif.ttl";

	String DATASET_FILES[] = new String[] { "OKE 2015", DEFAULT_URI };
	boolean DATASET_GIVEN;

	@Autowired
	private EagletDatabaseStatements database;
	private DocumentListParser parser = new DocumentListParser(
			new DocumentParser(new AdaptedAnnotationParser()));
	private List<Document> documents;
	private List<Document> remainingDocuments;
	private int counter;
	private ErraticMarkingUserInput er;

	/**
	 * Constructor
	 */
	public EagletController() {
		DATASET_GIVEN = false;
		this.documents = loadDocuments();
		this.counter = 0;
		this.remainingDocuments = new ArrayList<Document>();
		er = new ErraticMarkingUserInput();

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
	public ResponseEntity<String> nextDocument(
			@RequestParam(value = "username") String userName) {
		LOGGER.info("Got a message to /next!");

		int userId = getUser(userName);
		// get the next document
		Document document = getNextDocument(userId);
		if (document == null) {
			try {
				// CHECKING AFTER EVALUATION
				this.recheckUserInput();
			} catch (GerbilException e) {
				e.printStackTrace();
			} catch (IOException e) {
				LOGGER.error("Problem with rechecking pipeline");
				e.printStackTrace();
			}
			LOGGER.info("Redirecting to Thankyou");
			return new ResponseEntity<String>("thankyou.html", null,
					HttpStatus.OK);
		}
		// transform the document and its markings into a JSON String
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Content-Type", "application/json;charset=utf-8");
		return new ResponseEntity<String>(transformDocToJson(document),
				responseHeaders, HttpStatus.OK);
	}

	@RequestMapping(value = "/pipe", produces = "application/json;charset=utf-8")
	public ResponseEntity<String> runPipe(
			@RequestParam(value = "datasetname") String datasetName,
			@RequestParam(value = "path") String datasetPath) {
		LOGGER.info("Got a message to pipe!");
		try {
			new InputforPipeline(datasetName, datasetPath);

		} catch (GerbilException e) {

			e.printStackTrace();
			return new ResponseEntity<String>(null, null,
					HttpStatus.BAD_REQUEST);
		} catch (IOException e) {
			e.printStackTrace();
			return new ResponseEntity<String>(null, null,
					HttpStatus.BAD_REQUEST);

		}

		DATASET_FILES[0] = datasetName;

		DATASET_FILES[1] = "eaglet_data" + File.separator + "result_pipe"
				+ File.separator + datasetName + "-result-nif.ttl";
		this.DATASET_GIVEN = true;
		this.documents.clear();
		this.documents = loadDocuments();
		// transform the document and its markings into a JSON String
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Content-Type", "application/json;charset=utf-8");
		return new ResponseEntity<String>("Done", responseHeaders,
				HttpStatus.OK);
	}

	private Document getNextDocument(int userId) {

		LOGGER.info("Updated all other documents for the previous markings");
		Set<String> alreadySeenDocuments = new HashSet<String>(
				database.getDocumentUser(userId));

		this.remainingDocuments.clear();

		for (Document document : documents) {
			if (!alreadySeenDocuments.contains(document.getDocumentURI())) {
				remainingDocuments.add(document);
			}
		}

		if (remainingDocuments.isEmpty())
			return null;
		else
			return remainingDocuments.get(0);
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
			Marking entity = new NamedEntityCorrections(markings.getJSONObject(
					i).getInt("start"), markings.getJSONObject(i).getInt(
					"length"), uris, error, parseDecisionType(markings
					.getJSONObject(i).getString("decision")));
			userAcceptedEntities.add(entity);
		}
		return userAcceptedEntities;
	}

	public void updateDocumentList(List<Document> remList) {
		for (Document doc : remList) {
			for (Document doc1 : this.documents) {
				if (doc1.getDocumentURI().equals(doc.getDocumentURI())) {
					doc1.setMarkings(doc.getMarkings());
				}
			}
		}

	}

	/**
	 * The method handles the returning of Json string.
	 *
	 * @param document
	 * @param userInput
	 * @param userName
	 * @return Json String of entities.
	 * @throws IOException
	 * @throws GerbilException
	 */
	@RequestMapping(value = "/submitResults", method = RequestMethod.POST)
	public String submitResults(
			@RequestParam(value = "documenturi") String document,
			@RequestParam(value = "markings") String userInput,
			@RequestParam(value = "username") String userName)
			throws IOException, GerbilException {
		int userId = getUser(userName);
		List<Marking> changes = transformEntityFromJson(userInput);
		Document result = null;
		String filename = null;
		// ADDING THE RESULT
		for (Document doc : documents) {
			if (doc.getDocumentURI().equals(document)) {
				{
					changes.addAll(doc.getMarkings(StanfordParsedMarking.class));
					doc.setMarkings(changes);
					result = doc;

				}

			}
		}
		if (result == null) {
			LOGGER.info("NO RESULT FRoM ThE USER");
		}

		String name = result.getDocumentURI().replaceAll("[^a-zA-Z0-9.-]", "_");
		filename = "result-" + name + userName;
		filename = name.substring(0, 20);
		counter++;
		Model nifModel = generateModifiedModel(result);

		LOGGER.info("SIZE Of DOCUMENT LIST" + documents.size());

		er.erraticMarkingUserInput(remainingDocuments, result);
		this.updateDocumentList(remainingDocuments);

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
		for (NamedEntityCorrections correction : document
				.getMarkings(NamedEntityCorrections.class)) {
			annotationResource = nifModel.getResource(NIFUriHelper.getNifUri(
					document.getDocumentURI(), correction.getStartPosition(),
					correction.getStartPosition() + correction.getLength()));
			System.out.println(correction.getUris().toString() + " -> "
					+ correction.getUserDecision());
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
	protected List<Document> loadDocuments() {
		List<Document> loadedDocuments = new ArrayList<Document>();
		List<Document> temp;
		if(DATASET_GIVEN==false)
		{
			LOGGER.info("LOADING DEFAULT DATASET!!");
		}
		else
		{
			LOGGER.info("LOADING USER GIVEN DATASET!!");
		}
		for (int i = 0; i < DATASET_FILES.length; ++i) {
			temp = readDocuments(new File(DATASET_FILES[i]));
			if (temp != null) {
				loadedDocuments.addAll(temp);
			} else {
				LOGGER.error("Couldn't load the dataset!");
			}
		}

		DocumentProcessor dp = new DocumentProcessor();
		dp.process(loadedDocuments);
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

	private List<Document> generateDocumentList() {
		List<Document> whitelist = new ArrayList<Document>();
		generateList(whitelist, new File("eaglet_data/result_user"));
		return whitelist;
	}

	private void generateList(List<Document> whitelist, File file) {
		if (file.exists()) {
			if (file.isDirectory()) {
				for (File f : file.listFiles()) {
					generateList(whitelist, f);
				}
			} else {
				List<Document> documents = readDocuments(file);
				if (documents != null) {
					for (Document document : documents) {
						whitelist.add(document);
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
				.replace(",http://", "> , <http://")
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

	public void recheckUserInput() throws GerbilException, IOException {

		new InputforPipeline(this.generateDocumentList(), "eaglet_data"
				+ File.separator + "result_final" + File.separator
				+ DATASET_FILES[0] + "-nif.ttl");
		LOGGER.info("FINAL output is genreated!! After our correction");

	}
}