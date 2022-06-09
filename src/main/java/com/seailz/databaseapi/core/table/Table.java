package com.seailz.databaseapi.core.table;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Represents a MySQL table
 * @author Seailz
 */
@Getter
@RequiredArgsConstructor
public class Table {
    private final String name;
    private final List<Column> columns;
    @Setter
    private String primaryKey;
}
