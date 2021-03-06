package org.aksw.dice.eaglet.error;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.aksw.dice.eaglet.entitytypemodify.NamedEntityCorrections;
import org.aksw.dice.eaglet.entitytypemodify.NamedEntityCorrections.Correction;
import org.aksw.dice.eaglet.entitytypemodify.NamedEntityCorrections.ErrorType;
import org.aksw.dice.eaglet.uri.UriCheckerManager;
import org.aksw.dice.eaglet.uri.impl.FileBasedCachingUriCheckerManager;
import org.aksw.dice.eaglet.uri.impl.HttpBasedUriChecker;
import org.aksw.dice.eaglet.uri.impl.UriCheckerWrappingDBpediaWikipediaBridge;
import org.aksw.dice.eaglet.uri.impl.WikipidiaUriChecker;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks the existence of all URIs of given entities.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public class UriError implements ErrorChecker, Closeable {

	private static final Logger LOGGER = LoggerFactory.getLogger(UriError.class);

	private static final String CACHE_FILE_NAME = "eaglet_data/cache/uri_check.cache";
	private static final long CACHE_DURATION = Long.MAX_VALUE;

	private UriCheckerManager uriChecker;

	public UriError() {
		File cacheFile = new File(CACHE_FILE_NAME);
		if (!cacheFile.exists()) {
			LOGGER.info("Cache file couldn't be found. Creating a new one.");
			if (!cacheFile.getParentFile().exists()) {
				cacheFile.getParentFile().mkdirs();
			}
		}
		uriChecker = FileBasedCachingUriCheckerManager.create(CACHE_DURATION, cacheFile);

		HttpBasedUriChecker httpBasedChecker = new HttpBasedUriChecker();
		WikipidiaUriChecker wikiBasedChecker = new WikipidiaUriChecker();
		uriChecker.registerUriChecker("http://dbpedia.org/resource", httpBasedChecker);
		uriChecker.registerUriChecker("http://dbpedia.org/resource", new UriCheckerWrappingDBpediaWikipediaBridge(
				wikiBasedChecker));
		uriChecker.registerUriChecker("http://fr.dbpedia.org/resource", httpBasedChecker);
		uriChecker.registerUriChecker("http://fr.dbpedia.org/resource", new UriCheckerWrappingDBpediaWikipediaBridge(
				wikiBasedChecker));
		uriChecker.registerUriChecker("http://de.dbpedia.org/resource", httpBasedChecker);
		uriChecker.registerUriChecker("http://de.dbpedia.org/resource", new UriCheckerWrappingDBpediaWikipediaBridge(
				wikiBasedChecker));

		uriChecker.registerUriChecker("http://en.wikipedia.org/wiki", new UriCheckerWrappingDBpediaWikipediaBridge(
				httpBasedChecker));
		uriChecker.registerUriChecker("http://en.wikipedia.org/wiki", wikiBasedChecker);
		uriChecker.registerUriChecker("http://fr.wikipedia.org/wiki", new UriCheckerWrappingDBpediaWikipediaBridge(
				httpBasedChecker));
		uriChecker.registerUriChecker("http://fr.wikipedia.org/wiki", wikiBasedChecker);
		uriChecker.registerUriChecker("http://de.wikipedia.org/wiki", new UriCheckerWrappingDBpediaWikipediaBridge(
				httpBasedChecker));
		uriChecker.registerUriChecker("http://de.wikipedia.org/wiki", wikiBasedChecker);
	}

	@Override
	public void check(List<Document> documents) throws GerbilException {
		for (Document document : documents) {
			check(document);
		}
	}

	public void check(Document document) {
		checkEntities(document.getMarkings(NamedEntityCorrections.class));
	}

	public void checkEntities(List<NamedEntityCorrections> entities) {
		ErrorType result;
		for (NamedEntityCorrections entity : entities) {
			if (entity.getCorrectionSuggested() == Correction.GOOD) {
				result = checkUris(entity.getUris());
				if (result != ErrorType.NOERROR) {
					entity.setError(result);
					entity.setCorrectionSuggested(Correction.CHECK);
				}
			}
		}
	}

	public ErrorType checkUris(Collection<String> uris) {
		ErrorType result;
		for (String uri : uris) {
			result = checkUri(uri);
			if (result != ErrorType.NOERROR) {
				return result;
			}
		}
		return ErrorType.NOERROR;
	}

	private ErrorType checkUri(String uri) {
		if ((!uri.startsWith("http://")) && (!uri.startsWith("https://"))) {
			LOGGER.info("INVALID_URI \"{}\"", uri);
			return ErrorType.INVALIDURIERR;
		}
		return uriChecker.checkUri(uri);
	}

	@Override
	public void close() throws IOException {
		if (uriChecker instanceof Closeable) {
			IOUtils.closeQuietly((Closeable) uriChecker);
		}
	}
}
