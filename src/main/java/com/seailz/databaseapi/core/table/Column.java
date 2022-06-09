package com.seailz.databaseapi.core.table;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Represents a value in a table
 * @author Seailz
 */
@Getter
@RequiredArgsConstructor
public class Column {

    private final ColumnType type;
    private final String name;

}

