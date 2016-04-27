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
package org.aksw.simba.eaglet.uri.impl;

import java.io.IOException;
import java.nio.charset.Charset;

import org.aksw.gerbil.http.AbstractHttpRequestEmitter;
import org.aksw.gerbil.semantic.sameas.impl.SimpleDomainExtractor;
import org.aksw.gerbil.semantic.sameas.impl.wiki.WikipediaXMLParser;
import org.aksw.gerbil.utils.WikipediaHelper;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections.Check;
import org.aksw.simba.eaglet.uri.UriChecker;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;

public class WikipidiaUriChecker extends AbstractHttpRequestEmitter implements UriChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(WikipidiaUriChecker.class);

    private static final String URL_PROTOCOL_PART = "http://";
    private static final String URL_QUERY_PART = "/w/api.php?format=xml&action=query&redirects=true&titles=";
    private static final String CHARSET_NAME = "UTF-8";
    private static final Escaper TITLE_ESCAPER = UrlEscapers.urlFormParameterEscaper();

    private Charset charset;
    private WikipediaXMLParser parser = new WikipediaXMLParser();

    public WikipidiaUriChecker() {
        try {
            charset = Charset.forName(CHARSET_NAME);
        } catch (Exception e) {
            charset = Charset.defaultCharset();
        }
    }

    @Override
    public Check checkUri(String uri) {
        if (uri == null) {
			LOGGER.info("INVALID_URI \"{}\"", uri);
            return Check.INVALID_URI;
        }
        String title = WikipediaHelper.getWikipediaTitle(uri);
        if (title == null) {
			LOGGER.info("INVALID_URI \"{}\"", uri);
            return Check.INVALID_URI;
        }
        String xml = queryRedirect(SimpleDomainExtractor.extractDomain(uri), title);
        String redirect = parser.extractRedirect(xml);
        if ((redirect != null) && (!title.equals(redirect))) {
			LOGGER.info("OUTDATED_URI \"{}\"", uri);
            return Check.OUTDATED_URI;
        } else {
            return Check.GOOD;
        }
    }

    public String queryRedirect(String domain, String title) {
        StringBuilder urlBuilder = new StringBuilder(150);
        urlBuilder.append(URL_PROTOCOL_PART);
        urlBuilder.append(domain);
        urlBuilder.append(URL_QUERY_PART);
        urlBuilder.append(TITLE_ESCAPER.escape(title));

        HttpGet request = null;
        try {
            request = createGetRequest(urlBuilder.toString());
        } catch (IllegalArgumentException e) {
            LOGGER.error("Got an exception while creating a request querying the wiki api of \"" + domain
                    + "\". Returning null.", e);
            return null;
        }
        CloseableHttpResponse response = null;
        HttpEntity entity = null;
        try {
            response = sendRequest(request);
            entity = response.getEntity();
            return IOUtils.toString(entity.getContent(), charset);
        } catch (Exception e) {
            LOGGER.error("Got an exception while querying the wiki api of \"" + domain + "\". Returning null.", e);
            return null;
        } finally {
            if (entity != null) {
                try {
                    EntityUtils.consume(entity);
                } catch (IOException e1) {
                }
            }
            IOUtils.closeQuietly(response);
            closeRequest(request);
        }
    }

}
