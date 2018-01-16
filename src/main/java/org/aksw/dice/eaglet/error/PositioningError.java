package org.aksw.dice.eaglet.error;

import java.util.List;

import org.aksw.dice.eaglet.entitytypemodify.NamedEntityCorrections;
import org.aksw.dice.eaglet.entitytypemodify.NamedEntityCorrections.Check;
import org.aksw.dice.eaglet.entitytypemodify.NamedEntityCorrections.ErrorType;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PositioningError implements ErrorChecker {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(PositioningError.class);

	static String text;
	static String entity_name;

	public void positioningError(List<Document> documents)
			throws GerbilException {
		LOGGER.info(" POSITIONING MODULE RUNNING");

		// DATASET.getDataset(ExperimentType.A2KB).getInstances();

		for (Document doc : documents) { // getting list of documents
			text = doc.getText();
			List<NamedEntityCorrections> entities = doc
					.getMarkings(NamedEntityCorrections.class);
			for (NamedEntityCorrections entity : entities) {
				if (entity.getResult().equals(Check.GOOD)) {
					if (entity.getStartPosition() > 0) {
						if ((!Character.isWhitespace(text.charAt(entity
								.getStartPosition() - 1)))
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
							entity.setResult(Check.DELETED);
							entity.setError(ErrorType.WRONGPOSITION);
						}
					}
					// If there are letters behind check for letters or digits
					if (entity.getStartPosition() + entity.getLength() < text
							.length()) {
						if (Character.isLetterOrDigit(text.charAt(entity
								.getStartPosition() + entity.getLength()))) {
							entity.setResult(Check.DELETED);
							entity.setError(ErrorType.WRONGPOSITION);
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
