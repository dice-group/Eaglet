package org.aksw.simba.eaglet.web.config;

import org.aksw.gerbil.database.ExperimentDAO;
import org.aksw.gerbil.web.config.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

public class DataBaseConfig extends WebMvcConfigurerAdapter  {
	 private static final transient Logger LOGGER = LoggerFactory.getLogger(DatabaseConfig.class);

	    /**
	     * This {@link Configuration} creates the {@link ExperimentDAO} bean by loading the XML config from the class path.
	     * After that, the bean is initialized using the {@link ExperimentDAO#initialize()} method.
	     * 
	     * @return the database bean
	     */
	    @Bean
	    public ExperimentDAO experimentDAO() {
	        LOGGER.debug("Setting up database.");
	        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
	                "/spring/database/database-context.xml");
	        ExperimentDAO database = context.getBean(ExperimentDAO.class);
	        database.initialize();
	        context.close();
	        return database;
	    }s
}
