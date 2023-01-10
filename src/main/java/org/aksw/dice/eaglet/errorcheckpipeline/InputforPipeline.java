package org.aksw.dice.eaglet.errorcheckpipeline;

/**
 * <h1>EAGLET</h1>
 * <p>The tool allows user to the pass their gold standard through a series of
 * checks allowing them to generate better NER/NED gold standard dataset.
 * The tool has a pipeline structure for processing of documents.
 * </p>
 * @author  Kunal Jha
 * @author  Michael Röder
 * @version 1.0
 * @since   2016-05-31
 */

import java.io.IOException;
import java.util.List;

import org.aksw.dice.eaglet.documentprocessor.DocumentProcessor;
import org.aksw.dice.eaglet.entitytypemodify.EntityTypeChange;
import org.aksw.dice.eaglet.new_gerdaq.GERDAQDataset;
import org.aksw.gerbil.dataset.Dataset;
import org.aksw.gerbil.dataset.DatasetConfiguration;
import org.aksw.gerbil.datatypes.ExperimentType;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.web.config.AdapterList;
import org.aksw.gerbil.web.config.DatasetsConfig;

/**
 * <p>
 * Main Class.
 * </p>
 *
 * <p>
 * This class is used to generate the input for the pipeline. It takes the
 * datasets and parses it through StanfordNLP tool in order to generate the
 * required lemmas and POS tagging for further use in the pipeline.
 * </p>
 *
 * @author Kunal
 * @author Michael
 */
public class InputforPipeline {

	/**
	 * Constructor
	 *
	 * @throws GerbilException
	 * @throws IOException
	 */
	public InputforPipeline() throws GerbilException, IOException {
		AdapterList<DatasetConfiguration> datasetAdapters = DatasetsConfig
				.datasets(null, null);

		List<DatasetConfiguration> datasetConfigs = datasetAdapters
				.getAdaptersForExperiment(ExperimentType.A2KB);

		for (DatasetConfiguration datasetConfig : datasetConfigs) {
			Dataset dataset = datasetConfig.getDataset(ExperimentType.A2KB);
			List<Document> documents = dataset.getInstances();

			this.prePipeProcessor(documents);
			// Starting the Pipe.
			this.startPipe(documents, dataset.getName());
		}

	}

	public InputforPipeline(String name, String path) throws GerbilException,
			IOException {

		GERDAQDataset DATASET = new GERDAQDataset(path);
		DATASET.setName("name");
		DATASET.init();

		List<Document> documents = DATASET.getInstances();

		this.prePipeProcessor(documents);
		// Starting the Pipe.
		this.startPipe(documents, name);

	}

	public InputforPipeline(List<Document> documents, String outPath)
			throws GerbilException, IOException {

		this.prePipeProcessor(documents);
		CheckerPipeline cp = new CheckerPipeline();

		cp.runPipeAfterEval(documents, outPath);

	}

	/**
	 * <p>
	 * This method performs the processing the documents of the input Gold
	 * Standard before passing it through the error checkers.
	 * </p>
	 *
	 * @param documents
	 */
	public void prePipeProcessor(List<Document> documents)
			throws GerbilException {
		for (Document doc : documents) {
			List<Marking> list = EntityTypeChange.changeType(doc);
			doc.setMarkings(list);
		}
		DocumentProcessor dp = new DocumentProcessor();
		dp.process(documents);
	}

	/**
	 * This method starts the checker pipeline.
	 *
	 * @param doc
	 * @param datasetname
	 */
	public void startPipe(List<Document> doc, String datasetname)
			throws GerbilException, IOException {
		CheckerPipeline cp = new CheckerPipeline();
		cp.runPipe(doc, datasetname);

	}

	
}
