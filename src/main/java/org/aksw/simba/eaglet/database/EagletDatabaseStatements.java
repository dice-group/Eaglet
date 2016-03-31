package org.aksw.simba.eaglet.database;

import java.util.List;

import javax.sql.DataSource;

import org.aksw.gerbil.database.StringRowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class EagletDatabaseStatements {

	private static final Logger LOGGER = LoggerFactory.getLogger(EagletDatabaseStatements.class);
	private final static String INSERT_USER = "INSERT INTO Users (userId, name) VALUES (:userId, :userName)";
	private final static String INSERT_DOCUMENT = "INSERT INTO Documents (userId, documentUri,fileName) VALUES (:userId, :documenturi, :filename)";
	private final static String GET_USER = "SELECT userId,name FROM Users WHERE id=:userId";
	private final static String GET_DOCUMENT = "SELECT userId, documentUri, fileName FROM Documents WHERE documentUri=:documenturi";
	private final static String GET_DOCUMENT_REVIEW_COUNT = "SELECT COUNT(*) FROM Documents GROUP BY documentUri";
	
	private final NamedParameterJdbcTemplate template;
	public EagletDatabaseStatements(DataSource dataSource) {
        this.template = new NamedParameterJdbcTemplate(dataSource);
    }
	
	public void getUser(int userID)
	{
		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("userId", userID);
		List<String> result = this.template.query(GET_USER, parameters, new StringRowMapper());
        
	}
}
