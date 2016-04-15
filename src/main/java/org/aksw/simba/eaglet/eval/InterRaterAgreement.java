package org.aksw.simba.eaglet.eval;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.aksw.gerbil.annotator.Annotator;
import org.aksw.gerbil.annotator.AnnotatorConfiguration;
import org.aksw.gerbil.dataset.Dataset;
import org.aksw.gerbil.dataset.DatasetConfiguration;
import org.aksw.gerbil.datatypes.AbstractAdapterConfiguration;
import org.aksw.gerbil.datatypes.ExperimentTaskConfiguration;
import org.aksw.gerbil.datatypes.ExperimentType;
import org.aksw.gerbil.datatypes.marking.ClassifiedSpanMeaning;
import org.aksw.gerbil.evaluate.Evaluator;
import org.aksw.gerbil.evaluate.EvaluatorFactory;
import org.aksw.gerbil.evaluate.SubTaskEvaluator;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.io.nif.DocumentListParser;
import org.aksw.gerbil.io.nif.DocumentParser;
import org.aksw.gerbil.matching.Matching;
import org.aksw.gerbil.matching.MatchingsSearcher;
import org.aksw.gerbil.matching.MatchingsSearcherFactory;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.NIFTransferPrefixMapping;
import org.aksw.simba.eaglet.annotator.AdaptedAnnotationParser;
import org.aksw.simba.eaglet.entitytypemodify.EntityCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class InterRaterAgreement {

    private static final Logger LOGGER = LoggerFactory.getLogger(InterRaterAgreement.class);

    private static final int USER_IDS[] = new int[] { 1, 6 };
    private static final String USER_OUTPUT_FOLDER = "eaglet_data/result_user";

    public static void main(String[] args) {
        InterRaterAgreement raterAgreement = new InterRaterAgreement();
        raterAgreement.run(USER_IDS, new File(USER_OUTPUT_FOLDER));
    }

    private ExtendedEvaluatorFactory factory = new ExtendedEvaluatorFactory();

    public void run(int[] userIds, File userOutputFolder) {
        List<List<Document>> documents = loadDocuments(userIds, userOutputFolder);
        for (int i = 0; i < userIds.length; ++i) {
            for (int j = i + 1; j < userIds.length; ++j) {
                LOGGER.info("Comparing user {} and {}.", userIds[i], userIds[j]);
                evaluate(documents.get(i), documents.get(j));
            }
        }
    }

    protected List<List<Document>> loadDocuments(int userIds[], File userOutputFolder) {
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

    private void evaluate(List<Document> list, List<Document> list2) {

    }

    protected class ExtendedEvaluatorFactory extends EvaluatorFactory {

        @SuppressWarnings("unchecked")
        private List<Evaluator<EntityCheck>> createEvaluator(String name1, String name2) {
            List<Evaluator<EntityCheck>> evaluators = new ArrayList<Evaluator<EntityCheck>>();

            MatchingsSearcher<ClassifiedSpanMeaning> searcher = (MatchingsSearcher<ClassifiedSpanMeaning>) MatchingsSearcherFactory
                    .createSpanMatchingsSearcher(Matching.STRONG_ANNOTATION_MATCH);
            // evaluators.add(new ClassifyingEvaluatorDecorator<MeaningSpan,
            // ClassifiedSpanMeaning>(
            // new ClassConsideringFMeasureCalculator<ClassifiedSpanMeaning>(
            // new MatchingsCounterImpl<ClassifiedSpanMeaning>(
            // new CompoundMatchingsSearcher<ClassifiedSpanMeaning>(searcher,
            // new
            // ClassifiedMeaningMatchingsSearcher<ClassifiedSpanMeaning>())),
            // MarkingClasses.IN_KB, MarkingClasses.EE,
            // MarkingClasses.GS_IN_KB),
            // new
            // UriBasedMeaningClassifier<ClassifiedSpanMeaning>(this.globalClassifier,
            // MarkingClasses.IN_KB),
            // new EmergingEntityMeaningClassifier<ClassifiedSpanMeaning>()));

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
