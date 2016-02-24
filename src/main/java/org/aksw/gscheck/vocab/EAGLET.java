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
package org.aksw.gscheck.vocab;

import org.aksw.gscheck.corrections.NamedEntityCorrections.Check;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

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

    public static final Resource Inserted = resource("INSERTED");
    public static final Resource Deleted = resource("DELETED");
    public static final Resource Good = resource("GOOD");
    public static final Resource NeedToPair = resource("NEED_TO_PAIR");
    public static final Resource Overlaps = resource("OVERLAPS");

    public static final Property hasCheckResult = property("hasCheckResult");
    public static final Property hasPairPartner = property("hasPairPartner");

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
        }
        LOGGER.error("Got an unknown matching type: " + checkResult.name());
        return null;
    }
}
