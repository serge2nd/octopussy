package ru.serge2nd.octopussy.util;

import lombok.NonNull;
import lombok.Value;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;

@Value
public class QueryWithParams {
    String query;
    Map<String, Object> params;

    public QueryWithParams(@NonNull String query, Map<String, Object> params) {
        this.query = query;
        this.params = params != null ? unmodifiableMap(params) : emptyMap();
    }
}
