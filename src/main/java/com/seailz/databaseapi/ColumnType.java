package com.seailz.databaseapi;

/**
 * Represents the type of value within a table
 *
 * @author Seailz
 * <p></p>
 * <p>{@code VARCHAR} represents a {@link String}</p>
 * <p>{@code INT} represents an {@link Integer}</p>
 * <p>{@code DOUBLE} represents a {@link Double}</p>
 * <p>{@code BOOLEAN} represents a {@link Boolean}</p>
 * <p>{@code BIGINT} represents a {@link java.math.BigInteger}</p>
 * <p>{@code FLOAT} represents a {@link Float}</p>
 * <p>{@code LONG} represents a {@link Long}</p>
 * <p>{@code BYTE} represents a {@link Byte}</p>
 * <p>{@code TINYINT} represents a smaller {@link Integer}</p>
 * <p>{@code BLOB} represents a {@link java.sql.Blob}</p>
 */
public enum ColumnType {

    VARCHAR,
    INT,
    DOUBLE,
    BOOLEAN,
    BIGINT,
    FLOAT,
    LONG,
    BYTE,
    DECIMAL,
    BLOB,
    TINYINT

}

