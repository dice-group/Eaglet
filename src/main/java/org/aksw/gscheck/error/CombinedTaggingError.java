package org.aksw.gscheck.error;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.aksw.gerbil.dataset.DatasetConfiguration;
import org.aksw.gerbil.dataset.impl.nif.NIFFileDatasetConfig;
import org.aksw.gerbil.datatypes.ExperimentType;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.data.NamedEntity;
import org.aksw.gerbil.transfer.nif.data.StartPosBasedComparator;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.util.Properties;

public class CombinedTaggingError {
	private static final DatasetConfiguration DATASET = new NIFFileDatasetConfig("DBpedia",
			"gerbil_data/datasets/spotlight/dbpedia-spotlight-nif.ttl", false, ExperimentType.A2KB);

	public static void main(String[] args) throws GerbilException {
		List<Document> documents = DATASET.getDataset(ExperimentType.A2KB).getInstances();
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		String substring;
		List<NamedEntity> entities;
		NamedEntity a, b;
		for (Document doc : documents) {
			String text = doc.getText();

			Annotation document = new Annotation(text);
			pipeline.annotate(document);

			List<CoreLabel> tokens = document.get(TokensAnnotation.class);
			int lastEnd = -1;
			for (CoreLabel token : tokens) {
				// we can get the token that has been marked inside the text
				System.out.println(token.get(TextAnnotation.class));
				// this is its position
				System.out.println(token.get(PartOfSpeechAnnotation.class));
				// this is its lemma
				System.out.println(token.get(LemmaAnnotation.class));
			}
			/*
			 * LexicalizedParser lp = LexicalizedParser.loadModel(
			 * "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
			 * lp.setOptionFlags(new String[] { "-maxLength", "80",
			 * "-retainTmpSubcategories" }); // String sent=
			 * "Ohio is located in America"; Tree parse; parse = (Tree)
			 * lp.parse(text);
			 * 
			 * List<Tree> phraseList=new ArrayList<Tree>(); for (Tree subtree:
			 * parse) {
			 * 
			 * if(subtree.label().value().startsWith("N")) {subtree.getSpan().
			 * 
			 * phraseList.add(subtree); System.out.println(subtree);
			 * 
			 * } }
			 */

			// List taggedWords = parse.taggedYield();

			// System.out.println(taggedWords);

			/*
			 * entities = doc.getMarkings(NamedEntity.class);
			 * Collections.sort(entities, new StartPosBasedComparator()); if
			 * (entities.size() > 0) { b = entities.get(0); for (int i = 1; i <
			 * entities.size(); ++i) { a = b; b = entities.get(i); // make sure
			 * that the entities are not overlapping if ((a.getStartPosition() +
			 * a.getLength()) <= b.getStartPosition()) { substring =
			 * text.substring(a.getStartPosition() + a.getLength(),
			 * b.getStartPosition()); if (substring.matches("[\\s]*")) {
			 * System.out.println(
			 * "I would connect two entities to a single large entity \"" +
			 * text.substring(a.getStartPosition(), b.getStartPosition() +
			 * b.getLength()) + "\"."); } } } }
			 */
		}

	}
}
