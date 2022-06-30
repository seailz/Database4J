package com.seailz.databaseapi.annotation.builder.general;

import lombok.Getter;

@Getter
public class WhereBuilder {

    private String key;
    private String value;

    public WhereBuilder key(String key) {
        this.key = key;
        return this;
    }

    public WhereBuilder value(String value) {
        this.value = value;
        return this;
    }
}
