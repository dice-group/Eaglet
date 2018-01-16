package org.aksw.dice.eaglet.annotator;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.aksw.dice.eaglet.entitytypemodify.NamedEntityCorrections;
import org.aksw.gerbil.annotator.A2KBAnnotator;
import org.aksw.gerbil.dataset.Dataset;
import org.aksw.gerbil.dataset.impl.nif.NIFFileDatasetConfig;
import org.aksw.gerbil.datatypes.ErrorTypes;
import org.aksw.gerbil.datatypes.ExperimentType;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.io.nif.NIFParser;
import org.aksw.gerbil.io.nif.impl.TurtleNIFParser;
import org.aksw.gerbil.transfer.nif.Document;
import org.apache.commons.io.IOUtils;

/**
 * The class that defines the annotator result.
 *
 * @author Kunal
 * @author Michael
 */
public class AnnotatorResult {

    private static final ExperimentType EXPERIMENT_TYPE = ExperimentType.A2KB;
    private List<A2KBAnnotator> annotators = new ArrayList<A2KBAnnotator>();
    String filename;

    /**
     * Constructor
     *
     * @param Filename
     * @throws GerbilException
     */
    public AnnotatorResult(String Filename) throws GerbilException {
        this.filename = Filename;
        readannotatorlist();

    }

    public List<A2KBAnnotator> getAnnotators() {
        return annotators;
    }

    public void setAnnotators(List<A2KBAnnotator> annotators) {
        this.annotators = annotators;
    }

    /**
     * The method reads all the A2KB annotators.
     *
     * @throws GerbilException
     */
    public void readannotatorlist() throws GerbilException {
        File folder = new File(filename);
        File[] listOfFiles = folder.listFiles();
        for (File file : listOfFiles) {
            if (file.isFile()) {
                String annotatorFilenName = file.getAbsolutePath();
                annotators.add(AnnotatorResult.loadAnnotator(annotatorFilenName, file.getName()));
            }
        }
    }

    @Deprecated
    public static List<NamedEntityCorrections> loadAnnotatorResult(String annotatorFileName, String AnnotatorName)
            throws GerbilException {
        Dataset dataset = (new NIFFileDatasetConfig("ANNOTATOR", annotatorFileName, false, EXPERIMENT_TYPE, null, null))
                .getDataset(EXPERIMENT_TYPE);
        ArrayList<NamedEntityCorrections> entity_set = new ArrayList<NamedEntityCorrections>();
        List<Document> documents = dataset.getInstances();
        for (Document doc : documents) {
            List<NamedEntityCorrections> entities = doc.getMarkings(NamedEntityCorrections.class);
            entity_set.addAll(entities);
        }
        return entity_set;
    }

    public static A2KBAnnotator loadAnnotator(String annotatorFileName, String AnnotatorName) throws GerbilException {
        List<Document> documents;
        if (annotatorFileName.endsWith(".gz")) {
            documents = readFromGzippedFile(annotatorFileName);
        } else {
            Dataset dataset = (new NIFFileDatasetConfig("ANNOTATOR", annotatorFileName, false, EXPERIMENT_TYPE, null, null))
                    .getDataset(EXPERIMENT_TYPE);
            documents = dataset.getInstances();
        }
        return new TestA2KBAnnotator(documents);
    }

    protected static List<Document> readFromGzippedFile(String annotatorFileName) throws GerbilException {
        NIFParser parser = new TurtleNIFParser();
        FileInputStream fin = null;
        GZIPInputStream gin = null;
        try {
            fin = new FileInputStream(annotatorFileName);
            gin = new GZIPInputStream(fin);
            return parser.parseNIF(gin);
        } catch (Exception e) {
            throw new GerbilException("Couldn't read gzipped annotator file.", e, ErrorTypes.DATASET_LOADING_ERROR);
        } finally {
            IOUtils.closeQuietly(gin);
            IOUtils.closeQuietly(fin);
        }
    }

}