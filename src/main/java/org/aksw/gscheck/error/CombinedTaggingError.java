package org.aksw.gscheck.error;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.aksw.gerbil.dataset.DatasetConfiguration;
import org.aksw.gerbil.dataset.impl.nif.NIFFileDatasetConfig;
import org.aksw.gerbil.datatypes.ExperimentType;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.data.NamedEntity;
import org.aksw.gerbil.transfer.nif.data.StartPosBasedComparator;

public class CombinedTaggingError {
    private static final DatasetConfiguration DATASET = new NIFFileDatasetConfig("DBpedia",
            "gerbil_data/datasets/spotlight/dbpedia-spotlight-nif.ttl", false, ExperimentType.A2KB);

    public static void main(String[] args) throws GerbilException {
        List<Document> documents = DATASET.getDataset(ExperimentType.A2KB).getInstances();

        String text, substring;
        List<NamedEntity> entities;
        NamedEntity a, b;
        for (Document doc : documents) {
            text = doc.getText();
            entities = doc.getMarkings(NamedEntity.class);
            Collections.sort(entities, new StartPosBasedComparator());
            if (entities.size() > 0) {
                b = entities.get(0);
                for (int i = 1; i < entities.size(); ++i) {
                    a = b;
                    b = entities.get(i);
                    // make sure that the entities are not overlapping
                    if ((a.getStartPosition() + a.getLength()) <= b.getStartPosition()) {
                        substring = text.substring(a.getStartPosition() + a.getLength(), b.getStartPosition());
                        if (substring.matches("[\\s]*")) {
                            System.out.println("I would connect two entities to a single large entity \""
                                    + text.substring(a.getStartPosition(), b.getStartPosition() + b.getLength())
                                    + "\".");
                        }
                    }
                }
            }
        }
    }

}
