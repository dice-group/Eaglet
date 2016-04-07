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

import org.aksw.gerbil.dataset.DatasetConfiguration;
import org.aksw.gerbil.dataset.impl.nif.NIFFileDatasetConfig;
import org.aksw.gerbil.datatypes.ExperimentType;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.io.nif.DocumentListParser;
import org.aksw.gerbil.io.nif.DocumentParser;
import org.aksw.gerbil.io.nif.NIFWriter;
import org.aksw.gerbil.io.nif.impl.TurtleNIFWriter;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.NIFTransferPrefixMapping;
import org.aksw.gerbil.transfer.nif.data.DocumentImpl;
import org.aksw.gerbil.transfer.nif.data.NamedEntity;
import org.aksw.simba.eaglet.annotator.AdaptedAnnotationParser;
import org.aksw.simba.eaglet.database.EagletDatabaseStatements;
import org.aksw.simba.eaglet.entitytypemodify.EntityTypeChange;
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

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

@Controller
public class EagletController {

    private static final Logger LOGGER = LoggerFactory.getLogger(EagletController.class);

    private static final String DATASET_FILES[] = new String[] { "eaglet_data/gerbil_data/datasets/oke-challenge/example_data/task1.ttl" };

    @Autowired
    private EagletDatabaseStatements database;

    private List<Document> documents;

    public EagletController() {
        this.documents = loadDocuments();
    }

    public EagletController(List<Document> documents) {
        this.documents = documents;
    }

    @RequestMapping(value = "/next", produces = "application/json;charset=utf-8")
    public ResponseEntity<String> nextDocument(@RequestParam(value = "username") String userName) {
        LOGGER.info("Got a message to /next!");
        int userId;
        if (database.getUser(userName) == -1) {
            database.addUser(userName);
            userId = database.getUser(userName);
        } else {
            userId = database.getUser(userName);
        }
        // get the next document
        Document document = getNextDocument(userId);
        if (document == null) {
            // TODO return that this was the last document
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

    private String transformDocToJson(Document document) {
        JSONObject doc = new JSONObject();
        doc.append("text", document.getText());
        doc.append("uri", document.getDocumentURI());
        JSONArray array = new JSONArray();
        JSONObject ne;
        for (NamedEntityCorrections nec : document.getMarkings(NamedEntityCorrections.class)) {
            ne = new JSONObject();
            ne.append("start", nec.getStartPosition());
            ne.append("length", nec.getLength());
            ne.append("partner", nec.getPartner());
            ne.append("doc", nec.getDoc());
            ne.append("uris", nec.getUris());
            ne.append("name", nec.getEntity_name());
            array.put(ne);
        }
        doc.append("markings", array);
        return doc.toString();
    }

    private List<Marking> transformEntityFromJson(JSONArray userInput) {
        List<Marking> userAcceptedEntities = new ArrayList<Marking>();

        for (int i = 0; i < userInput.length(); i++) {
            Marking entity = new NamedEntity(userInput.getJSONObject(i).getInt("start"),
                    userInput.getJSONObject(i).getInt("length"), userInput.getJSONObject(i).getString("uri"));
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
            name = name.replaceAll("/", "-");
            filename = "result_" + userId + "_" + name;
        }
        Document newdoc = new DocumentImpl(result.getText(), result.getDocumentURI(), changes);
        FileOutputStream fout = new FileOutputStream(
                "C:/Users/Kunal/workspace/gs_check/gerbil_data/datasets/spotlight/" + filename + "-nif.ttl");
        // serialize the document into a file
        NIFWriter nif = new TurtleNIFWriter();
        nif.writeNIF(Arrays.asList(newdoc), fout);
        fout.close();

        // store the user ID - file name - document URI triple into the
        // database
        database.addDocument(userId, document, filename);
    }

    protected static List<Document> loadDocuments() {
        List<Document> loadedDocuments = new ArrayList<Document>();
        for (int i = 0; i < DATASET_FILES.length; ++i) {
            try {
                Model nifModel = ModelFactory.createDefaultModel();
                nifModel.setNsPrefixes(NIFTransferPrefixMapping.getInstance());
                FileInputStream fin = new FileInputStream(new File(DATASET_FILES[i]));
                nifModel.read(fin, "", "TTL");
                fin.close();
                DocumentListParser parser = new DocumentListParser(new DocumentParser(new AdaptedAnnotationParser()));
                List<Document> documents = parser.parseDocuments(nifModel);
                for (Document document : documents) {
                    loadedDocuments.add(new DocumentImpl(document.getText(), document.getDocumentURI(), EntityTypeChange.changeType(document)));
                }
                
//                loadedDocuments.addAll(CheckerPipeline.readDocuments(DATASET_FILES[i]));
            } catch (Exception e) {
                LOGGER.error("Couldn't load the dataset!", e);
            }
        }
        return loadedDocuments;
    }
}
