package org.aksw.dice.eaglet.error;

import java.util.Collections;
import java.util.List;

import org.aksw.dice.eaglet.entitytypemodify.NamedEntityCorrections;
import org.aksw.dice.eaglet.entitytypemodify.NamedEntityCorrections.Correction;
import org.aksw.dice.eaglet.entitytypemodify.NamedEntityCorrections.ErrorType;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.data.StartPosBasedComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles the Combined Tagging Error.
 *
 * @author Kunal
 * @author Michael
 *
 */

public class CombinedTaggingError implements ErrorChecker {
	/** Value - {@value} , LOGGER used for log information. */
	private static final Logger LOGGER = LoggerFactory.getLogger(CombinedTaggingError.class);

	/**
	 * The method checks for possible entities that are more appropriate when
	 * combined together.
	 *
	 * @param documents
	 *            : A List of Documents passing through the pipeline.
	 * @throws GerbilException
	 */
	public void CombinedTagger(List<Document> documents) throws GerbilException {

		LOGGER.info(" COMBINED TAGGER MODULE RUNNING");

		for (Document doc : documents) {
			String text = doc.getText();
			List<NamedEntityCorrections> entities = doc.getMarkings(NamedEntityCorrections.class);
			Collections.sort(entities, new StartPosBasedComparator());
			if (entities.size() > 0) {
				for (int i = 1; i < entities.size(); ++i) {
					if (entities.get(i).getError().equals(ErrorType.NOERROR)) {
						String substring;
						if ((entities.get(i - 1).getStartPosition() + entities.get(i - 1).getLength()) <= entities
								.get(i).getStartPosition()) {
							substring = text.substring(
									entities.get(i - 1).getStartPosition() + entities.get(i - 1).getLength(),
									entities.get(i).getStartPosition());
							if (substring.matches("[\\s]*")) {
								entities.get(i).setError(ErrorType.COMBINEDTAGGINGERR);
								entities.get(i).setCorrectionSuggested(Correction.CHECK);
								entities.get(i).setPartner(entities.get(i - 1));
								entities.get(i - 1).setError(ErrorType.COMBINEDTAGGINGERR);
								entities.get(i - 1).setCorrectionSuggested(Correction.CHECK);
							}

						}
					}
				}
			}
		}
	}

	/**
	 * Check interface to pass on the list of documents
	 */
	@Override
	public void check(List<Document> documents) throws GerbilException {
		// TODO Auto-generated method stub
		this.CombinedTagger(documents);
	}
}
