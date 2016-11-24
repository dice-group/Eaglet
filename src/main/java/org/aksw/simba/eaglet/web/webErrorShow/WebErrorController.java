package org.aksw.simba.eaglet.web.webErrorShow;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.io.nif.DocumentListParser;
import org.aksw.gerbil.io.nif.DocumentParser;
import org.aksw.gerbil.io.nif.utils.NIFTransferPrefixMapping;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.simba.eaglet.annotator.AdaptedAnnotationParser;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections;
import org.aksw.simba.eaglet.errorcheckpipeline.CheckerPipeline;
import org.aksw.simba.eaglet.errorcheckpipeline.InputforPipeline;
import org.aksw.simba.eaglet.io.nif.impl.EagletAnnotationWriter;
import org.aksw.simba.eaglet.io.nif.impl.JsonLDNIFWriter;
import org.aksw.simba.eaglet.vocab.EAGLET;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

@Controller
public class WebErrorController {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebErrorController.class);
    private List<Document> documents;

    private static DocumentListParser parser = new DocumentListParser(
            new DocumentParser(new AdaptedAnnotationParser()));
    private static final String DATASET_FILES[] = new String[] { "/data1/Workspace/Eaglet/example.ttl" };

    /**
     * The method transforms the document into Json for parsing in the
     * webservice.
     *
     * @param document
     * @return Json string
     */
    private static String transformDocToJson(Document document) {
        // JSONObject doc = new JSONObject();
        // doc.append("text", document.getText());
        // doc.append("uri", document.getDocumentURI());
        // JSONArray array = new JSONArray();
        // JSONObject ne;
        // List<NamedEntityCorrections> necs =
        // document.getMarkings(NamedEntityCorrections.class);
        // necs.sort(new StartPosBasedComparator());
        // for (NamedEntityCorrections nec : necs) {
        // ne = new JSONObject();
        // ne.append("start", nec.getStartPosition());
        // ne.append("length", nec.getLength());
        // ne.append("partner", nec.getPartner());
        // ne.append("result", nec.getResult());
        // ne.append("doc", nec.getDoc());
        // ne.append("uris", nec.getUris());
        // ne.append("name", document.getText()
        // .substring(nec.getStartPosition(), nec.getStartPosition() +
        // nec.getLength()).toUpperCase());
        // ne.append("error", nec.getError().toString());
        // array.put(ne);
        // }
        // doc.append("markings", array);
        // return doc.toString();

        JsonLDNIFWriter writer = new JsonLDNIFWriter();
        writer.getDocumentListWriter().getDocumentWriter().setAnnotationWriter(new EagletAnnotationWriter());
        return writer.writeNIF(Arrays.asList(document));
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
            List<NamedEntityCorrections.ErrorType> error = new ArrayList<NamedEntityCorrections.ErrorType>();
            String errortype = markings.getJSONObject(i).getString("error");
            error.add(parseErroResult(errortype));
            Marking entity = new NamedEntityCorrections(markings.getJSONObject(i).getInt("start"),
                    markings.getJSONObject(i).getInt("length"), uris, error,
                    parseDecisionType(markings.getJSONObject(i).getString("decision")));
            userAcceptedEntities.add(entity);
        }
        return userAcceptedEntities;
    }





    private NamedEntityCorrections.ErrorType parseErroResult(String errortype) {
        if (errortype.toUpperCase().equals("OVERLAPPING")) {
            return NamedEntityCorrections.ErrorType.OVERLAPPING;
        } else if (errortype.toUpperCase().equals("COMBINED")) {
            return NamedEntityCorrections.ErrorType.COMBINED;
        } else if (errortype.toUpperCase().equals("ERRATIC")) {
            return NamedEntityCorrections.ErrorType.ERRATIC;
        } else if (errortype.toUpperCase().equals("WRONGPOSITION")) {
            return NamedEntityCorrections.ErrorType.WRONGPOSITION;
        } else if (errortype.toUpperCase().equals("LONGDESC")) {
            return NamedEntityCorrections.ErrorType.LONGDESC;
        } else if (errortype.toUpperCase().equals("INVALIDURIERR")) {
            return NamedEntityCorrections.ErrorType.INVALIDURIERR;
        } else if (errortype.toUpperCase().equals("DISAMBIGURIERR")) {
            return NamedEntityCorrections.ErrorType.DISAMBIGURIERR;
        } else if (errortype.toUpperCase().equals("OUTDATEDURIERR")) {
            return NamedEntityCorrections.ErrorType.OUTDATEDURIERR;

        } else {

            return null;
        }
    }

    private NamedEntityCorrections.DecisionValue parseDecisionType(String errortype) {
        if (errortype.toUpperCase().equals("CORRECT")) {
            return NamedEntityCorrections.DecisionValue.CORRECT;
        } else if (errortype.toUpperCase().equals("WRONG")) {
            return NamedEntityCorrections.DecisionValue.WRONG;
        } else if (errortype.toUpperCase().equals("ADDED")) {
            return NamedEntityCorrections.DecisionValue.ADDED;
        } else {

            return null;
        }
    }

    private static List<Document> readDocuments(File file) {
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
        return builder.toString().replace("<null>", "<http://aksw.org/notInWiki/null>")
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

    @RequestMapping(value = "/post-turtle-string",method = RequestMethod.POST)
    public ResponseEntity<String> postTurtle(@RequestParam("turtle") String text) {
        String out = null;
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "application/json;charset=utf-8");

        if (text.equals("")) {

            return new ResponseEntity<String>("No Content found", responseHeaders, HttpStatus.NO_CONTENT);
        }
        try {

            List<Document> documents = new ArrayList<>();
            Model nifModel = ModelFactory.createDefaultModel();
            nifModel.setNsPrefixes(NIFTransferPrefixMapping.getInstance());
            text = correctNIF(text);
            nifModel.read(new StringReader(text), "", "TTL");
            documents.addAll(parser.parseDocuments(nifModel));
            out = errorCheck(documents);

        } catch (GerbilException e) {
            LOGGER.error(e.getMessage());
            return new ResponseEntity<String>("No Content found", responseHeaders, HttpStatus.NO_CONTENT);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            return new ResponseEntity<String>("No Content found", responseHeaders, HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<String>(out, responseHeaders, HttpStatus.OK);

    }

    @RequestMapping(value = "/post-turtle-file", method = RequestMethod.POST)
    public ResponseEntity<String> PostTurtleFile(@RequestParam("file") MultipartFile file) throws IOException, GerbilException {

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "application/json;charset=utf-8");

        // get the Document
        List<Document> documents;
        File temp = File.createTempFile("turtle-file", ".ttl");
        file.transferTo(temp);
        documents = readDocuments(temp);

        // Document equils Null
        if (documents == null) {

            return new ResponseEntity<String>("No Document found", responseHeaders, HttpStatus.NOT_FOUND);
        }
        String jasonLDString = errorCheck(documents);

        return new ResponseEntity<String>(jasonLDString, responseHeaders, HttpStatus.OK);
    }

    private String errorCheck(List<Document> documents) throws GerbilException, IOException {
        // Data for input Pipline and initialization
        InputforPipeline preprocessor = new InputforPipeline();
        preprocessor.prePipeProcessor(documents);
        // After the preprocessing, we can use the pipeline to search for errors
        CheckerPipeline pipeline = new CheckerPipeline();
        pipeline.runPipe(documents);
        String jasonLDString = null;

        for (Document d : documents) {
            jasonLDString += transformDocToJson(d);
        }
        return jasonLDString;
    }

    public static void main(String[] args) throws IOException, GerbilException {
        List<Document> documents;
        documents = readDocuments(new File("/data1/Workspace/Eaglet/example.ttl"));
        // We have to preprocess the documents before we can insert them into
        // the pipeline (Note that later on, the preprocess as well as the
        // checker pipeline should be attributes of this class, so that they are
        // only created once).
        InputforPipeline preprocessor = new InputforPipeline();
        preprocessor.prePipeProcessor(documents);
        // After the preprocessing, we can use the pipeline to search for errors
        CheckerPipeline pipeline = new CheckerPipeline();
        pipeline.runPipe(documents);
        // Done. Print the result.
        System.out.println(transformDocToJson(documents.get(0)));
        System.out.println(transformDocToJson(documents.get(1)));
    }

}
