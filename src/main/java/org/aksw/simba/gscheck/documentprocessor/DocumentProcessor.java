package org.aksw.simba.gscheck.documentprocessor;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.aksw.gerbil.transfer.nif.Document;


import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class DocumentProcessor {
	protected StanfordCoreNLP pipeline;
	List<String> lemmas = new LinkedList<String>();
	List<Integer> position = new LinkedList<Integer>();

	public DocumentProcessor() {
		// Create StanfordCoreNLP object properties, with POS tagging
		// (required for lemmatization), and lemmatization

		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma");

		this.pipeline = new StanfordCoreNLP(props);

	}

	public void process(List<Document> docs)
	{
		for (Document doc : docs) 
		{
			String documentText = doc.getText();
			Annotation document = new Annotation(documentText);
			// run all Annotators on this text
			this.pipeline.annotate(document);
			doc.addMarking(new StanfordParsedMarking(document));
		}
			
			// StanfordParsedMarking stanfordAnn = stanfordAnns.get(0);
			// stanfordAnn.getAnnotation().get(SentenceAnnotation.)

			/*
			 * List<StanfordParsedMarking> stanfordAnns =
			 * doc.getMarkings(StanfordParsedMarking.class);
			 * if(stanfordAnns.size() != 1) { // TODO PANIC!!!! }
			 */
		

	}

/*
	public String lemmatize_entity(String entity) {

		List<NamedEntityCorrections> lemma_list = new LinkedList<NamedEntityCorrections>();
		// Create an empty Annotation just with the given text
		String result = null;
		Annotation document = new Annotation(entity);
		// run all Annotators on this text
		this.pipeline.annotate(document);
		// Iterate over all of the sentences found
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		for (CoreMap sentence : sentences) {
			// Iterate over all tokens in a sentence
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				// Retrieve and add the lemma for each word into the
				// list of lemmas
				result += token.get(LemmaAnnotation.class);
				// position.add(token.beginPosition());
			}

		}
		result = result.replaceAll("null", "");
		return result;
	}


*/
}