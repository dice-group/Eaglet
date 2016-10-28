package org.aksw.simba.eaglet.eval;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.Span;

/**
 * For one of the datasets, the pronoun "he" has been marked accidentially by
 * the pipeline. This class fixes this problem without the need to rerun the
 * manual evaluation.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public class PronounFilter {

    private static final String DATASET_FILES[] = { "eaglet_data/result_user/OKE_Kunal.ttl",
            "eaglet_data/result_user/OKE_Micha.ttl" };

    public static void main(String[] args) throws IOException {
        List<Document> corpus;
        List<Marking> markings, newMarkings;
        String text;
        int start;
        for (String dataset : DATASET_FILES) {
            corpus = new ArrayList<>();
            AnnotationMerger.loadDocuments(new File(dataset), corpus);

            for (Document document : corpus) {
                markings = document.getMarkings();
                newMarkings = new ArrayList<Marking>(markings.size());
                text = document.getText();
                for (Marking marking : markings) {
                    if (marking instanceof Span) {
                        start = ((Span) marking).getStartPosition();
                        if (!text.substring(start, start + ((Span) marking).getLength()).equals("he")) {
                            newMarkings.add(marking);
                        }
                    }
                }
                System.out.println((markings.size() - newMarkings.size()) + " markings deleted.");
                document.setMarkings(newMarkings);
            }
            AnnotationMerger.writeDocuments(corpus, new File(dataset));
        }
    }
}
