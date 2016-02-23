package org.aksw.gscheck.errorutils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.aksw.gerbil.transfer.nif.Document;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

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

	public List<Problem_Entity> lemmatize(Document doc) {
		String documentText = doc.getText();
		List<Problem_Entity> lemma_list = new LinkedList<Problem_Entity>();
		// Create an empty Annotation just with the given text
		Annotation document = new Annotation(documentText);
		// run all Annotators on this text
		this.pipeline.annotate(document);
		// Iterate over all of the sentences found
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		for (CoreMap sentence : sentences) {
			// Iterate over all tokens in a sentence
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				// Retrieve and add the lemma for each word into the
				// list of lemmas
				Problem_Entity dummy = new Problem_Entity(token.beginPosition(),
						token.endPosition() - token.beginPosition(), null);
				dummy.setEntity_name(token.get(LemmaAnnotation.class));

				dummy.setEnd_pos(token.endPosition());

				dummy.setDoc(doc.getDocumentURI());
				if (dummy.getLength() != 0)
					lemma_list.add(dummy);

			}

		}

		return lemma_list;
	}

	public String lemmatize_entity(String entity) {

		List<Problem_Entity> lemma_list = new LinkedList<Problem_Entity>();
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

	public List<CoreLabel> Noun_Ad_Extracter(String text) {
		List<CoreLabel> eligible_makrings = new ArrayList<CoreLabel>();

		Annotation document = new Annotation(text);
		pipeline.annotate(document);

		List<CoreLabel> tokens = document.get(TokensAnnotation.class);
		for (CoreLabel token : tokens) {
			// we can get the token that has been marked inside the text
			if (token.get(PartOfSpeechAnnotation.class).startsWith("N")
					&& (!token.get(PartOfSpeechAnnotation.class).equals("NP"))
					|| (token.get(PartOfSpeechAnnotation.class).startsWith("JJ"))) {
				eligible_makrings.add(token);

			}
		}

		return eligible_makrings;
	}

	public List<CoreLabel> LongEntity_Extracter_util(String text) {
		List<CoreLabel> POS_Blacklist = new ArrayList<CoreLabel>();

		Annotation document = new Annotation(text);
		pipeline.annotate(document);

		List<CoreLabel> tokens = document.get(TokensAnnotation.class);
		for (CoreLabel token : tokens) {
			// we can get the token that has been marked inside the text

			// System.out.println(token.get(PartOfSpeechAnnotation.class));
			// System.out.print(token.get(TextAnnotation.class));
			// System.out.print("??????");
			if ((token.get(PartOfSpeechAnnotation.class).equals("VBP"))
					|| (token.get(PartOfSpeechAnnotation.class).equals("VBZ")
							|| (token.get(PartOfSpeechAnnotation.class).equals("WDT"))
							|| (token.get(PartOfSpeechAnnotation.class).equals("PRP"))
							|| (token.get(PartOfSpeechAnnotation.class).equals("MD")))) {
				POS_Blacklist.add(token);

			}
		}

		return POS_Blacklist;
	}
}