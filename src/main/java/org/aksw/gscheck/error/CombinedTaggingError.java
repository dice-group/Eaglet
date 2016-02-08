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

import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.util.Properties;

public class CombinedTaggingError {
    private static final DatasetConfiguration DATASET = new NIFFileDatasetConfig("DBpedia",
            "gerbil_data/datasets/spotlight/dbpedia-spotlight-nif.ttl", false, ExperimentType.A2KB);

    public static void main(String[] args) throws GerbilException {
        List<Document> documents = DATASET.getDataset(ExperimentType.A2KB).getInstances();
        List<CoreLabel> eligible_makrings = new ArrayList<CoreLabel>();
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        String substring;
        List<NamedEntity> entities;
        NamedEntity a, b;

        for (Document doc : documents) {
            String text = doc.getText();

            Annotation document = new Annotation(text);
            pipeline.annotate(document);

            List<CoreLabel> tokens = document.get(TokensAnnotation.class);
            for (CoreLabel token : tokens) {
                // we can get the token that has been marked inside the text
                if (token.get(PartOfSpeechAnnotation.class).startsWith("N")
                        && (!token.get(PartOfSpeechAnnotation.class).equals("NP"))
                        || (token.get(PartOfSpeechAnnotation.class).startsWith("JJ"))) {
                    eligible_makrings.add(token);
                    // System.out.println(token.get(TextAnnotation.class));
                    // System.out.print(token.get(LemmaAnnotation.class));
                    // this is its position

                    // System.out.println(" " +
                    // token.get(PartOfSpeechAnnotation.class) + " " +
                    // token.beginPosition() + " "
                    // + token.endPosition());
                }

                // this is its lemma
                // System.out.println(" >>>");
            }

            entities = doc.getMarkings(NamedEntity.class);
            Collections.sort(entities, new StartPosBasedComparator());
            if (entities.size() > 0) {
                b = entities.get(0);
                for (int i = 1; i < entities.size(); ++i) {
                    a = b;
                    b = entities.get(i); // make sure that the entities are not
                                         // overlapping
                    if ((a.getStartPosition() + a.getLength()) <= b.getStartPosition()) {
                        substring = text.substring(a.getStartPosition() + a.getLength(), b.getStartPosition());
                        if (substring.matches("[\\s]*")) {

                            String[] arr = text.substring(a.getStartPosition(), b.getStartPosition() + b.getLength())
                                    .split(" ");
                            for (String x : arr) {
                                for (CoreLabel z : eligible_makrings) {
                                    if (z.get(TextAnnotation.class).equals(x)) {
                                        System.out.println(
                                                z.get(TextAnnotation.class) + " " + z.get(PartOfSpeechAnnotation.class)
                                                        + " " + z.beginPosition() + " " + z.endPosition());
                                    }
                                }
                            }
                            System.out.println("I would connect two entities to a single large entity \""
                                    + text.substring(a.getStartPosition(), b.getStartPosition() + b.getLength())
                                    + "\".");

                        }

                    }
                }
            }
            // System.out.println("Document over!!");
            eligible_makrings.clear();
        }

    }
}
