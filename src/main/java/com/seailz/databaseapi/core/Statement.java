package com.seailz.databaseapi.core;

import lombok.Getter;
import lombok.Setter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Create a new Statement
 * @author Seailz
 */
@Getter
@Setter
public class Statement {

    private String value;
    private Connection connection;

    /**
     * Initiate a new {@code Statement}
     * @param value The {@link String} value of your SQL statement
     * @param connection
     */
    public Statement(String value, Connection connection) {
        setValue(value);
        setConnection(connection);
    }

    /**
     * Execute your statement
     * @return a {@link ResultSet}
     * @throws SQLException If your connection is invalid
     */
    public ResultSet executeWithResults() throws SQLException {
        return connection.createStatement().executeQuery(getValue());
    }

    public void execute() throws SQLException {
        connection.createStatement().execute(getValue());
    }
}

