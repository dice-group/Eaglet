package org.aksw.dice.eaglet.error;

import java.util.List;

import org.aksw.dice.eaglet.entitytypemodify.NamedEntityCorrections;
import org.aksw.dice.eaglet.entitytypemodify.NamedEntityCorrections.Correction;
import org.aksw.dice.eaglet.entitytypemodify.NamedEntityCorrections.ErrorType;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PositioningError implements ErrorChecker {
	private static final Logger LOGGER = LoggerFactory.getLogger(PositioningError.class);

	static String text;
	static String entity_name;

	public void positioningError(List<Document> documents) throws GerbilException {
		LOGGER.info(" POSITIONING MODULE RUNNING");
		for (Document doc : documents) { // getting list of documents
			text = doc.getText();
			List<NamedEntityCorrections> entities = doc.getMarkings(NamedEntityCorrections.class);
			for (NamedEntityCorrections entity : entities) {
				if (entity.getError().equals(ErrorType.NOERROR)) {
					if (entity.getStartPosition() > 0) {
						if ((!Character.isWhitespace(text.charAt(entity.getStartPosition() - 1)))
								&& (text.charAt(entity.getStartPosition() - 1) != '@')
								&& (text.charAt(entity.getStartPosition() - 1) != '\'')
								&& (text.charAt(entity.getStartPosition() - 1) != '\"')
								&& (text.charAt(entity.getStartPosition() - 1) != '#')
								&& (text.charAt(entity.getStartPosition() - 1) != '_')
								&& (text.charAt(entity.getStartPosition() - 1) != '*')
								&& (text.charAt(entity.getStartPosition() - 1) != '(')
								&& (text.charAt(entity.getStartPosition() - 1) != '{')
								&& (text.charAt(entity.getStartPosition() - 1) != '[')
								&& (text.charAt(entity.getStartPosition() - 1) != '$')
								&& (text.charAt(entity.getStartPosition()) != '@')
								&& (text.charAt(entity.getStartPosition()) != '\'')
								&& (text.charAt(entity.getStartPosition()) != '\"')
								&& (text.charAt(entity.getStartPosition()) != '#')
								&& (text.charAt(entity.getStartPosition()) != '_')
								&& (text.charAt(entity.getStartPosition()) != '*')
								&& (text.charAt(entity.getStartPosition()) != '(')
								&& (text.charAt(entity.getStartPosition()) != '{')
								&& (text.charAt(entity.getStartPosition()) != '[')
								&& (text.charAt(entity.getStartPosition()) != '$')) {
							entity.setCorrectionSuggested(Correction.DELETE);
							entity.setError(ErrorType.WRONGPOSITIONERR);
						}
					}
					// If there are letters behind check for letters or digits
					if (entity.getStartPosition() + entity.getLength() < text.length()) {
						if (Character.isLetterOrDigit(text.charAt(entity.getStartPosition() + entity.getLength()))) {
							entity.setCorrectionSuggested(Correction.DELETE);
							entity.setError(ErrorType.WRONGPOSITIONERR);
						}
					}
				}

			}
		}

	}

	@Override
	public void check(List<Document> documents) throws GerbilException {
		// TODO Auto-generated method stub
		this.positioningError(documents);

	}

}
