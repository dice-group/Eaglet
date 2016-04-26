package org.aksw.simba.eaglet.errorcheckpipeline;

import java.io.IOException;
import java.util.List;

import org.aksw.gerbil.dataset.Dataset;
import org.aksw.gerbil.dataset.DatasetConfiguration;
import org.aksw.gerbil.dataset.impl.msnbc.MSNBCDataset;
import org.aksw.gerbil.dataset.impl.nif.NIFFileDatasetConfig;
import org.aksw.gerbil.datatypes.ExperimentType;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.web.config.AdapterList;
import org.aksw.gerbil.web.config.AdapterManager;
import org.aksw.gerbil.web.config.DatasetsConfig;
import org.aksw.gerbil.web.config.RootConfig;
import org.aksw.simba.eaglet.documentprocessor.DocumentProcessor;
import org.aksw.simba.eaglet.entitytypemodify.EntityTypeChange;

public class InputforPipeline {
    // private String name =
    // "eaglet_data/gerbil_data/datasets/spotlight/dbpedia-spotlight-nif.ttl";
    // private DatasetConfiguration DATASET = new NIFFileDatasetConfig("KORE50",
    // name, false, ExperimentType.A2KB);

    public InputforPipeline() throws GerbilException, IOException {
        AdapterList<DatasetConfiguration> datasetAdapters = DatasetsConfig.datasets(null, null);
        List<DatasetConfiguration> datasetConfigs = datasetAdapters.getAdaptersForExperiment(ExperimentType.A2KB);

        for (DatasetConfiguration datasetConfig : datasetConfigs) {
            Dataset dataset = datasetConfig.getDataset(ExperimentType.A2KB);
            List<Document> documents = dataset.getInstances();
            PrePipeProcessor(documents);
          
            callPipe(documents, dataset.getName());
        }

    }

    public void PrePipeProcessor(List<Document> documents) throws GerbilException {
        for (Document doc : documents) {
            List<Marking> list = EntityTypeChange.changeType(doc);
            doc.setMarkings(list);
        }
        DocumentProcessor dp = new DocumentProcessor();
        dp.process(documents);
    }

    public void callPipe(List<Document> doc, String datasetname) throws GerbilException, IOException {
        CheckerPipeline.startPipe(doc, datasetname);
    }

    public static void main(String[] args) throws GerbilException, IOException {
        new InputforPipeline();
        // CheckerPipeline.callAnnotator("eaglet_data/Results_anontator_dbpedia/Kore50");

    }
}
