package org.aksw.simba.eaglet.eval;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.io.nif.NIFParser;
import org.aksw.gerbil.io.nif.impl.TurtleNIFParser;
import org.aksw.gerbil.matching.Matching;
import org.aksw.gerbil.matching.MatchingsSearcher;
import org.aksw.gerbil.matching.MatchingsSearcherFactory;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.MeaningSpan;
import org.aksw.gerbil.transfer.nif.data.StartPosBasedComparator;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections.Check;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections.DecisionValue;
import org.aksw.simba.eaglet.web.EagletController;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.BitSet;

public class CompletionModuleEval {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompletionModuleEval.class);

//    private static final String[] USER_INPUT_FILES = new String[] { "eaglet_data/result_user/AIDA_Micha.ttl",
//            "eaglet_data/result_user/AIDA_Kunal.ttl" };
//    public static final String DATASET_NAME = "AIDA_CoNLL_Test_A";

//     private static final String[] USER_INPUT_FILES = new String[] {
//     "eaglet_data/result_user/OKE_Kunal.ttl",
//     "eaglet_data/result_user/OKE_Micha.ttl" };
//     public static final String DATASET_NAME = "OKE";

     public static final String[] USER_INPUT_FILES = {
     "eaglet_data/result_user/ACE_Kunal.ttl",
     "eaglet_data/result_user/ACE_Micha.ttl" };
     public static final String DATASET_NAME = "ACE";

    public static final File ANNOTATOR_RESULT_FOLDER = new File("eaglet_data/Result_Annotator");
    public static final MatchingsSearcher<MeaningSpan> searcher = (MatchingsSearcher<MeaningSpan>) MatchingsSearcherFactory
            .createSpanMatchingsSearcher(Matching.WEAK_ANNOTATION_MATCH);
    public static final int MIN_NUMBER_OF_AGREED_ANNOTATORS = 5;

    public static void main(String[] args) {
        List<Map<String, List<MeaningSpan>>> userCorpora = loadUserAnnotations();
        Set<String> documentUris = userCorpora.get(0).keySet();
        Map<String, List<List<MeaningSpan>>> systemResults = loadAnnotatorResults(DATASET_NAME, documentUris);
        List<List<MeaningSpan>> userAnnotations = new ArrayList<>();
        List<List<MeaningSpan>> systemAnnotations;
        int[] counts;
        int sum = 0;
        int maxSum = 0;
        for (String uri : documentUris) {
            for (Map<String, List<MeaningSpan>> userCorpus : userCorpora) {
                userAnnotations.add(userCorpus.get(uri));
            }
            systemAnnotations = systemResults.get(uri);
            counts = countMatchingMarkings(userAnnotations, systemAnnotations);
            sum += counts[0];
            maxSum += counts[1];
            userAnnotations.clear();
        }
        System.out.println("Found " + sum + "/" + maxSum + " (" + (sum * 100 / maxSum) + "%) matching annotations");
    }

    private static int[] countMatchingMarkings(List<List<MeaningSpan>> userAnnotations,
            List<List<MeaningSpan>> systemAnnotations) {
        List<MeaningSpan> mergedUserAnnotations = userAnnotations.get(0);
        for (int i = 1; i < userAnnotations.size(); ++i) {
            mergedUserAnnotations = mergeUserAnnotations(mergedUserAnnotations, userAnnotations.get(i));
        }
        StartPosBasedComparator comparator = new StartPosBasedComparator();
        mergedUserAnnotations.sort(comparator);
        BitSet alreadyUsedMarkings[] = new BitSet[systemAnnotations.size()];
        List<MeaningSpan> systemMarkings;
        for (int i = 0; i < systemAnnotations.size(); ++i) {
            systemMarkings = systemAnnotations.get(i);
            systemMarkings.sort(comparator);
            alreadyUsedMarkings[i] = new BitSet(systemMarkings.size());
        }
        int ids[] = new int[systemAnnotations.size()];
        int count4Marking, overallCount = 0;
        BitSet matchingElements;
        for (MeaningSpan expectedMarking : mergedUserAnnotations) {
            count4Marking = 0;
            for (int i = 0; i < systemAnnotations.size(); ++i) {
                systemMarkings = systemAnnotations.get(i);
                matchingElements = searcher.findMatchings(expectedMarking, systemMarkings, alreadyUsedMarkings[i]);
                if (!matchingElements.isEmpty()) {
                    ++count4Marking;
                    ids[i] = matchingElements.nextSetBit(0);
                } else {
                    ids[i] = -1;
                }
            }
            if (count4Marking > MIN_NUMBER_OF_AGREED_ANNOTATORS) {
                ++overallCount;
                for (int i = 0; i < ids.length; ++i) {
                    if (ids[i] >= 0) {
                        alreadyUsedMarkings[i].set(ids[i]);
                    }
                }
            }
        }
        return new int[] { overallCount, mergedUserAnnotations.size() };
    }

    private static List<Map<String, List<MeaningSpan>>> loadUserAnnotations() {
        List<Map<String, List<MeaningSpan>>> userAnnotations = new ArrayList<>();
        for (int i = 0; i < USER_INPUT_FILES.length; ++i) {
            userAnnotations.add(loadUserDocuments(USER_INPUT_FILES[i]));
        }
        return userAnnotations;
    }

    private static Map<String, List<MeaningSpan>> loadUserDocuments(String file) {
        List<Document> documents = new ArrayList<>();
        AnnotationMerger.loadDocuments(new File(file), documents);
        Map<String, List<MeaningSpan>> userAnnotations = new HashMap<>();
        for (Document document : documents) {
            List<MeaningSpan> checkedEntities = new ArrayList<MeaningSpan>();
            for (NamedEntityCorrections nec : document.getMarkings(NamedEntityCorrections.class)) {
                if (nec.getUserDecision() == DecisionValue.ADDED) {
                    checkedEntities.add(nec);
                }
            }
            userAnnotations.put(document.getDocumentURI(), checkedEntities);
        }
        return userAnnotations;
    }

    public static List<MeaningSpan> mergeUserAnnotations(List<MeaningSpan> userA, List<MeaningSpan> userB) {
        List<MeaningSpan> mergedResults = new ArrayList<>();
        BitSet matchingElements;
        BitSet alreadyUsedResults = new BitSet(userA.size());
        int id;
        for (MeaningSpan expectedElement : userB) {
            matchingElements = searcher.findMatchings(expectedElement, userA, alreadyUsedResults);
            if (!matchingElements.isEmpty()) {
                id = matchingElements.nextSetBit(0);
                if (expectedElement.getLength() > userA.get(id).getLength()) {
                    mergedResults.add(expectedElement);
                } else {
                    mergedResults.add(userA.get(id));
                }
                alreadyUsedResults.set(id);
            }
        }
        return mergedResults;
    }

    public static void addMissingEntity(List<List<MeaningSpan>> annotator_result, Document doc) throws GerbilException {
        List<NamedEntityCorrections> existingEntities = doc.getMarkings(NamedEntityCorrections.class);
        Set<MeaningSpan> newEntities = new HashSet<MeaningSpan>();
        NamedEntityCorrections existingEntity;
        boolean found = false;
        // iterate over all annotatorResult
        for (List<MeaningSpan> list : annotator_result) {
            for (MeaningSpan annotatorentity : list) {
                found = false;
                for (int i = 0; !found && (i < existingEntities.size()); ++i) {
                    existingEntity = existingEntities.get(i);
                    if ((existingEntity.getStartPosition() == annotatorentity.getStartPosition())
                            && (existingEntity.getLength() == annotatorentity.getLength())) {
                        found = true;
                    }
                }
                if (!found) {
                    newEntities.add(annotatorentity);
                }
            }
        }
        // add new entities
        for (MeaningSpan newEntity : newEntities) {
            doc.addMarking(new NamedEntityCorrections(newEntity.getStartPosition(), newEntity.getLength(),
                    newEntity.getUris(), Check.COMPLETED));
        }
    }

    public static Map<String, List<List<MeaningSpan>>> loadAnnotatorResults(String datasetName,
            Set<String> documentUris) {
        Map<String, List<List<MeaningSpan>>> result = new HashMap<>();
        for (String uri : documentUris) {
            result.put(uri, new ArrayList<>());
        }
        List<Document> documents;
        for (File file : ANNOTATOR_RESULT_FOLDER.listFiles()) {
            if (file.getName().contains(datasetName)) {
                documents = loadDocuments(file);
                addToMap(documents, result);
            }
        }
        return result;
    }

    private static List<Document> loadDocuments(File file) {
        FileInputStream in = null;
        GZIPInputStream gin = null;
        try {
            in = new FileInputStream(file);
            gin = new GZIPInputStream(in);
            NIFParser parser = new TurtleNIFParser();
            return parser.parseNIF(EagletController.correctNIF(IOUtils.toString(gin)));
        } catch (Exception e) {
            LOGGER.error("Couldn't write annotator result to file.", e);
        } finally {
            IOUtils.closeQuietly(gin);
            IOUtils.closeQuietly(in);
        }
        return null;
    }

    private static void addToMap(List<Document> documents, Map<String, List<List<MeaningSpan>>> map) {
        List<List<MeaningSpan>> nes;
        for (Document document : documents) {
            if (map.containsKey(document.getDocumentURI())) {
                nes = map.get(document.getDocumentURI());
                nes.add(document.getMarkings(MeaningSpan.class));
            }
        }
    }
}
