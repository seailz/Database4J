package com.seailz.databaseapi.core.table;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
public class Column {

    private final ColumnType type;
    private final String name;
    @Setter
    private int length = 255;
    @Setter
    private boolean allowNull = true;

}

