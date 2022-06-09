package com.seailz.databaseapi.core.table;

/**
 * Represents the type of value within a table
 * @author Seailz
 * <p></p>
 * <p>{@code VARCHAR} represents a {@link String}</p>
 * <p>{@code INT} represents an {@link Integer}</p>
 * <p>{@code DOUBLE} represents a {@link Double}</p>
 * <p>{@code BOOLEAN} represents a {@link Boolean}</p>
 * <p>{@code BIGINT} represents a {@link java.math.BigInteger}</p>
 */
public enum ColumnType {

    VARCHAR,
    INT,
    DOUBLE,
    BOOLEAN,
    BIGINT

}
