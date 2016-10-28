package org.aksw.simba.eaglet.eval;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.gerbil.io.nif.DocumentListParser;
import org.aksw.gerbil.io.nif.DocumentListWriter;
import org.aksw.gerbil.io.nif.DocumentParser;
import org.aksw.gerbil.io.nif.utils.NIFUriHelper;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.NIFTransferPrefixMapping;
import org.aksw.gerbil.transfer.nif.data.StartPosBasedComparator;
import org.aksw.simba.eaglet.annotator.AdaptedAnnotationParser;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections.Check;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections.ErrorType;
import org.aksw.simba.eaglet.vocab.EAGLET;
import org.aksw.simba.eaglet.web.EagletController;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * This class is a tool for merging the pipe results into a corpus with the same
 * documents and annotations but without the results.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public class AnnotationMerger {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationMerger.class);

//     private static final String INPUT_FOLDER =
//     "eaglet_data/result_user/Micha_ACE";
     private static final String INPUT_FOLDER =
     "eaglet_data/result_user/Result Kunal/ACE";
     private static final String PIPE_RESULT_FILE =
     "eaglet_data/result_pipe/ACE2004-result-nif.ttl";
//     private static final String OUTPUT_FILE =
//     "eaglet_data/result_user/ACE_Micha.ttl";
     private static final String OUTPUT_FILE =
     "eaglet_data/result_user/ACE_Kunal.ttl";

    // private static final String INPUT_FOLDER =
    // "eaglet_data/result_user/Micha_OKE";
    //// private static final String INPUT_FOLDER =
    // "eaglet_data/result_user/Result Kunal/OKE";
    // private static final String PIPE_RESULT_FILE =
    // "eaglet_data/result_pipe/OKE 2015 Task 1 evaluation
    // dataset-result-nif.ttl";
    // private static final String OUTPUT_FILE =
    // "eaglet_data/result_user/OKE_Micha.ttl";
    //// private static final String OUTPUT_FILE =
    // "eaglet_data/result_user/OKE_Kunal.ttl";

