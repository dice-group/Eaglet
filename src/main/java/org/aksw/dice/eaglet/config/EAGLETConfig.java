package org.aksw.dice.eaglet.config;

import org.aksw.gerbil.config.GerbilConfiguration;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EAGLETConfig {
	 private static final Logger LOGGER = LoggerFactory.getLogger(GerbilConfiguration.class);

	    private static final String DEFAULT_EAGLET_PROPERTIES_FILE_NAME = "eaglet.properties";
	    public static final String EAGLET_DATAPATH_PROPERTY_NAME = "org.aksw.eaglet.DataPath";
	    public static final String EAGLET_VERSION_PROPERTY_NAME = "org.aksw.eaglet.Version";

	    private static Configuration instance = null;

	    public static synchronized Configuration getInstance() {
	        if (instance == null) {
	            instance = new CompositeConfiguration();
	            loadAdditionalProperties(DEFAULT_EAGLET_PROPERTIES_FILE_NAME);
	        }
	        return instance;
	    }

	    public static synchronized void loadAdditionalProperties(String fileName) {
	        try {
	            ((CompositeConfiguration) getInstance()).addConfiguration(new PropertiesConfiguration(fileName));
	        } catch (ConfigurationException e) {
	            LOGGER.error("Couldnt load Properties from the properties file (\"" + fileName
	                    + "\"). This EAGLET instance won't work as expected.", e);
	        }
	    }

	    public static String getGerbilVersion() {
	        return getInstance().getString(EAGLET_VERSION_PROPERTY_NAME);
	    }
}
