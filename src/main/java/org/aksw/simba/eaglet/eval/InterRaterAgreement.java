package org.aksw.simba.eaglet.eval;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.gerbil.annotator.Annotator;
import org.aksw.gerbil.annotator.AnnotatorConfiguration;
import org.aksw.gerbil.dataset.Dataset;
import org.aksw.gerbil.dataset.DatasetConfiguration;
import org.aksw.gerbil.datatypes.AbstractAdapterConfiguration;
import org.aksw.gerbil.datatypes.ExperimentTaskConfiguration;
import org.aksw.gerbil.datatypes.ExperimentType;
import org.aksw.gerbil.datatypes.marking.MarkingClasses;
import org.aksw.gerbil.evaluate.EvaluationResult;
import org.aksw.gerbil.evaluate.EvaluationResultContainer;
import org.aksw.gerbil.evaluate.Evaluator;
import org.aksw.gerbil.evaluate.EvaluatorFactory;
import org.aksw.gerbil.evaluate.SubTaskEvaluator;
import org.aksw.gerbil.evaluate.impl.ClassConsideringFMeasureCalculator;
import org.aksw.gerbil.evaluate.impl.FMeasureCalculator;
import org.aksw.gerbil.evaluate.impl.filter.MarkingFilteringEvaluatorDecorator;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.matching.Matching;
import org.aksw.gerbil.matching.MatchingsSearcher;
import org.aksw.gerbil.matching.MatchingsSearcherFactory;
import org.aksw.gerbil.matching.impl.ClassifiedMeaningMatchingsSearcher;
import org.aksw.gerbil.matching.impl.CompoundMatchingsSearcher;
import org.aksw.gerbil.matching.impl.MatchingsCounterImpl;
import org.aksw.gerbil.matching.impl.clas.EmergingEntityMeaningClassifier;
import org.aksw.gerbil.matching.impl.clas.UriBasedMeaningClassifier;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.utils.filter.AbstractMarkingFilter;
import org.aksw.simba.eaglet.entitytypemodify.ClassifiedEntityCheck;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections.Check;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections.DecisionValue;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections.ErrorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InterRaterAgreement {

    private static final Logger LOGGER = LoggerFactory.getLogger(InterRaterAgreement.class);

    private static final int USER_IDS[] = new int[] { 1, 2 };
    private static final String USER_OUTPUT_FOLDER = "eaglet_data/result_user/KORE50";

    public static final int MAX_NUMBER_OF_DOCUMENTS = 30;

     private static final String[] USER_OUTPUT_FOLDERS = new String[] {
     "eaglet_data/result_user/AIDA_Micha.ttl",
     "eaglet_data/result_user/AIDA_Kunal.ttl" };

    // private static final String[] USER_OUTPUT_FOLDERS = new String[] {
    // "src/test/resources/org/aksw/simba/eaglet/eval/user1",
    // "src/test/resources/org/aksw/simba/eaglet/eval/user2" };

//     private static final String[] USER_OUTPUT_FOLDERS = new String[] {
//     "eaglet_data/result_user/OKE_Kunal.ttl",
//     "eaglet_data/result_user/OKE_Micha.ttl" };

//    private static final String[] USER_OUTPUT_FOLDERS = new String[] { "eaglet_data/result_user/ACE_Kunal.ttl",
//            "eaglet_data/result_user/ACE_Micha.ttl" };

    public static void main(String[] args) {
        InterRaterAgreement raterAgreement = new InterRaterAgreement();
        raterAgreement.run(USER_IDS, new File(USER_OUTPUT_FOLDER));
    }

    private ExtendedEvaluatorFactory factory = new ExtendedEvaluatorFactory();

    public void run(int[] userIds, File userOutputFolder) {
        List<List<List<NamedEntityCorrections>>> annotations = loadAnnotations(userIds, userOutputFolder);
        List<Evaluator<NamedEntityCorrections>> evaluators;
        EvaluationResultContainer result;
        int checkCount;
        for (int i = 0; i < USER_OUTPUT_FOLDERS.length; ++i) {
            for (int j = i + 1; j < USER_OUTPUT_FOLDERS.length; ++j) {
                for (Check checkResult : Check.values()) {
                    checkCount = countMarkingsWithResult(checkResult, annotations.get(i))
                            + countMarkingsWithResult(checkResult, annotations.get(j));
                    if (checkCount > 0) {
                        LOGGER.info("Comparing user {} and {}.", USER_OUTPUT_FOLDERS[i], USER_OUTPUT_FOLDERS[j]);
                        evaluators = factory.createEvaluator4PipeDecisions(checkResult, USER_OUTPUT_FOLDERS[i],
                                USER_OUTPUT_FOLDERS[j]);
                        result = (EvaluationResultContainer) evaluate(evaluators, annotations.get(i),
                                annotations.get(j));
                        System.out.println("Decision for result " + checkResult + " (" + checkCount
                                + " annotations) user " + USER_OUTPUT_FOLDERS[i] + " vs. " + USER_OUTPUT_FOLDERS[j]);
                        printResult(result);
                    }
                }
                for (ErrorType errorType : ErrorType.values()) {
                    checkCount = countMarkingsWithResult(errorType, annotations.get(i))
                            + countMarkingsWithResult(errorType, annotations.get(j));
                    if (checkCount > 0) {
                        LOGGER.info("Comparing user {} and {}.", USER_OUTPUT_FOLDERS[i], USER_OUTPUT_FOLDERS[j]);
                        evaluators = factory.createEvaluator4ErrorType(errorType, USER_OUTPUT_FOLDERS[i],
                                USER_OUTPUT_FOLDERS[j]);
                        result = (EvaluationResultContainer) evaluate(evaluators, annotations.get(i),
                                annotations.get(j));
                        System.out.println("Decision for error type " + errorType + " (" + checkCount
                                + " annotations) user " + USER_OUTPUT_FOLDERS[i] + " vs. " + USER_OUTPUT_FOLDERS[j]);
                        printResult(result);
                    }
                }
                LOGGER.info("Comparing user {} and {}.", USER_OUTPUT_FOLDERS[i], USER_OUTPUT_FOLDERS[j]);
                evaluators = factory.createEvaluator4PipeDecisions(null, USER_OUTPUT_FOLDERS[i],
                        USER_OUTPUT_FOLDERS[j]);
                result = (EvaluationResultContainer) evaluate(evaluators, annotations.get(i), annotations.get(j));
                System.out.println("Decision for corret markings user " + USER_OUTPUT_FOLDERS[i] + " vs. "
                        + USER_OUTPUT_FOLDERS[j]);
                printResult(result);
                LOGGER.info("Comparing user {} and {}.", USER_OUTPUT_FOLDERS[i], USER_OUTPUT_FOLDERS[j]);
                evaluators = factory.createEvaluator4UserAdditions(USER_OUTPUT_FOLDERS[i], USER_OUTPUT_FOLDERS[j]);
                result = (EvaluationResultContainer) evaluate(evaluators, annotations.get(i), annotations.get(j));
                System.out.println("ADDED by user " + USER_OUTPUT_FOLDERS[i] + " vs. " + USER_OUTPUT_FOLDERS[j]);
                printResult(result);
            }
        }
    }

    private int countMarkingsWithResult(Check checkResult, List<List<NamedEntityCorrections>> annotations) {
        int count = 0;
        for (List<NamedEntityCorrections> list : annotations) {
            for (NamedEntityCorrections annotation : list) {
                if (annotation.getResult() == checkResult) {
                    ++count;
                }
            }
        }
        return count;
    }

    private int countMarkingsWithResult(ErrorType error, List<List<NamedEntityCorrections>> annotations) {
        int count = 0;
        for (List<NamedEntityCorrections> list : annotations) {
            for (NamedEntityCorrections annotation : list) {
                if ((annotation.getError() != null) && (annotation.getError().contains(error))) {
                    ++count;
                }
            }
        }
        return count;
    }

    protected EvaluationResult evaluate(List<Evaluator<NamedEntityCorrections>> evaluators,
            List<List<NamedEntityCorrections>> annotatorResults, List<List<NamedEntityCorrections>> goldStandard) {
        EvaluationResultContainer evalResults = new EvaluationResultContainer();
        for (Evaluator<NamedEntityCorrections> e : evaluators) {
            ((Evaluator<NamedEntityCorrections>) e).evaluate(annotatorResults, goldStandard, evalResults);
        }
        return evalResults;
    }

    protected List<List<List<NamedEntityCorrections>>> loadAnnotations(int[] userIds, File userOutputFolder) {
        List<List<Document>> documents = loadDocuments(userIds, userOutputFolder);
        // // FIXME REMOVE THIS FILTERING!
        // for (List<Document> documentList : documents) {
        // for (int i = documentList.size() - 1; i >= 0; --i) {
        // if
        // (!documentList.get(i).getDocumentURI().equals("http://AIDA/CoNLL-TestA/1099"))
        // {
        // documentList.remove(i);
        // }
        // }
        // }
        List<Map<String, List<NamedEntityCorrections>>> documentAnnotations = new ArrayList<Map<String, List<NamedEntityCorrections>>>(
                documents.size());
        Set<String> uris = generateMappings(documents, documentAnnotations);
        List<List<List<NamedEntityCorrections>>> annotations = new ArrayList<>(documents.size());
        documents = null;
        List<List<NamedEntityCorrections>> userAnnotations;
        Map<String, List<NamedEntityCorrections>> uriAnnotationMap;
        List<NamedEntityCorrections> emptyDocument = new ArrayList<>(0);
        for (int i = 0; i < userIds.length; ++i) {
            uriAnnotationMap = documentAnnotations.get(i);
            userAnnotations = new ArrayList<>();
            annotations.add(userAnnotations);
            for (String uri : uris) {
                if (uriAnnotationMap.containsKey(uri)) {
                    userAnnotations.add(uriAnnotationMap.get(uri));
                } else {
                    LOGGER.warn("user #{} does not have a result for document {}", userIds[i], uri);
                    userAnnotations.add(emptyDocument);
                }
            }
        }
        return annotations;
    }

    protected Set<String> generateMappings(List<List<Document>> documents,
            List<Map<String, List<NamedEntityCorrections>>> documentAnnotations) {
        Map<String, List<NamedEntityCorrections>> uriAnnotationMap;
        Set<String> uris = new HashSet<String>();
        for (List<Document> userDocuments : documents) {
            uriAnnotationMap = new HashMap<String, List<NamedEntityCorrections>>();
            documentAnnotations.add(uriAnnotationMap);
            for (Document document : userDocuments) {
                uriAnnotationMap.put(document.getDocumentURI(), document.getMarkings(NamedEntityCorrections.class));
                uris.add(document.getDocumentURI());
            }
        }
        return uris;
    }

    protected static List<List<Document>> loadDocuments(int userIds[], File userOutputFolder) {
        List<List<Document>> loadedDocuments = new ArrayList<List<Document>>();
        List<Document> userDocuments = null;
        // String expectedPrefix;
        for (int i = 0; i < USER_OUTPUT_FOLDERS.length; ++i) {
            userDocuments = new ArrayList<Document>();
            // userOutputFolder = new File(USER_OUTPUT_FOLDERS[i]);
            // // expectedPrefix = "result-" + userIds[i];
            // for (File file : userOutputFolder.listFiles()) {
            // // if (file.getName().startsWith(expectedPrefix))
            // try {
            // Model nifModel = ModelFactory.createDefaultModel();
            // nifModel.setNsPrefixes(NIFTransferPrefixMapping.getInstance());
            // FileInputStream fin = new FileInputStream(file);
            // nifModel.read(new
            // StringReader(EagletController.correctNIF(IOUtils.toString(fin))),
            // "", "TTL");
            // fin.close();
            // DocumentListParser parser = new DocumentListParser(
            // new DocumentParser(new AdaptedAnnotationParser()));
            // userDocuments.addAll(parser.parseDocuments(nifModel));
            // } catch (Exception e) {
            // LOGGER.error("Couldn't load the dataset!", e);
            // }
            // }
            AnnotationMerger.loadDocuments(new File(USER_OUTPUT_FOLDERS[i]), userDocuments);
            userDocuments.sort(new Comparator<Document>() {
                @Override
                public int compare(Document o1, Document o2) {
                    String uri1 = o1.getDocumentURI();
                    String uri2 = o2.getDocumentURI();
                    if (uri1 == null) {
                        return uri2 == null ? 0 : -1;
                    }
                    return uri1.compareTo(uri2);
                }
            });
            if (userDocuments.size() > MAX_NUMBER_OF_DOCUMENTS) {
                userDocuments = shrinkCorpus(userDocuments);
            }
            loadedDocuments.add(userDocuments);
        }
        return loadedDocuments;
    }

    public static List<Document> shrinkCorpus(List<Document> corpus) {
        List<Document> shrinkedCorpus = new ArrayList<Document>(corpus.size());
        Set<String> uris = new HashSet<String>();
        for (Document document : corpus) {
            uris.add(document.getDocumentURI());
        }
        String sortedUris[] = uris.toArray(new String[uris.size()]);
        for (int i = MAX_NUMBER_OF_DOCUMENTS; i < sortedUris.length; ++i) {
            uris.remove(sortedUris[i]);
        }
        for (Document document : corpus) {
            if (uris.contains(document.getDocumentURI())) {
                shrinkedCorpus.add(document);
            }
        }
        return shrinkedCorpus;
    }

    public static void printResult(EvaluationResult result) {
        if (result instanceof EvaluationResultContainer) {
            System.out.println("--- container start ---");
            for (EvaluationResult singleResult : ((EvaluationResultContainer) result).getResults()) {
                printResult(singleResult);
            }
            System.out.println("--- container end ---");
        } else {
            System.out.print(result.getName());
            System.out.print(" = ");
            System.out.println(result.getValue());
        }
    }

    protected class ExtendedEvaluatorFactory extends EvaluatorFactory {

        @SuppressWarnings("unchecked")
        public List<Evaluator<NamedEntityCorrections>> createEvaluator4PipeDecisions(Check checkResult, String name1,
                String name2) {
            List<Evaluator<NamedEntityCorrections>> evaluators = new ArrayList<Evaluator<NamedEntityCorrections>>();

            // The URIs are not interesting. It is sufficient to look at the
            // positions
            evaluators.add(new MarkingFilteringEvaluatorDecorator<NamedEntityCorrections>(
                    new AbstractMarkingFilter<NamedEntityCorrections>() {
                        @Override
                        public boolean isMarkingGood(NamedEntityCorrections marking) {
                            return marking.getUserDecision() != DecisionValue.ADDED;
                        }
                    }, new MarkingFilteringEvaluatorDecorator<NamedEntityCorrections>(
                            new AbstractMarkingFilter<NamedEntityCorrections>() {
                                @Override
                                public boolean isMarkingGood(NamedEntityCorrections marking) {
                                    return (checkResult == null) || (marking.getResult() == checkResult);
                                }
                            },
                            new FMeasureCalculator<NamedEntityCorrections>(
                                    new MatchingsCounterImpl<NamedEntityCorrections>(
                                            new CompoundMatchingsSearcher<>(new DecisionMatchingsSearcher(),
                                                    (MatchingsSearcher<NamedEntityCorrections>) MatchingsSearcherFactory
                                                            .createSpanMatchingsSearcher(
                                                                    Matching.STRONG_ANNOTATION_MATCH)))))));
            evaluators.add(new MarkingFilteringEvaluatorDecorator<NamedEntityCorrections>(
                    new AbstractMarkingFilter<NamedEntityCorrections>() {
                        @Override
                        public boolean isMarkingGood(NamedEntityCorrections marking) {
                            return marking.getUserDecision() != DecisionValue.ADDED;
                        }
                    }, new MarkingFilteringEvaluatorDecorator<NamedEntityCorrections>(
                            new AbstractMarkingFilter<NamedEntityCorrections>() {
                                @Override
                                public boolean isMarkingGood(NamedEntityCorrections marking) {
                                    return (checkResult == null) || (marking.getResult() == checkResult);
                                }
                            }, new CorrectnessAccuracy())));
            return evaluators;
        }

        @SuppressWarnings("unchecked")
        public List<Evaluator<NamedEntityCorrections>> createEvaluator4ErrorType(ErrorType errorType, String name1,
                String name2) {
            List<Evaluator<NamedEntityCorrections>> evaluators = new ArrayList<Evaluator<NamedEntityCorrections>>();

            // The URIs are not interesting. It is sufficient to look at the
            // positions
            evaluators.add(new MarkingFilteringEvaluatorDecorator<NamedEntityCorrections>(
                    new AbstractMarkingFilter<NamedEntityCorrections>() {
                        @Override
                        public boolean isMarkingGood(NamedEntityCorrections marking) {
                            return marking.getUserDecision() != DecisionValue.ADDED;
                        }
                    }, new MarkingFilteringEvaluatorDecorator<NamedEntityCorrections>(
                            new AbstractMarkingFilter<NamedEntityCorrections>() {
                                @Override
                                public boolean isMarkingGood(NamedEntityCorrections marking) {
                                    return ((marking.getError() != null) && (marking.getError().contains(errorType)));
                                }
                            },
                            new FMeasureCalculator<NamedEntityCorrections>(
                                    new MatchingsCounterImpl<NamedEntityCorrections>(
                                            new CompoundMatchingsSearcher<>(new DecisionMatchingsSearcher(),
                                                    (MatchingsSearcher<NamedEntityCorrections>) MatchingsSearcherFactory
                                                            .createSpanMatchingsSearcher(
                                                                    Matching.STRONG_ANNOTATION_MATCH)))))));
            evaluators.add(new MarkingFilteringEvaluatorDecorator<NamedEntityCorrections>(
                    new AbstractMarkingFilter<NamedEntityCorrections>() {
                        @Override
                        public boolean isMarkingGood(NamedEntityCorrections marking) {
                            return marking.getUserDecision() != DecisionValue.ADDED;
                        }
                    }, new MarkingFilteringEvaluatorDecorator<NamedEntityCorrections>(
                            new AbstractMarkingFilter<NamedEntityCorrections>() {
                                @Override
                                public boolean isMarkingGood(NamedEntityCorrections marking) {
                                    return ((marking.getError() != null) && (marking.getError().contains(errorType)));
                                }
                            }, new CorrectnessAccuracy())));
            return evaluators;
        }

        @SuppressWarnings("unchecked")
        public List<Evaluator<NamedEntityCorrections>> createEvaluator4UserAdditions(String name1, String name2) {
            List<Evaluator<NamedEntityCorrections>> evaluators = new ArrayList<Evaluator<NamedEntityCorrections>>();

            // The URIs are not interesting. It is sufficient to look at the
            // positions
            evaluators.add(new MarkingFilteringEvaluatorDecorator<NamedEntityCorrections>(
                    new AbstractMarkingFilter<NamedEntityCorrections>() {

                        @Override
                        public boolean isMarkingGood(NamedEntityCorrections marking) {
                            return marking.getUserDecision() == DecisionValue.ADDED;
                        }
                    },
                    new FMeasureCalculator<NamedEntityCorrections>(new MatchingsCounterImpl<NamedEntityCorrections>(
                            (MatchingsSearcher<NamedEntityCorrections>) MatchingsSearcherFactory
                                    .createSpanMatchingsSearcher(Matching.WEAK_ANNOTATION_MATCH)))));

            // The URIs are not interesting. It is sufficient to look at the
            // positions
            evaluators.add(new MarkingFilteringEvaluatorDecorator<NamedEntityCorrections>(
                    new AbstractMarkingFilter<NamedEntityCorrections>() {
                        @Override
                        public boolean isMarkingGood(NamedEntityCorrections marking) {
                            return marking.getUserDecision() == DecisionValue.ADDED;
                        }
                    },
                    new AddedMarkingsCountingEvaluator(new MatchingsCounterImpl<NamedEntityCorrections>(
                            (MatchingsSearcher<NamedEntityCorrections>) MatchingsSearcherFactory
                                    .createSpanMatchingsSearcher(Matching.WEAK_ANNOTATION_MATCH)))));

            return evaluators;
        }

        @SuppressWarnings("unchecked")
        @Deprecated
        public List<Evaluator<NamedEntityCorrections>> createEvaluator(String name1, String name2) {
            List<Evaluator<NamedEntityCorrections>> evaluators = new ArrayList<Evaluator<NamedEntityCorrections>>();

            MatchingsSearcher<ClassifiedEntityCheck> searcher = (MatchingsSearcher<ClassifiedEntityCheck>) MatchingsSearcherFactory
                    .createSpanMatchingsSearcher(Matching.STRONG_ANNOTATION_MATCH);
            evaluators.add(
                    new ClassifyingEvaluatorDecorator(new ClassConsideringFMeasureCalculator<ClassifiedEntityCheck>(
                            new MatchingsCounterImpl<ClassifiedEntityCheck>(
                                    new CompoundMatchingsSearcher<ClassifiedEntityCheck>(searcher,
                                            new ClassifiedMeaningMatchingsSearcher<ClassifiedEntityCheck>()
                                    // ,new
                                    // IsNamedEntityFlagBasedMatchingsSearcher<ClassifiedEntityCheck>()
                                    )), MarkingClasses.IN_KB, MarkingClasses.EE, MarkingClasses.GS_IN_KB), new UriBasedMeaningClassifier<ClassifiedEntityCheck>(this.globalClassifier, MarkingClasses.IN_KB), new EmergingEntityMeaningClassifier<ClassifiedEntityCheck>()));

            ExperimentTaskConfiguration subTaskConfig;
            subTaskConfig = new ExperimentTaskConfiguration(new ConfigurationImpl(name1, false, ExperimentType.ERec),
                    new ConfigurationImpl(name2, false, ExperimentType.ERec), ExperimentType.ERec,
                    Matching.STRONG_ANNOTATION_MATCH);
            evaluators.add(
                    new SubTaskEvaluator<>(subTaskConfig, createEvaluator(ExperimentType.ERec, subTaskConfig, null)));
            subTaskConfig = new ExperimentTaskConfiguration(new ConfigurationImpl(name1, false, ExperimentType.C2KB),
                    new ConfigurationImpl(name2, false, ExperimentType.C2KB), ExperimentType.C2KB,
                    Matching.STRONG_ENTITY_MATCH);
            evaluators.add(
                    new SubTaskEvaluator<>(subTaskConfig, createEvaluator(ExperimentType.C2KB, subTaskConfig, null)));
            return evaluators;
        }

    }

    protected class ConfigurationImpl extends AbstractAdapterConfiguration
            implements DatasetConfiguration, AnnotatorConfiguration {

        public ConfigurationImpl(String name, boolean couldBeCached, ExperimentType applicableForExperiment) {
            super(name, couldBeCached, applicableForExperiment);
        }

        @Override
        public Annotator getAnnotator(ExperimentType type) throws GerbilException {
            return null;
        }

        @Override
        public Dataset getDataset(ExperimentType experimentType) throws GerbilException {
            return null;
        }
    }
}
