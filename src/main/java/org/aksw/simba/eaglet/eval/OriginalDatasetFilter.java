package org.aksw.simba.eaglet.eval;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.gerbil.io.nif.NIFWriter;
import org.aksw.gerbil.io.nif.impl.TurtleNIFWriter;
import org.aksw.gerbil.transfer.nif.Document;
import org.apache.commons.io.IOUtils;

/**
 * This class is a tool for merging the pipe results into a corpus with the same
 * documents and annotations but without the results.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public class OriginalDatasetFilter {

    private static final String[] INPUT_FILES = new String[] { "eaglet_data/result_user/Micha_ACE",
            "eaglet_data/result_user/Micha_OKE", "eaglet_data/result_user/Micha_AIDA" };
    private static final String[] PIPE_FILES = new String[] { "eaglet_data/result_pipe/ACE2004-result-nif.ttl",
            "eaglet_data/result_pipe/OKE 2015 Task 1 evaluation dataset-result-nif.ttl",
            "eaglet_data/result_user/Result Kunal/AIDA_CoNLL-Test A-result-nif" };
    private static final String[] OUTPUT_FILES = new String[] { "eaglet_data/result_user/Orig_ACE.ttl",
            "eaglet_data/result_user/Orig_OKE.ttl", "eaglet_data/result_user/Orig_AIDA.ttl" };

    public static void main(String[] args) {
        NIFWriter writer = new TurtleNIFWriter();
        OutputStream out = null;
        for (int i = 0; i < INPUT_FILES.length; ++i) {
            List<Document> userDocuments = new ArrayList<Document>();
            AnnotationMerger.loadDocuments(new File(INPUT_FILES[i]), userDocuments);
            if (userDocuments.size() > InterRaterAgreement.MAX_NUMBER_OF_DOCUMENTS) {
                userDocuments = InterRaterAgreement.shrinkCorpus(userDocuments);
            }
            List<Document> pipeDocuments = new ArrayList<Document>();
            AnnotationMerger.loadDocuments(new File(PIPE_FILES[i]), pipeDocuments);

            List<Document> shrinkedCorpus = new ArrayList<Document>(userDocuments.size());
            Set<String> uris = new HashSet<String>();
            for (Document document : userDocuments) {
                uris.add(document.getDocumentURI());
            }
            for (Document document : pipeDocuments) {
                if (uris.contains(document.getDocumentURI())) {
                    shrinkedCorpus.add(document);
                }
            }
            try {
                out = new BufferedOutputStream(new FileOutputStream(OUTPUT_FILES[i]));
                writer.writeNIF(shrinkedCorpus, out);
                System.out.print("Generated ");
                System.out.print(OUTPUT_FILES[i]);
                System.out.print(" with ");
                System.out.print(shrinkedCorpus.size());
                System.out.println(" documents.");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                IOUtils.closeQuietly(out);
            }
        }
    }

}
