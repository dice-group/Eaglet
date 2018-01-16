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
package org.aksw.dice.eaglet.uri;

import org.aksw.dice.eaglet.entitytypemodify.NamedEntityCorrections.Check;

/**
 * Interface for an {@link UriChecker} that checks whether an URI is valid for a
 * named entity.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public interface UriChecker {

    /**
     * Returns {@link Check#GOOD} if the given URI is valid. Else, a different
     * check result is returned, i.e., {@link Check#INVALID_URI},
     * {@link Check#DISAMBIG_URI} or {@link Check#OUTDATED_URI}.
     * 
     * @param uri
     *            the URI that should be checked
     * @return {@link Check#GOOD} if the given URI is valid.
     */
    public Check checkUri(String uri);
}
