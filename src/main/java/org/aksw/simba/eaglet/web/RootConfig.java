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
package org.aksw.simba.eaglet.web;

import org.aksw.gerbil.utils.ConsoleLogger;
import org.aksw.simba.eaglet.database.EagletDatabaseStatements;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

/**
 * This is the root {@link Configuration} class that is processed by the Spring
 * framework and performs the following configurations:
 * <ul>
 * <li>Loads the properties file \"gerbil.properties\"</li>
 * <li>Starts a component scan inside the package
 * <code>org.aksw.gerbil.web.config</code> searching for other
 * {@link Configuration}s</li>
 * <li>Replaces the streams used by <code>System.out</code> and
 * <code>System.err</code> by two {@link ConsoleLogger} objects. (This is a very
 * ugly workaround that should be fixed in the near future)</li>
 * </ul>
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 * @author Lars Wesemann
 * @author Didier Cherix
 * 
 */
@org.springframework.context.annotation.Configuration
@ComponentScan(basePackages = "org.aksw.simba.eaglet.web")
@PropertySource("gerbil.properties")
public class RootConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(RootConfig.class);

    private static final String ENTITY_CHECKING_MANAGER_USE_PERSISTENT_CACHE_KEY = "org.aksw.gerbil.dataset.check.EntityCheckerManagerImpl.usePersistentCache";
    private static final String ENTITY_CHECKING_MANAGER_PERSISTENT_CACHE_FILE_NAME_KEY = "org.aksw.gerbil.dataset.check.FileBasedCachingEntityCheckerManager.cacheFile";
    private static final String ENTITY_CHECKING_MANAGER_PERSISTENT_CACHE_DURATION_KEY = "org.aksw.gerbil.dataset.check.FileBasedCachingEntityCheckerManager.cacheDuration";
    private static final String ENTITY_CHECKING_MANAGER_IN_MEM_CACHE_SIZE_KEY = "org.aksw.gerbil.dataset.check.InMemoryCachingEntityCheckerManager.cacheSize";
    private static final String ENTITY_CHECKING_MANAGER_IN_MEM_CACHE_DURATION_KEY = "org.aksw.gerbil.dataset.check.InMemoryCachingEntityCheckerManager.cacheDuration";
    private static final String HTTP_BASED_ENTITY_CHECKING_NAMESPACE_KEY = "org.aksw.gerbil.dataset.check.HttpBasedEntityChecker.namespace";

    // static @Bean public PropertySourcesPlaceholderConfigurer
    // myPropertySourcesPlaceholderConfigurer() {
    // PropertySourcesPlaceholderConfigurer p = new
    // PropertySourcesPlaceholderConfigurer();
    // Resource[] resourceLocations = new Resource[] { new
    // ClassPathResource("gerbil.properties"), };
    // p.setLocations(resourceLocations);
    // return p;
    // }

    @Bean
    public EagletDatabaseStatements experimentDAO() {
        LOGGER.debug("Setting up database.");
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
                "/spring/database/database-context.xml");
        EagletDatabaseStatements database = context.getBean(EagletDatabaseStatements.class);
        context.close();
        return database;
    }

//    @Bean
//    public EagletController controller() {
//        return new EagletController(EagletController.loadDocuments());
//    }

    // public static @Bean EntityCheckerManager getEntityCheckerManager() {
    // EntityCheckerManager manager = null;
    // Configuration config = GerbilConfiguration.getInstance();
    // if (config.containsKey(ENTITY_CHECKING_MANAGER_USE_PERSISTENT_CACHE_KEY)
    // && config.getBoolean(ENTITY_CHECKING_MANAGER_USE_PERSISTENT_CACHE_KEY)
    // &&
    // config.containsKey(ENTITY_CHECKING_MANAGER_PERSISTENT_CACHE_DURATION_KEY))
    // {
    // LOGGER.info("Using file based cache for entity checking.");
    // try {
    // long duration =
    // config.getLong(ENTITY_CHECKING_MANAGER_PERSISTENT_CACHE_DURATION_KEY);
    // String cacheFile =
    // config.getString(ENTITY_CHECKING_MANAGER_PERSISTENT_CACHE_FILE_NAME_KEY);
    // manager = FileBasedCachingEntityCheckerManager.create(duration, new
    // File(cacheFile));
    // } catch (ConversionException e) {
    // LOGGER.error("Exception while parsing parameter.", e);
    // }
    // }
    // if ((manager == null) &&
    // config.containsKey(ENTITY_CHECKING_MANAGER_IN_MEM_CACHE_SIZE_KEY)
    // && config.containsKey(ENTITY_CHECKING_MANAGER_IN_MEM_CACHE_DURATION_KEY))
    // {
    // LOGGER.info("Using in-memory based cache for entity checking.");
    // try {
    // int cacheSize =
    // config.getInt(ENTITY_CHECKING_MANAGER_IN_MEM_CACHE_SIZE_KEY);
    // long duration =
    // config.getLong(ENTITY_CHECKING_MANAGER_IN_MEM_CACHE_DURATION_KEY);
    // manager = new InMemoryCachingEntityCheckerManager(cacheSize, duration);
    // } catch (Exception e) {
    // LOGGER.error("Exception while parsing parameter. Creating default
    // EntityCheckerManagerImpl.", e);
    // manager = new EntityCheckerManagerImpl();
    // }
    // }
    // if (manager == null) {
    // manager = new EntityCheckerManagerImpl();
    // }
    // @SuppressWarnings("unchecked")
    // List<String> namespaces =
    // config.getList(HTTP_BASED_ENTITY_CHECKING_NAMESPACE_KEY);
    // if (!namespaces.isEmpty()) {
    // HttpBasedEntityChecker checker = new HttpBasedEntityChecker();
    // for (String namespace : namespaces) {
    // manager.registerEntityChecker(namespace.toString(), checker);
    // }
    // }
    // return manager;
    // }

    @Bean
    public CommonsMultipartResolver multipartResolver() {
        CommonsMultipartResolver resolver=new CommonsMultipartResolver();
        resolver.setDefaultEncoding("utf-8");
        return resolver;
    }

}
