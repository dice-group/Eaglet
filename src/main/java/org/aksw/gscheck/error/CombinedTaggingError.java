package org.aksw.gscheck.error;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.data.StartPosBasedComparator;
import org.aksw.gscheck.corrections.NamedEntityCorrections;
import org.aksw.gscheck.corrections.NamedEntityCorrections.Check;
import org.aksw.simba.gscheck.documentprocessor.StanfordParsedMarking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;

public class CombinedTaggingError implements ErrorChecker {
	private static final Logger LOGGER = LoggerFactory.getLogger(CombinedTaggingError.class);

	public void CombinedTagger(List<Document> documents) throws GerbilException {

		LOGGER.info(" COMBINED TAGGER MODULE RUNNING");

		for (Document doc : documents) {
			String text = doc.getText();

			List<CoreLabel> eligible_makrings = Noun_Ad_Extracter(doc);
			List<NamedEntityCorrections> entities = doc.getMarkings(NamedEntityCorrections.class);
			Collections.sort(entities, new StartPosBasedComparator());
			if (entities.size() > 0) {
				for (int i = 1; i < entities.size(); ++i) {
					String substring;
					// make sure that the entities are not
					// overlapping
					if ((entities.get(i - 1).getStartPosition() + entities.get(i - 1).getLength()) <= entities.get(i)
							.getStartPosition()) {
						substring = text.substring(
								entities.get(i - 1).getStartPosition() + entities.get(i - 1).getLength(),
								entities.get(i).getStartPosition());
						if (substring.matches("[\\s]*")) {
							String[] arr = text
									.substring(entities.get(i - 1).getStartPosition(),
											entities.get(i).getStartPosition() + entities.get(i).getLength())
									.split(" ");
							for (String x : arr) {
								for (CoreLabel z : eligible_makrings) {
									if (z.get(TextAnnotation.class).equals(x)) {
										System.out.println(
												z.get(TextAnnotation.class) + " " + z.get(PartOfSpeechAnnotation.class)
														+ " " + z.beginPosition() + " " + z.endPosition());
									}
								}
							}

							entities.get(i).setResult(Check.NEED_TO_PAIR);
							entities.get(i).setPartner(entities.get(i - 1));
						}

					}
				}
			}
			eligible_makrings.clear();
		}

	}

	public List<CoreLabel> Noun_Ad_Extracter(Document doc) {
		List<CoreLabel> eligible_makrings = new ArrayList<CoreLabel>();

		List<StanfordParsedMarking> stanfordAnns = doc.getMarkings(StanfordParsedMarking.class);
		StanfordParsedMarking stanfordAnn = stanfordAnns.get(0);
		List<CoreLabel> tokens = stanfordAnn.getAnnotation().get(TokensAnnotation.class);
		if (stanfordAnns.size() != 1) {
			// TODO PANIC!!!!
			LOGGER.error(" Parser not working ");
		}

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

	@Override
	public void check(List<Document> documents) throws GerbilException {
		// TODO Auto-generated method stub
		this.CombinedTagger(documents);
	}
}
