package com.seailz.databaseapi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.ResultSet;

/**
 * Create a new Statement
 *
 * @author Seailz
 */
@Data
@AllArgsConstructor
public class Statement {

    private String value;
    private Connection connection;

    /**
     * Execute your statement
     *
     * @return a {@link ResultSet}
     */
    @SneakyThrows
    public ResultSet executeWithResults() {
        return connection.createStatement().executeQuery(getValue());
    }

    /**
     * Execute your statement
     */
    @SneakyThrows
    public void execute() {
        connection.createStatement().execute(getValue());
    }
}

