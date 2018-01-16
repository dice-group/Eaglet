package org.aksw.dice.eaglet.error;

import java.util.Collections;
import java.util.List;

import org.aksw.dice.eaglet.entitytypemodify.NamedEntityCorrections;
import org.aksw.dice.eaglet.entitytypemodify.NamedEntityCorrections.Check;
import org.aksw.dice.eaglet.entitytypemodify.NamedEntityCorrections.ErrorType;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.data.StartPosBasedComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles the OverLapping Error.
 *
 * @author Kunal
 * @author Michael
 *
 */
public class OverLappingError implements ErrorChecker {
	/** Value - {@value} , LOGGER used for log information. */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(OverLappingError.class);

	/**
	 * The method checks for multiple markings coincide with other marking(s).
	 *
	 * @param documents
	 *            : A List of Documents passing through the pipeline.
	 * @throws GerbilException
	 */
	public void overLapCheck(List<Document> documents) throws GerbilException {
		LOGGER.info(" OVERLAPPING ENTITY MODULE RUNNING");
		for (Document doc : documents) {
			List<NamedEntityCorrections> entities = doc
					.getMarkings(NamedEntityCorrections.class);
			Collections.sort(entities, new StartPosBasedComparator());
			for (int i = 0; i < entities.size() - 1; i++) {
				if (entities.get(i).getResult().equals(Check.GOOD)) {
					if ((entities.get(i).getStartPosition() + entities.get(i)
							.getLength()) >= entities.get(i + 1)
							.getStartPosition()) {
						entities.get(i).setResult(Check.OVERLAPS);
						entities.get(i).setError(ErrorType.OVERLAPPING);
						entities.get(i).setPartner(entities.get(i + 1));
						entities.get(i + 1).setResult(Check.OVERLAPS);
						entities.get(i + 1).setError(ErrorType.OVERLAPPING);
					}
				}

			}
		}
	}

	/**
	 * The interface method to pass the Documents through the pipeline.
	 */
	@Override
	public void check(List<Document> documents) throws GerbilException {
		this.overLapCheck(documents);

	}

}
