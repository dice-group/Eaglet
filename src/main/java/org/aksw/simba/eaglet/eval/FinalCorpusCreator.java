package org.aksw.simba.eaglet.eval;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.aksw.gerbil.io.nif.NIFWriter;
import org.aksw.gerbil.io.nif.impl.TurtleNIFWriter;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.simba.eaglet.entitytypemodify.EntityCheck;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FinalCorpusCreator {

    private static final Logger LOGGER = LoggerFactory.getLogger(FinalCorpusCreator.class);

    private static final int MERGE_USER_ID = 8;
    private static final String USER_OUTPUT_FOLDER = "eaglet_data/result_user/KORE50/final";

    public static void main(String[] args) {
        FinalCorpusCreator merger = new FinalCorpusCreator();
        merger.run(MERGE_USER_ID, new File(USER_OUTPUT_FOLDER));
    }

    public void run(int mergeUserId, File userOutputFolder) {
        List<Document> documents = InterRaterAgreement.loadDocuments(new int[] { mergeUserId }, userOutputFolder)
                .get(0);

        NIFWriter writer = new TurtleNIFWriter();
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(userOutputFolder.getAbsolutePath() + File.separator + "final.ttl");
            writer.writeNIF(documents, fout);
        } catch (Exception e) {
            LOGGER.error("Couldn't write result file.", e);
        } finally {
            IOUtils.closeQuietly(fout);
        }
        filter(documents);
        try {
            fout = new FileOutputStream(userOutputFolder.getAbsolutePath() + File.separator + "final_strict.ttl");
            writer.writeNIF(documents, fout);
        } catch (Exception e) {
            LOGGER.error("Couldn't write result file.", e);
        } finally {
            IOUtils.closeQuietly(fout);
        }
    }

    private void filter(List<Document> documents) {
        for (Document document : documents) {
            filter(document);
        }
    }

    private void filter(Document document) {
        List<EntityCheck> checkObjects = document.getMarkings(EntityCheck.class);
        List<Marking> newMarkings = new ArrayList<Marking>(checkObjects.size());
        for (EntityCheck entity : checkObjects) {
            if (entity.isNamedEntity()) {
                newMarkings.add(entity);
            }
        }
        document.setMarkings(newMarkings);
    }
}
