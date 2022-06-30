package com.seailz.databaseapi;

import lombok.Data;

/**
 * Represents a value in a table
 *
 * @author Seailz
 */
@Data
public class Column {
    private final ColumnType type;
    private final String name;
    private int length = 255;
    private boolean allowNull = false;
    private String defaultValue = null;
}
