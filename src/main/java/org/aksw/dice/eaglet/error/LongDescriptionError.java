package org.aksw.dice.eaglet.error;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.aksw.dice.eaglet.documentprocessor.DocumentProcessor;
import org.aksw.dice.eaglet.documentprocessor.StanfordParsedMarking;
import org.aksw.dice.eaglet.entitytypemodify.NamedEntityCorrections;
import org.aksw.dice.eaglet.entitytypemodify.NamedEntityCorrections.Correction;
import org.aksw.dice.eaglet.entitytypemodify.NamedEntityCorrections.ErrorType;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.data.StartPosBasedComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;

/**
 * This class handles the Long Description Error.
 *
 * @author Kunal
 * @author Michael
 *
 */

public class LongDescriptionError implements ErrorChecker {
	/** Value - {@value} , LOGGER used for log information. */
	private static final Logger LOGGER = LoggerFactory.getLogger(LongDescriptionError.class);

	public DocumentProcessor dp = new DocumentProcessor();

	/**
	 * This method identifies entities containing text not really required for
	 * annotations.
	 *
	 * @param documents
	 *            : A List of Documents passing through the pipeline.
	 * @throws GerbilException
	 */
	public void LongDescription(List<Document> documents) throws GerbilException {
		LOGGER.info("LONG DESCRIPTION MODULE RUNNING");
		for (Document doc : documents) {
			String text = doc.getText();
			List<NamedEntityCorrections> entities = doc.getMarkings(NamedEntityCorrections.class);
			List<CoreLabel> POSBlackList = LongEntity_Extracter_util(doc);
			Collections.sort(entities, new StartPosBasedComparator());
			for (NamedEntityCorrections entity : entities) {
				if (entity.getError().equals(ErrorType.NOERROR)) {
					String entity_text = text.substring(entity.getStartPosition(),
							entity.getLength() + entity.getStartPosition());
					String[] arr = entity_text.split(" ");
					for (String dummy : arr) {
						for (CoreLabel bentity : POSBlackList) {
							if (bentity.get(TextAnnotation.class).equals(dummy)) {
								entity.setCorrectionSuggested(Correction.DELETE);
								entity.setError(ErrorType.LONGDESCERR);

							}
						}
					}

				}

			}
		}

	}

	/**
	 * This method is a utility method for the LongDescription Error and helps in
	 * identifying the not required marked terms in the annotation.
	 *
	 * @param doc
	 *            : A single document to be analzsed.
	 * @return List of CoreLabels(Stanford NLP)
	 */
	public List<CoreLabel> LongEntity_Extracter_util(Document doc) {
		List<CoreLabel> POS_Blacklist = new ArrayList<CoreLabel>();
		List<StanfordParsedMarking> stanfordAnns = doc.getMarkings(StanfordParsedMarking.class);
		StanfordParsedMarking stanfordAnn = stanfordAnns.get(0);
		List<CoreLabel> tokens = stanfordAnn.getAnnotation().get(TokensAnnotation.class);
		if (stanfordAnns.size() != 1) {
			LOGGER.error(" Parser not working ");
		}
		for (CoreLabel token : tokens) {
			if (((token.get(PartOfSpeechAnnotation.class).equals("WDT"))
					|| (token.get(PartOfSpeechAnnotation.class).equals("MD")))) {
				POS_Blacklist.add(token);
			}
		}
		return POS_Blacklist;
	}

	/**
	 * The interface method to pass the Documents through the pipeline.
	 */
	@Override
	public void check(List<Document> documents) throws GerbilException {
		// TODO Auto-generated method stub
		this.LongDescription(documents);
	}

}
