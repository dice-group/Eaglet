package org.aksw.dice.eaglet.error;

import java.util.List;

import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;

/**
 * The interface is a wrapper for the errors in the pipeline.
 *
 * @author Kunal
 * @author Michael
 */
public interface ErrorChecker {
	/**
	 * This method passes a list of pre- processed documents for all the errors
	 * in the flow of the pipe.
	 *
	 * @param documents
	 * @throws GerbilException
	 */
	public void check(List<Document> documents) throws GerbilException;
}
