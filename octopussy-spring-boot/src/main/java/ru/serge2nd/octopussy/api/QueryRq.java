package ru.serge2nd.octopussy.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import ru.serge2nd.octopussy.util.QueryWithParams;

import javax.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;

@Value
@Builder
public class QueryRq {
    @NotBlank
    String query;

    @Singular
    Map<@NotBlank String, Object> params;

    public QueryRq(@JsonProperty("query")  String query,
                   @JsonProperty("params") Map<String, Object> params) {
        this.query = query;
        this.params = params;
    }

    @SuppressWarnings("unchecked,rawtypes")
    public QueryWithParams toQuery(Map<String, Object> global) {
        return new QueryWithParams(query,
            params == null || params.isEmpty()
                ? global :
            global == null || global.isEmpty()
                ? params
                : new HashMap(global){{putAll(params);}});
    }
}
