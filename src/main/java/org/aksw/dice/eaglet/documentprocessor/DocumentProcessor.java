package org.aksw.dice.eaglet.documentprocessor;

import java.util.List;
import java.util.Properties;

import org.aksw.gerbil.transfer.nif.Document;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

/**
 * The class to generate with POS tagging and lemmatization.
 *
 * @author Kunal
 * @author Michael
 *
 */
public class DocumentProcessor {
	protected StanfordCoreNLP pipeline;


	/**
	 * Constructor
	 */
	public DocumentProcessor() {
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma");
		this.pipeline = new StanfordCoreNLP(props);
	}

	/**
	 * The implementing the process.
	 *
	 * @param docs
	 *            : list of Documents
	 */

	public void process(List<Document> docs) {
		for (Document doc : docs) {
			String documentText = doc.getText();
			Annotation document = new Annotation(documentText);
			this.pipeline.annotate(document);
			doc.addMarking(new StanfordParsedMarking(document));
		}
	}
}