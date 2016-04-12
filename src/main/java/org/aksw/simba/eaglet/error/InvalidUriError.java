package org.aksw.simba.eaglet.error;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.aksw.gerbil.dataset.check.impl.EntityCheckerManagerImpl;
import org.aksw.gerbil.dataset.check.impl.FileBasedCachingEntityCheckerManager;
import org.aksw.gerbil.dataset.check.impl.HttpBasedEntityChecker;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections.Check;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks the existence of all URIs of given entities.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public class InvalidUriError implements ErrorChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(InvalidUriError.class);

    private static final String CACHE_FILE_NAME = "eaglet_data/cache/uri_check.cache";
    private static final long CACHE_DURATION = Long.MAX_VALUE;

    private EntityCheckerManagerImpl entityChecker;

    public InvalidUriError() {
        File cacheFile = new File(CACHE_FILE_NAME);
        if (!cacheFile.exists()) {
            LOGGER.info("Cache file couldn't be found. Creating a new one.");
            if (!cacheFile.getParentFile().exists()) {
                cacheFile.getParentFile().mkdirs();
            }
        }
        entityChecker = FileBasedCachingEntityCheckerManager.create(CACHE_DURATION, cacheFile);

        HttpBasedEntityChecker httpBasedChecker = new HttpBasedEntityChecker();
        entityChecker.registerEntityChecker("http://dbpedia.org/resource", httpBasedChecker);
        entityChecker.registerEntityChecker("http://fr.dbpedia.org/resource", httpBasedChecker);
        entityChecker.registerEntityChecker("http://de.dbpedia.org/resource", httpBasedChecker);
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
        for (NamedEntityCorrections entity : entities) {
            if ((entity.getResult() == Check.GOOD) && (!checkUris(entity.getUris()))) {
                entity.setResult(Check.INVALID_URI);
            }
        }
    }

    public boolean checkUris(Collection<String> uris) {
        for (String uri : uris) {
            if (!checkUri(uri)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkUri(String uri) {
        if ((!uri.startsWith("http://")) && (!uri.startsWith("https://"))) {
            return false;
        }
        return entityChecker.checkUri(uri);
    }

}
