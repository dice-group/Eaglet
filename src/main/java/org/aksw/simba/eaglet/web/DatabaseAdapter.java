package org.aksw.simba.eaglet.web;

import java.io.Closeable;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class DatabaseAdapter implements Closeable {

    private final static String SHUTDOWN = "SHUTDOWN";

    private final NamedParameterJdbcTemplate template;

    public DatabaseAdapter(DataSource dataSource) {
        this.template = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public void close() {
        this.template.execute(SHUTDOWN, new PreparedStatementCallback<Object>() {
            @Override
            public Object doInPreparedStatement(PreparedStatement arg0) throws SQLException, DataAccessException {
                // nothing to do
                return null;
            }
        });
    }
}
