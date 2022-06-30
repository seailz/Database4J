package com.seailz.databaseapi.annotation.builder;

import lombok.Getter;

import java.util.HashMap;

@Getter
public class InsertBuilder {

    private String table;
    private HashMap<String, String> values = new HashMap<>();

    public InsertBuilder value(String column, String value) {
        values.put(column, value);
        return this;
    }

    public InsertBuilder table(String table) {
        this.table = table;
        return this;
    }

}

