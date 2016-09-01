package org.aksw.simba.eaglet.eval;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.gerbil.config.GerbilConfiguration;
import org.aksw.gerbil.datatypes.marking.ClassifiedNamedEntity;
import org.aksw.gerbil.datatypes.marking.ClassifiedSpanMeaning;
import org.aksw.gerbil.datatypes.marking.MarkingClasses;
import org.aksw.gerbil.evaluate.EvaluationResult;
import org.aksw.gerbil.evaluate.EvaluationResultContainer;
import org.aksw.gerbil.evaluate.Evaluator;
import org.aksw.gerbil.io.nif.NIFParser;
import org.aksw.gerbil.io.nif.impl.TurtleNIFParser;
import org.aksw.gerbil.matching.Matching;
import org.aksw.gerbil.matching.MatchingsSearcher;
import org.aksw.gerbil.matching.MatchingsSearcherFactory;
import org.aksw.gerbil.matching.impl.ClassifiedMeaningMatchingsSearcher;
import org.aksw.gerbil.matching.impl.clas.UriBasedMeaningClassifier;
import org.aksw.gerbil.semantic.kb.SimpleWhiteListBasedUriKBClassifier;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.data.NamedEntity;
import org.aksw.gerbil.transfer.nif.data.StartPosBasedComparator;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.BitSet;

public class CorpusComparator {

    private static final Logger LOGGER = LoggerFactory.getLogger(CorpusComparator.class);

    private static final String ORIGINAL_CORPUS_FILE = "eaglet_data/gerbil_data/datasets/KORE50/kore50-nif.ttl";
    private static final String CHECKED_CORPUS_FILE = "eaglet_data/result_user/KORE50/final/final.ttl";
    private static final String DEFAULT_WELL_KNOWN_KBS_PARAMETER_KEY = "org.aksw.gerbil.evaluate.DefaultWellKnownKB";
    private static final String DEFAULT_WELL_KNOWN_KBS[] = loadDefaultKBs();

    private static String[] loadDefaultKBs() {
        String kbs[] = GerbilConfiguration.getInstance().getStringArray(DEFAULT_WELL_KNOWN_KBS_PARAMETER_KEY);
        if (kbs == null) {
            LOGGER.error("Couldn't load the list of well known KBs. This instance might not work as expected!");
        }
        return kbs;
    }

    public static void main(String[] args) throws Exception {
        CorpusComparator comparator = new CorpusComparator();
        comparator.run(new File(ORIGINAL_CORPUS_FILE), new File(CHECKED_CORPUS_FILE));
    }

    protected UriBasedMeaningClassifier<ClassifiedSpanMeaning> classifier = new UriBasedMeaningClassifier<ClassifiedSpanMeaning>(
            new SimpleWhiteListBasedUriKBClassifier(DEFAULT_WELL_KNOWN_KBS), MarkingClasses.IN_KB);

