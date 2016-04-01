package org.aksw.simba.eaglet.web;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.gerbil.dataset.DatasetConfiguration;
import org.aksw.gerbil.dataset.impl.nif.NIFFileDatasetConfig;
import org.aksw.gerbil.datatypes.ExperimentType;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;
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
        return new ResponseEntity<String>(transformToJson(document), responseHeaders, HttpStatus.OK);
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

    private String transformToJson(Document document) {
        JSONObject doc = new JSONObject();
        doc.append("text", document.getText());
        // TODO uri
        JSONArray array = new JSONArray();
        JSONObject ne;
        for (NamedEntityCorrections nec : document.getMarkings(NamedEntityCorrections.class)) {
            ne = new JSONObject();
            ne.append("start", nec.getStartPosition());
            // TODO
            array.put(ne);
        }
        return doc.toString();
    }

    @RequestMapping("/service/submit")
    public void nextDocument(@RequestParam(value = "document") String document,
            @RequestParam(value = "user") String user) {
        int userId = Integer.parseInt(user);
        // TODO parse document from JSON

        // TODO generate file name
        // TODO serialize the document into a file
        // TODO store the user ID - file name - document URI triple into the
        // database
    }

    protected static List<Document> loadDocuments() {
        List<Document> loadedDocuments = new ArrayList<Document>();
        for (int i = 0; i < DATASET_FILES.length; ++i) {
            try {
                loadedDocuments.addAll(CheckerPipeline.readFromDocument(DATASET_FILES[i]));
            } catch (Exception e) {
                LOGGER.error("Couldn't load the dataset!", e);
            }
        }
        return loadedDocuments;
    }
}
