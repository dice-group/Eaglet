package org.aksw.simba.eaglet.web;

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
        // TODO  store the user ID - file name - document URI triple into the database
    }

}
