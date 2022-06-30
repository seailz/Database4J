package com.seailz.databaseapi.annotation.builder;

import com.seailz.databaseapi.Column;
import com.seailz.databaseapi.ColumnType;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents a MySQL table
 *
 * @author Seailz
 */
@Data
public class TableBuilder {
    private final String name;
    private final List<Column> columns;
    private String primaryKey;

    public void addColumn(@NotNull ColumnType type, @NotNull String name) {
        columns.add(new Column(type, name));
    }

    public @NotNull TableBuilder addColumn(@NotNull Column column) {
        columns.add(column);
        return this;
    }
}
