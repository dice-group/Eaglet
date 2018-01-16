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
package org.aksw.dice.eaglet.uri.impl;

import java.util.Arrays;
import java.util.List;

import org.aksw.dice.eaglet.entitytypemodify.NamedEntityCorrections.Check;
import org.aksw.dice.eaglet.uri.UriChecker;
import org.aksw.dice.eaglet.uri.UriCheckerManager;
import org.aksw.gerbil.dataset.check.EntityChecker;

import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

/**
 * <p>
 * Standard implementation of the {@link UriCheckerManager} interface.
 * Internally it uses a cache for storing the results of the {@link UriChecker}.
 * </p>
 * TODO The current implementation is not thread safe if
 * {@link #registerEntityChecker(String, EntityChecker)} is called while another
 * thread already is inside the {@link #checkMeanings(List)} method.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public class UriCheckerManagerImpl implements UriCheckerManager {

    private ObjectObjectOpenHashMap<String, UriChecker[]> registeredCheckers = new ObjectObjectOpenHashMap<String, UriChecker[]>();

    @Override
    public void registerUriChecker(String namespace, UriChecker checker) {
        UriChecker checkers[];
        if (registeredCheckers.containsKey(namespace)) {
            checkers = registeredCheckers.lget();
            checkers = Arrays.copyOf(checkers, checkers.length + 1);
            checkers[checkers.length - 1] = checker;
        } else {
            checkers = new UriChecker[] { checker };
        }
        registeredCheckers.put(namespace, checkers);
    }

    @Override
    public Check checkUri(String uri) {
        String namespace;
        int matchingId = -1;
        for (int i = 0; (i < registeredCheckers.allocated.length) && (matchingId < 0); ++i) {
            if (registeredCheckers.allocated[i]) {
                namespace = (String) ((Object[]) registeredCheckers.keys)[i];
                if (uri.startsWith(namespace)) {
                    matchingId = i;
                }
            }
        }
        // If there is a checker available for this URI
        if (matchingId >= 0) {
            Check result;
            UriChecker checkers[] = (UriChecker[]) ((Object[]) registeredCheckers.values)[matchingId];
            for (UriChecker checker : checkers) {
                result = checker.checkUri(uri);
                if (result != Check.GOOD) {
                    return result;
                }
            }
            return Check.GOOD;
        } else {
            return Check.GOOD;
        }
    }

}
