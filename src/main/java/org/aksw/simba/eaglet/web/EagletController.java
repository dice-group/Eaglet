package org.aksw.simba.eaglet.web;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.gerbil.dataset.DatasetConfiguration;
import org.aksw.gerbil.dataset.impl.nif.NIFFileDatasetConfig;
import org.aksw.gerbil.datatypes.ExperimentType;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.data.DocumentImpl;
import org.aksw.simba.eaglet.database.EagletDatabaseStatements;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections;
import org.aksw.simba.eaglet.errorcheckpipeline.CheckerPipeline;
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

@Controller
public class EagletController {

	private static final Logger LOGGER = LoggerFactory.getLogger(EagletController.class);

	private static final String DATASET_FILES[] = new String[] { "" };

	@Autowired
	private EagletDatabaseStatements database;

	private List<Document> documents;

	public EagletController(List<Document> documents) {
		this.documents = documents;
	}

	@RequestMapping("/service")
	public String service() {
		LOGGER.info("Got a message to /service!");
		return "";
	}

	@RequestMapping(method = RequestMethod.GET, value = "/loginpage")
	public String login(@RequestParam(value = "username") String userName) throws Exception {
		if (database.getUser(userName) == -1) {
			database.addUser(userName);
		} else {
			// TODO: To show what user needs to review (Load documents)
		}
		return "/loginpage";
	}

	@RequestMapping(value = "/service/next", produces = "application/json;charset=utf-8")
	public ResponseEntity<String> nextDocument(@RequestParam(value = "user") String user) {
		int userId = Integer.parseInt(user);
		// get the next document
		Document document = getNextDocument(userId);
		if (document == null) {
			// TODO return that this was the last document

		}
		// transform the document and its markings into a JSON String
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Content-Type", "application/json;charset=utf-8");
		// TODO send also the marking
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

	private String transformDocToJson(Document document) {
		JSONObject doc = new JSONObject();
		doc.append("text", document.getText());
		doc.append("uri", document.getDocumentURI());
		return doc.toString();
	}

	private String transformMarkingToJson(Document document) {

		JSONArray array = new JSONArray();
		JSONObject ne;
		for (NamedEntityCorrections nec : document.getMarkings(NamedEntityCorrections.class)) {
			ne = new JSONObject();
			ne.append("start", nec.getStartPosition());
			ne.append("length", nec.getLength());
			ne.append("partner", nec.getPartner());
			ne.append("Doc", nec.getDoc());
			ne.append("Uris", nec.getUris());
			array.put(ne);
		}
		return array.toString();
	}

	private List<NamedEntityCorrections> transformEntityFromJson(JSONArray userInput) {
		List<NamedEntityCorrections> userAcceptedEntities = new ArrayList<NamedEntityCorrections>();

		for (int i = 0; i < userInput.length(); i++) {
			NamedEntityCorrections entity = new NamedEntityCorrections(userInput.getJSONObject(i).getInt("start"),
					userInput.getJSONObject(i).getInt("length"), null, userInput.getJSONObject(i).getString("Doc"));
			userAcceptedEntities.add(entity);
		}
		return userAcceptedEntities;
	}

	@RequestMapping("/service/submitResults")
	public void submitResults(@RequestParam(value = "documenturi") String document,
			@RequestParam(value = "document") JSONArray userInput, @RequestParam(value = "user") String user)
			throws IOException {
		int userId = Integer.parseInt(user);
		// TODO parse document from JSON
		List<NamedEntityCorrections> changes = transformEntityFromJson(userInput);
		// TODO generate file name
		String filename = "result_" + userId + "_";

		// TODO serialize the document into a file

		FileOutputStream fout = new FileOutputStream(
				"C:/Users/Kunal/workspace/gs_check/gerbil_data/datasets/spotlight/dbpedia-spotlight-result-nif.ttl");
		// nifModel.write(fout, "TTL");

		fout.close();
		// store the user ID - file name - document URI triple into the
		// database
		database.addDocument(userId, document, filename);
	}

	protected static List<Document> loadDocuments() {
		List<Document> loadedDocuments = new ArrayList<Document>();
		for (int i = 0; i < DATASET_FILES.length; ++i) {
			try {
				loadedDocuments.addAll(CheckerPipeline.readDocuments(DATASET_FILES[i]));
			} catch (Exception e) {
				LOGGER.error("Couldn't load the dataset!", e);
			}
		}
		return loadedDocuments;
	}
}
