/**
 * This file is part of General Entity Annotator Benchmark.
 *
 * General Entity Annotator Benchmark is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * General Entity Annotator Benchmark is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with General Entity Annotator Benchmark.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.aksw.dice.eaglet.vocab;

import org.aksw.dice.eaglet.entitytypemodify.NamedEntityCorrections.Check;
import org.aksw.dice.eaglet.entitytypemodify.NamedEntityCorrections.DecisionValue;
import org.aksw.dice.eaglet.entitytypemodify.NamedEntityCorrections.ErrorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * Vocabulary for the whole software.
 *
 * @author Kunal
 *
 */
public class EAGLET {

	private static final Logger LOGGER = LoggerFactory.getLogger(EAGLET.class);

	protected static final String uri = "http://gerbil.aksw.org/eaglet/vocab#";

	/**
	 * returns the URI for this schema
	 *
	 * @return the URI for this schema
	 */
	public static String getURI() {
		return uri;
	}

	protected static final Resource resource(String local) {
		return ResourceFactory.createResource(uri + local);
	}

	protected static final Property property(String local) {
		return ResourceFactory.createProperty(uri, local);
	}

	// Check Result
	public static final Resource Inserted = resource("Inserted");
	public static final Resource Deleted = resource("Deleted");
	public static final Resource Good = resource("Good");
	public static final Resource NeedToPair = resource("NeedToPair");
	public static final Resource Overlaps = resource("Overlaps");
	public static final Resource Completed = resource("Completed");
	public static final Resource InvalidUri = resource("InvalidUri");
	public static final Resource OutdatedUri = resource("OutdatedUri");
	public static final Resource DisambiguationUri = resource("DisambiguationUri");

	// ErrorType
	public static final Resource Combined = resource("CombinedTagging");
	public static final Resource Overlapping = resource("Overlapping");
	public static final Resource Erratic = resource("Combined");
	public static final Resource WrongPos = resource("WrongPos");
	public static final Resource LongDesc = resource("LongDesc");
	public static final Resource InvalidUriErr = resource("InvalidUriErr");
	public static final Resource OutdatedUriErr = resource("OutdatedUriErr");
	public static final Resource DisambiguationUriErr = resource("DisambiguationUriErr");

	// User Decision Value
	public static final Resource Correct = resource("Correct");
	public static final Resource Wrong = resource("Wrong");
	public static final Resource Added = resource("Added");

	public static final Property hasErrorType = property("hasErrorType");
	public static final Property hasCheckResult = property("hasCheckResult");
	public static final Property hasPairPartner = property("hasPairPartner");
	public static final Property hasUserDecision = property("hasUserDecision");

	public static Resource getUserDecision(DecisionValue desVal) {
		switch (desVal) {
		case ADDED:
			return Added;
		case CORRECT:
			return Correct;
		case WRONG:
			return Wrong;

		}
		LOGGER.error("Got an unknown Decision type: " + desVal.name());
		return null;
	}

	public static Resource getErrorType(ErrorType list) {
		switch (list) {
		case OVERLAPPING:
			return Overlapping;
		case COMBINED:
			return Combined;
		case ERRATIC:
			return Erratic;
		case LONGDESC:
			return LongDesc;
		case INVALIDURIERR:
			return InvalidUriErr;
		case OUTDATEDURIERR:
			return OutdatedUriErr;
		case DISAMBIGURIERR:
			return DisambiguationUriErr;
		case WRONGPOSITION:
			return WrongPos;

		}
		LOGGER.error("Got an unknown matching type: " + list.name());
		return null;
	}

	public static Resource getCheckResult(Check checkResult) {
		switch (checkResult) {
		case INSERTED:
			return Inserted;
		case DELETED:
			return Deleted;
		case GOOD:
			return Good;
		case NEED_TO_PAIR:
			return NeedToPair;
		case OVERLAPS:
			return Overlaps;
		case COMPLETED:
			return Completed;
		case INVALID_URI:
			return InvalidUri;
		case OUTDATED_URI:
			return OutdatedUri;
		case DISAMBIG_URI:
			return DisambiguationUri;

		}
		LOGGER.error("Got an unknown matching type: " + checkResult.name());
		return null;
	}

}
