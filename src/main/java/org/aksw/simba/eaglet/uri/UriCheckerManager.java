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
package org.aksw.simba.eaglet.uri;

/**
 * This is an interface for a class that manages the URI checking using an
 * internal mapping of URI name spaces to known {@link UriChecker} instances.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public interface UriCheckerManager extends UriChecker {

    /**
     * Registers the given {@link UriChecker} for being used for URIs of the
     * given name space. Note that only one single EntityChecker per name space
     * is allowed. If a second EntityChecker is registered, the first one will
     * be overwritten.
     * 
     * @param namespace
     *            URI name space for which the given checker should be used.
     * @param checker
     *            the {@link UriChecker} that should be registered for the given
     *            URI name space.
     */
    public void registerUriChecker(String namespace, UriChecker checker);

}