//    private static final String INPUT_FOLDER = "eaglet_data/result_user/Micha_AIDA";
//    // private static final String INPUT_FOLDER =
//    // "eaglet_data/result_user/Result Kunal/AIDA_CoNLL-Test A-result-nif";
//    private static final String PIPE_RESULT_FILE = "eaglet_data/result_pipe/CoNLL-Test A-result-nif.ttl";
//    private static final String OUTPUT_FILE = "eaglet_data/result_user/AIDA_Micha.ttl";
//    // private static final String OUTPUT_FILE =
//    // "eaglet_data/result_user/AIDA_Kunal.ttl";

    public static void main(String[] args) {
        List<Document> userDocuments = new ArrayList<Document>();
        loadDocuments(new File(INPUT_FOLDER), userDocuments);

        List<Document> pipeDocuments = new ArrayList<Document>();
        loadDocuments(new File(PIPE_RESULT_FILE), pipeDocuments);

        printCounts(userDocuments, pipeDocuments);

        Map<String, NamedEntityCorrections[]> pipeResultDocs = new HashMap<String, NamedEntityCorrections[]>();
        List<NamedEntityCorrections> markings;
        StartPosBasedComparator comparator = new StartPosBasedComparator();
        for (Document d : pipeDocuments) {
            markings = d.getMarkings(NamedEntityCorrections.class);
            markings.sort(comparator);
            pipeResultDocs.put(d.getDocumentURI(), markings.toArray(new NamedEntityCorrections[markings.size()]));
        }

        NamedEntityCorrections[] pipeMarkings;
        for (Document d : userDocuments) {
            if (pipeResultDocs.containsKey(d.getDocumentURI())) {
                pipeMarkings = pipeResultDocs.get(d.getDocumentURI());
                markings = d.getMarkings(NamedEntityCorrections.class);
                markings.sort(comparator);
                for (NamedEntityCorrections marking : markings) {
                    for (int i = 0; (i < pipeMarkings.length)
                            && (pipeMarkings[i].getStartPosition() <= marking.getStartPosition()); ++i) {
                        if ((pipeMarkings[i].getStartPosition() == marking.getStartPosition())
                                && (pipeMarkings[i].getLength() == marking.getLength())) {
                            marking.setResult(pipeMarkings[i].getResult());
                            if (pipeMarkings[i].getError() != null) {
                                for (ErrorType error : pipeMarkings[i].getError()) {
                                    marking.setError(error);
                                }
                            }
                        }
                    }
                }
            } else {
                LOGGER.error("Couldn't find a document with the URI \"" + d.getDocumentURI() + "\".");
            }
        }

        writeDocuments(userDocuments, new File(OUTPUT_FILE));
    }

    private static void printCounts(List<Document> userDocuments, List<Document> pipeDocuments) {
        // temporary debug method...
        Set<String> uris = new HashSet<String>();
        for (Document d : userDocuments) {
            uris.add(d.getDocumentURI());
        }

        int annoCount = 0, localCount = 0, zero = 0;
        List<NamedEntityCorrections> necs;
        for (Document d : pipeDocuments) {
            if (uris.contains(d.getDocumentURI())) {
                necs = d.getMarkings(NamedEntityCorrections.class);
                localCount = 0;
                for (NamedEntityCorrections nec : necs) {
                    if (nec.getResult() != Check.INSERTED) {
                        ++localCount;
                    }
                }
                if (localCount == 0) {
                    ++zero;
                } else {
                    annoCount += localCount;
                }
            }
        }
        System.out.print(zero);
        System.out.println(" documents don't have a single annotation");
        System.out.print(annoCount);
        System.out.println(" annotations are present in the original dataset");
    }

    public static void writeDocuments(List<Document> userDocuments, File file) {
        Model nifModel = generateModel(userDocuments);

        if (!file.exists()) {
            File parent = file.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
        }
        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
            nifModel.write(out, "TTL");
            // NIFWriter writer = new TurtleNIFWriter();
            // writer.writeNIF(userDocuments, out);
        } catch (Exception e) {
            LOGGER.error("Exception while writing documents.", e);
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    private static Model generateModel(List<Document> documents) {
        Model nifModel = ModelFactory.createDefaultModel();
        nifModel.setNsPrefixes(NIFTransferPrefixMapping.getInstance());
        DocumentListWriter writer = new DocumentListWriter();
        writer.writeDocumentsToModel(nifModel, documents);
        Resource annotationResource;
        for (Document document : documents) {
            for (NamedEntityCorrections correction : document.getMarkings(NamedEntityCorrections.class)) {
                annotationResource = nifModel.getResource(NIFUriHelper.getNifUri(document.getDocumentURI(),
                        correction.getStartPosition(), correction.getStartPosition() + correction.getLength()));
                nifModel.add(annotationResource, EAGLET.hasUserDecision,
                        EAGLET.getUserDecision(correction.getUserDecision()));
                if (correction.getResult() != null) {
                    nifModel.add(annotationResource, EAGLET.hasCheckResult,
                            EAGLET.getCheckResult(correction.getResult()));
                }
            }
        }
        return nifModel;
    }

    public static void loadDocumentsFromFile(File file, List<Document> documents) {
        try {
            Model nifModel = ModelFactory.createDefaultModel();
            nifModel.setNsPrefixes(NIFTransferPrefixMapping.getInstance());
            FileInputStream fin = new FileInputStream(file);
            nifModel.read(new StringReader(EagletController.correctNIF(IOUtils.toString(fin))), "", "TTL");
            fin.close();
            DocumentListParser parser = new DocumentListParser(new DocumentParser(new AdaptedAnnotationParser()));
            documents.addAll(parser.parseDocuments(nifModel));
        } catch (Exception e) {
            LOGGER.error("Couldn't load the dataset! File=" + file.toString(), e);
        }
    }

    public static void loadDocuments(File file, List<Document> documents) {
        if (file.exists()) {
            if (file.isDirectory()) {
                for (File f : file.listFiles()) {
                    loadDocuments(f, documents);
                }
            } else {
                loadDocumentsFromFile(file, documents);
            }
        }
    }
}
