package org.aksw.simba.eaglet.completion;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.aksw.gerbil.annotator.A2KBAnnotator;
import org.aksw.gerbil.dataset.DatasetConfiguration;
import org.aksw.gerbil.dataset.impl.nif.NIFFileDatasetConfig;
import org.aksw.gerbil.datatypes.ExperimentType;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.MeaningSpan;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections.Check;
import org.aksw.simba.eaglet.error.ErrorChecker;
import org.aksw.simba.eaglet.errorutils.AnnotatorResult;

public class MissingEntityCompletion implements ErrorChecker {

    private List<A2KBAnnotator> annotators;

    public MissingEntityCompletion(List<A2KBAnnotator> annotators) {
        this.annotators = annotators;
    }

    @Deprecated
    public ArrayList<NamedEntityCorrections> completeDataset() throws GerbilException {
        File folder = new File("C:/Users/Kunal/workspace/gerbil/Results_anontator_dbpedia");
        File[] listOfFiles = folder.listFiles();
        ArrayList<NamedEntityCorrections> result_set = null;
        for (File file : listOfFiles) {
            if (file.isFile()) {
                String annotatorFilenName = file.getParent() + "/" + file.getName();
                // System.out.println(annotatorFilenName);
                System.out.println(file.getName());
                // Annotator alias_annotator =
                // AnnotatorResult.loadAnnotator(annotatorFilenName,file.getName());
                // List<NamedEntityCorrections> result_annotator =
                // AnnotatorResult.loadAnnotator(annotatorFilenName,
                // file.getName());
                // alias_annotator.docue
                // result_set = CompareWithGS(result_annotator);

                // AnnotatorResult.printlist(result_set);

            }

        }
        return result_set;

    }

    public static ArrayList<NamedEntityCorrections> CompareWithGS(List<NamedEntityCorrections> result_annotator)
            throws GerbilException {
        ArrayList<NamedEntityCorrections> result_set = new ArrayList<NamedEntityCorrections>();
        final DatasetConfiguration DATASET = new NIFFileDatasetConfig("DBpedia",
                "C:/Users/Kunal/workspace/gerbil/Results_anontator_dbpedia/WAT-DBpediaSpotlight-s-A2KB.ttl", false,
                ExperimentType.A2KB);
        ArrayList<NamedEntityCorrections> gs_entity_set = new ArrayList<NamedEntityCorrections>();
        List<Document> documents = DATASET.getDataset(ExperimentType.A2KB).getInstances();
        for (Document doc : documents) {
            List<NamedEntityCorrections> entities = doc.getMarkings(NamedEntityCorrections.class);
            gs_entity_set.addAll(entities);

        }

        for (NamedEntityCorrections en : result_annotator) {
            if (!(gs_entity_set).contains(en)) {
                en.setResult(Check.INSERTED);
                result_set.add(en);
            }
        }

        return result_set;
    }

    @Override
    public void check(List<Document> documents) {
        ArrayList<NamedEntityCorrections> result_set = new ArrayList<NamedEntityCorrections>();
        List<List<MeaningSpan>> annotatorResults = new ArrayList<List<MeaningSpan>>();
        for (Document document : documents) {
            annotatorResults.clear();
            for (A2KBAnnotator annotator : annotators) {
                annotatorResults.add(annotator.performA2KBTask(document));
            }
            CompareWithGS(annotatorResults, document);
        }
    }
}
