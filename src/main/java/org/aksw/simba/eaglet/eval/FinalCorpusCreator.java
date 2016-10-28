package org.aksw.simba.eaglet.eval;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.gerbil.io.nif.NIFWriter;
import org.aksw.gerbil.io.nif.impl.TurtleNIFWriter;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections.Check;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections.DecisionValue;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.BitSet;

public class FinalCorpusCreator {

    private static final Logger LOGGER = LoggerFactory.getLogger(FinalCorpusCreator.class);

    private static final String INPUT_FILES[] = new String[] { "eaglet_data/result_user/ACE_Micha.ttl",
            "eaglet_data/result_user/OKE_Micha.ttl", "eaglet_data/result_user/AIDA_Micha.ttl" };
    private static final String OUTPUT_FILES[] = new String[] { "eaglet_data/result_user/Final_ACE.ttl",
            "eaglet_data/result_user/Final_OKE.ttl", "eaglet_data/result_user/Final_AIDA.ttl" };
    private static final Set<String> BLACKLIST = new HashSet<String>(Arrays.asList("northeast", "he"));

    public static void main(String[] args) {
        FinalCorpusCreator creator = new FinalCorpusCreator();
        for (int i = 0; i < INPUT_FILES.length; ++i) {
            creator.run(new File(INPUT_FILES[i]), new File(OUTPUT_FILES[i]));
        }
    }

    public void run(File inputFile, File outputFile) {
        List<Document> documents = new ArrayList<>();
        AnnotationMerger.loadDocuments(inputFile, documents);

        for (Document document : documents) {
            createFinalDocument(document);
        }

        NIFWriter writer = new TurtleNIFWriter();
        OutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(outputFile));
            writer.writeNIF(documents, out);
        } catch (Exception e) {
            LOGGER.error("Couldn't write result file.", e);
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    private void createFinalDocument(Document document) {
        List<Marking> markings, newMarkings;
        markings = document.getMarkings();
        newMarkings = new ArrayList<Marking>(markings.size());
        BitSet blockedPositions = new BitSet();

        // First, add all annotations that have been added by the user
        for (NamedEntityCorrections nec : document.getMarkings(NamedEntityCorrections.class)) {
            if (nec.getUserDecision() == DecisionValue.ADDED) {
                newMarkings.add(nec);
                blockedPositions.set(nec.getStartPosition(), nec.getStartPosition() + nec.getLength());
            }
        }

        // Second, add all annotations that are CORRECT or only have an URI
        // problem and that do not overlap with one of the already added
        // annotations
        BitSet mask = new BitSet();
        String text = document.getText();
        String neString;
        for (NamedEntityCorrections nec : document.getMarkings(NamedEntityCorrections.class)) {
            if ((((nec.getResult() == Check.GOOD) || (nec.getResult() == Check.OUTDATED_URI)
                    || (nec.getResult() == Check.INVALID_URI) || (nec.getResult() == Check.DISAMBIG_URI))
                    && (nec.getUserDecision() == DecisionValue.CORRECT))) {
                mask.set(nec.getStartPosition(), nec.getStartPosition() + nec.getLength());
                neString = text.substring(nec.getStartPosition(), nec.getStartPosition() + nec.getLength());
                if ((BitSet.intersectionCount(blockedPositions, mask) == 0) && !BLACKLIST.contains(neString)) {
                    newMarkings.add(nec);
                }
                mask.clear();
            }
        }
        document.setMarkings(newMarkings);
    }
}
