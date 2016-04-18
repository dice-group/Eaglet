package org.aksw.simba.eaglet.eval;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
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
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.io.nif.DocumentListParser;
import org.aksw.gerbil.io.nif.DocumentParser;
import org.aksw.gerbil.matching.Matching;
import org.aksw.gerbil.matching.MatchingsSearcher;
import org.aksw.gerbil.matching.MatchingsSearcherFactory;
import org.aksw.gerbil.matching.impl.ClassifiedMeaningMatchingsSearcher;
import org.aksw.gerbil.matching.impl.CompoundMatchingsSearcher;
import org.aksw.gerbil.matching.impl.MatchingsCounterImpl;
import org.aksw.gerbil.matching.impl.clas.EmergingEntityMeaningClassifier;
import org.aksw.gerbil.matching.impl.clas.UriBasedMeaningClassifier;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.NIFTransferPrefixMapping;
import org.aksw.simba.eaglet.annotator.AdaptedAnnotationParser;
import org.aksw.simba.eaglet.entitytypemodify.ClassifiedEntityCheck;
import org.aksw.simba.eaglet.entitytypemodify.EntityCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class InterRaterAgreement {

    private static final Logger LOGGER = LoggerFactory.getLogger(InterRaterAgreement.class);

    private static final int USER_IDS[] = new int[] { 1, 6 };
    private static final String USER_OUTPUT_FOLDER = "eaglet_data/result_user/KORE50";

    public static void main(String[] args) {
        InterRaterAgreement raterAgreement = new InterRaterAgreement();
        raterAgreement.run(USER_IDS, new File(USER_OUTPUT_FOLDER));
    }

    private ExtendedEvaluatorFactory factory = new ExtendedEvaluatorFactory();

    public void run(int[] userIds, File userOutputFolder) {
        List<List<List<EntityCheck>>> annotations = loadAnnotations(userIds, userOutputFolder);
        List<Evaluator<EntityCheck>> evaluators;
        EvaluationResultContainer result;
        for (int i = 0; i < userIds.length; ++i) {
            for (int j = i + 1; j < userIds.length; ++j) {
                LOGGER.info("Comparing user {} and {}.", userIds[i], userIds[j]);
                evaluators = factory.createEvaluator(Integer.toString(userIds[i]), Integer.toString(userIds[j]));
                result = (EvaluationResultContainer) evaluate(evaluators, annotations.get(i), annotations.get(j));
                System.out.println("user #" + userIds[i] + " vs. #" + userIds[j]);
                printResult(result);
            }
        }
    }

    protected EvaluationResult evaluate(List<Evaluator<EntityCheck>> evaluators,
            List<List<EntityCheck>> annotatorResults, List<List<EntityCheck>> goldStandard) {
        EvaluationResultContainer evalResults = new EvaluationResultContainer();
        for (Evaluator<EntityCheck> e : evaluators) {
            ((Evaluator<EntityCheck>) e).evaluate(annotatorResults, goldStandard, evalResults);
        }
        return evalResults;
    }

    protected List<List<List<EntityCheck>>> loadAnnotations(int[] userIds, File userOutputFolder) {
        List<List<Document>> documents = loadDocuments(userIds, userOutputFolder);
        List<Map<String, List<EntityCheck>>> documentAnnotations = new ArrayList<Map<String, List<EntityCheck>>>(
                documents.size());
        Set<String> uris = generateMappings(documents, documentAnnotations);
        List<List<List<EntityCheck>>> annotations = new ArrayList<>(documents.size());
        documents = null;
        List<List<EntityCheck>> userAnnotations;
        Map<String, List<EntityCheck>> uriAnnotationMap;
        List<EntityCheck> emptyDocument = new ArrayList<>(0);
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
            List<Map<String, List<EntityCheck>>> documentAnnotations) {
        Map<String, List<EntityCheck>> uriAnnotationMap;
        Set<String> uris = new HashSet<String>();
        for (List<Document> userDocuments : documents) {
            uriAnnotationMap = new HashMap<String, List<EntityCheck>>();
            documentAnnotations.add(uriAnnotationMap);
            for (Document document : userDocuments) {
                uriAnnotationMap.put(document.getDocumentURI(), document.getMarkings(EntityCheck.class));
                uris.add(document.getDocumentURI());
            }
        }
        return uris;
    }

    protected static List<List<Document>> loadDocuments(int userIds[], File userOutputFolder) {
        List<List<Document>> loadedDocuments = new ArrayList<List<Document>>();
        List<Document> userDocuments = null;
        String expectedPrefix;
        for (int i = 0; i < userIds.length; ++i) {
            userDocuments = new ArrayList<Document>();
            expectedPrefix = "result-" + userIds[i];
            for (File file : userOutputFolder.listFiles()) {
                if (file.getName().startsWith(expectedPrefix))
                    try {
                        Model nifModel = ModelFactory.createDefaultModel();
                        nifModel.setNsPrefixes(NIFTransferPrefixMapping.getInstance());
                        FileInputStream fin = new FileInputStream(file);
                        nifModel.read(fin, "", "TTL");
                        fin.close();
                        DocumentListParser parser = new DocumentListParser(
                                new DocumentParser(new AdaptedAnnotationParser()));
                        userDocuments.addAll(parser.parseDocuments(nifModel));
                    } catch (Exception e) {
                        LOGGER.error("Couldn't load the dataset!", e);
                    }
            }
            loadedDocuments.add(userDocuments);
        }
        return loadedDocuments;
    }

    protected void printResult(EvaluationResult result) {
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
        public List<Evaluator<EntityCheck>> createEvaluator(String name1, String name2) {
            List<Evaluator<EntityCheck>> evaluators = new ArrayList<Evaluator<EntityCheck>>();

            MatchingsSearcher<ClassifiedEntityCheck> searcher = (MatchingsSearcher<ClassifiedEntityCheck>) MatchingsSearcherFactory
                    .createSpanMatchingsSearcher(Matching.STRONG_ANNOTATION_MATCH);
            evaluators.add(new ClassifyingEvaluatorDecorator(
                    new ClassConsideringFMeasureCalculator<ClassifiedEntityCheck>(
                            new MatchingsCounterImpl<ClassifiedEntityCheck>(
                                    new CompoundMatchingsSearcher<ClassifiedEntityCheck>(searcher,
                                            new ClassifiedMeaningMatchingsSearcher<ClassifiedEntityCheck>(),
                                            new IsNamedEntityFlagBasedMatchingsSearcher<ClassifiedEntityCheck>())),
                            MarkingClasses.IN_KB, MarkingClasses.EE, MarkingClasses.GS_IN_KB),
                    new UriBasedMeaningClassifier<ClassifiedEntityCheck>(this.globalClassifier, MarkingClasses.IN_KB),
                    new EmergingEntityMeaningClassifier<ClassifiedEntityCheck>()));

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