    public void run(File originalCorpus, File checkedCorpus) throws Exception {
        Map<String, List<ClassifiedSpanMeaning>> originalDocuments = loadNamedEntities(originalCorpus);
        Map<String, List<ClassifiedSpanMeaning>> checkedDocuments = loadNamedEntities(checkedCorpus);
        if (originalDocuments.size() != checkedDocuments.size()) {
            LOGGER.error("The corpora have different sizes.");
            return;
        }

        List<List<ClassifiedSpanMeaning>> originalNEs = new ArrayList<>(originalDocuments.size());
        List<List<ClassifiedSpanMeaning>> checkedNEs = new ArrayList<>(originalDocuments.size());
        for (String uri : originalDocuments.keySet()) {
            originalNEs.add(originalDocuments.get(uri));
            if (checkedDocuments.containsKey(uri)) {
                checkedNEs.add(checkedDocuments.get(uri));
            } else {
                LOGGER.error("The document {} is not present in the list of checked documents.", uri);
                return;
            }
        }

        @SuppressWarnings("unchecked")
        MatchingsSearcher<ClassifiedSpanMeaning> spanMatchingsSearcher = (MatchingsSearcher<ClassifiedSpanMeaning>) MatchingsSearcherFactory
                .createSpanMatchingsSearcher(Matching.STRONG_ANNOTATION_MATCH);
        MatchingsSearcher<ClassifiedSpanMeaning> meaningMatchingsSearcher = new ClassifiedMeaningMatchingsSearcher<ClassifiedSpanMeaning>();
        int origEntitiesCount = 0, checkedEntitiesCount = 0, matchingEntities = 0, differingPos = 0,
                differingMeaning = 0, newEntities = 0, deletedEntities = 0;
        BitSet alreadyUsedResults, currentMatchings, exactlyMatching;
        BitSet spanMatchings[], meaningMatchings[];
        StartPosBasedComparator comparator = new StartPosBasedComparator();
        List<ClassifiedSpanMeaning> origDoc, checkedDoc;
        ClassifiedSpanMeaning origNe;
        for (int i = 0; i < originalNEs.size(); ++i) {
            origDoc = originalNEs.get(i);
            origDoc.sort(comparator);
            origEntitiesCount += origDoc.size();
            checkedDoc = checkedNEs.get(i);
            checkedDoc.sort(comparator);
            checkedEntitiesCount += checkedDoc.size();
            spanMatchings = new BitSet[origDoc.size()];
            meaningMatchings = new BitSet[origDoc.size()];
            alreadyUsedResults = new BitSet();
            for (int j = 0; j < origDoc.size(); ++j) {
                origNe = origDoc.get(j);
                spanMatchings[j] = spanMatchingsSearcher.findMatchings(origNe, checkedDoc, alreadyUsedResults);
                meaningMatchings[j] = meaningMatchingsSearcher.findMatchings(origNe, checkedDoc, alreadyUsedResults);
            }
            exactlyMatching = new BitSet(spanMatchings.length);
            // search for all entities that are exactly matching
            for (int j = 0; j < origDoc.size(); ++j) {
                currentMatchings = (BitSet) spanMatchings[j].clone();
                currentMatchings.and(meaningMatchings[j]);
                if (currentMatchings.cardinality() > 0) {
                    alreadyUsedResults.set(currentMatchings.nextSetBit(0));
                    ++matchingEntities;
                    exactlyMatching.set(j);
                }
            }
            // search for all entities that are partly matching
            for (int j = 0; j < origDoc.size(); ++j) {
                if (!exactlyMatching.get(j)) {
                    spanMatchings[j].andNot(alreadyUsedResults);
                    // if the position is matching
                    if (spanMatchings[j].cardinality() > 0) {
                        ++differingMeaning;
                        alreadyUsedResults.set(spanMatchings[j].nextSetBit(0));
                    } else {
                        meaningMatchings[j].andNot(alreadyUsedResults);
                        // if the meaning is matching
                        if (meaningMatchings[j].cardinality() > 0) {
                            ++differingPos;
                            alreadyUsedResults.set(meaningMatchings[j].nextSetBit(0));
                        } else {
                            ++deletedEntities;
                        }
                    }
                }
            }
            newEntities += checkedDoc.size() - alreadyUsedResults.cardinality();
        }
        System.out.print("original entities = ");
        System.out.println(origEntitiesCount);
        System.out.print("matching entities = ");
        System.out.println(matchingEntities);
        System.out.print("diff pos entities = ");
        System.out.println(differingPos);
        System.out.print("diff uri entities = ");
        System.out.println(differingMeaning);
        System.out.print("deleted entities  = ");
        System.out.println(deletedEntities);
        System.out.print("added entities    = ");
        System.out.println(newEntities);
        System.out.print("final entities    = ");
        System.out.println(checkedEntitiesCount);

        System.out.println();
        System.out.print("checking results: matching + diff pos + diff uri + deleted == original ? ");
        System.out.println((matchingEntities + differingPos + differingMeaning + deletedEntities) == origEntitiesCount);
        System.out.print("checking results: matching + diff pos + diff uri + added == final ? ");
        System.out.println((matchingEntities + differingPos + differingMeaning + newEntities) == checkedEntitiesCount);
    }

    protected Map<String, List<ClassifiedSpanMeaning>> loadNamedEntities(File corpus) throws Exception {
        List<Document> documents = loadDocuments(corpus);
        Map<String, List<ClassifiedSpanMeaning>> nes = new HashMap<>(2 * documents.size());
        for (Document document : documents) {
            nes.put(document.getDocumentURI(), classify(document.getMarkings(NamedEntity.class)));
        }
        return nes;
    }

    protected List<ClassifiedSpanMeaning> classify(List<NamedEntity> markings) {
        List<ClassifiedSpanMeaning> classifiedMarkings = new ArrayList<>(markings.size());
        ClassifiedNamedEntity classifiedNE;
        for (NamedEntity ne : markings) {
            classifiedNE = new ClassifiedNamedEntity(ne.getStartPosition(), ne.getLength(), ne.getUris());
            classifier.classify(classifiedNE);
            classifiedMarkings.add(classifiedNE);
        }
        return classifiedMarkings;
    }

    protected List<Document> loadDocuments(File corpus) throws Exception {
        NIFParser parser = new TurtleNIFParser();
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(corpus);
            return parser.parseNIF(fin);
        } finally {
            IOUtils.closeQuietly(fin);
        }
    }

    @SuppressWarnings("unchecked")
    protected EvaluationResult evaluate(List<Evaluator<? extends Marking>> evaluators,
            List<List<Marking>> annotatorResults, List<List<Marking>> goldStandard) {
        EvaluationResultContainer evalResults = new EvaluationResultContainer();
        for (Evaluator<? extends Marking> e : evaluators) {
            ((Evaluator<Marking>) e).evaluate(annotatorResults, goldStandard, evalResults);
        }
        return evalResults;
    }
}
