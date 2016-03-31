package org.aksw.simba.eaglet.web;

import java.util.ArrayList;
import java.util.List;

import org.aksw.gerbil.dataset.DatasetConfiguration;
import org.aksw.gerbil.dataset.impl.nif.NIFFileDatasetConfig;
import org.aksw.gerbil.datatypes.ExperimentType;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class EagletController {

    private static final Logger LOGGER = LoggerFactory.getLogger(EagletController.class);

    private static final String DATASET_FILES[] = new String[] { "" };

    @Autowired
    private DatabaseAdapter database;

    @RequestMapping("/service")
    public String service() {
        LOGGER.info("Got a message to /service!");
        return "";
    }

    @RequestMapping("/service/next")
    public String nextDocument(@RequestParam(value = "user") String user) {
        int userId = Integer.parseInt(user);
        // TODO get the next document for this user
        String documentUri;
        // TODO load document with this URI
        Document document;
        // TODO transform the document and its markings into a JSON String
        return "";
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

    protected List<Document> loadDocuments() {
        DatasetConfiguration dataset;
        List<Document> loadedDocuments = new ArrayList<Document>();
        List<Document> tempDocuments;
        for (int i = 0; i < DATASET_FILES.length; ++i) {
            dataset = new NIFFileDatasetConfig("dataset", DATASET_FILES[i], false, ExperimentType.A2KB);
            try {
                tempDocuments = dataset.getDataset(ExperimentType.A2KB).getInstances();
                loadedDocuments.addAll(tempDocuments);
            } catch (GerbilException e) {
                LOGGER.error("Couldn't load the dataset!", e);
            }
        }
        return loadedDocuments;
    }
}
