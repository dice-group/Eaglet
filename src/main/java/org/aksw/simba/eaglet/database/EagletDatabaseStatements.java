package org.aksw.simba.eaglet.database;

import java.io.Closeable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.aksw.gerbil.database.IntegerRowMapper;
import org.aksw.gerbil.database.StringRowMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * The class handles the database for user results and userid. It works on the
 * eaglet-schema.sql
 *
 * @author Kunal
 * @author Michael
 *
 */
public class EagletDatabaseStatements implements Closeable {

	private final static String INSERT_USER = "INSERT INTO Users (name) VALUES (:userName)";
	private final static String INSERT_DOCUMENT = "INSERT INTO Documents (userId, documentUri,fileName) VALUES (:userId, :documentUri, :fileName)";
	private final static String GET_USER = "SELECT id FROM Users WHERE name=:userName";
	private final static String GET_DOCUMENT_FILES = "SELECT fileName FROM Documents WHERE documentUri=:documentUri";
	private final static String GET_DOCUMENTS_REVIEWED_BY_USER = "SELECT documentUri FROM Documents WHERE userId=:userId";

	private final static String SHUTDOWN = "SHUTDOWN";

	public static final int USER_NOT_FOUND = -1;

	private final NamedParameterJdbcTemplate template;

	/**
	 * Constructor
	 *
	 * @param dataSource
	 */
	public EagletDatabaseStatements(DataSource dataSource) {
		this.template = new NamedParameterJdbcTemplate(dataSource);
	}

	/**
	 * The method to get the user's data.
	 *
	 * @param userName
	 * @return Userid
	 */
	public int getUser(String userName) {
		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("userName", userName);
		List<Integer> result = this.template.query(GET_USER, parameters,
				new IntegerRowMapper());
		if (result.size() >= 1) {
			return result.get(0);
		} else {
			return USER_NOT_FOUND;
		}
	}

	/**
	 * The method return the list of all the fileNames.
	 *
	 * @param documentUri
	 * @return List of filenames
	 */
	public List<String> getDocumentFilenames(String documentUri) {
		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("documentUri", documentUri);
		List<String> result = this.template.query(GET_DOCUMENT_FILES,
				parameters, new StringRowMapper());
		if (result.size() >= 1) {
			return result;
		} else {
			return null;
		}
	}

	/**
	 * The method returns all the documents evaluated by the user.
	 *
	 * @param userId
	 * @return List of documents evaluated by user.
	 */

	public List<String> getDocumentUser(int userId) {
		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("userId", userId);
		List<String> result = this.template.query(
				GET_DOCUMENTS_REVIEWED_BY_USER, parameters,
				new StringRowMapper());
		if (result.size() >= 1) {
			return result;
		} else {
			return new ArrayList<String>(0);
		}
	}

	/**
	 * The method adds a new user to the database.
	 *
	 * @param name
	 */
	public void addUser(String name) {
		MapSqlParameterSource parameters = new MapSqlParameterSource();

		parameters.addValue("userName", name);
		this.template.update(INSERT_USER, parameters);

	}

	public void addDocument(int userid, String documentUri, String fileName) {
		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("userId", userid);
		parameters.addValue("documentUri", documentUri);
		parameters.addValue("fileName", fileName);

		this.template.update(INSERT_DOCUMENT, parameters);

	}

	@Override
	public void close() {
		this.template.execute(SHUTDOWN,
				new PreparedStatementCallback<Object>() {
					@Override
					public Object doInPreparedStatement(PreparedStatement arg0)
							throws SQLException, DataAccessException {
						// nothing to do
						return null;
					}
				});
	}
}
