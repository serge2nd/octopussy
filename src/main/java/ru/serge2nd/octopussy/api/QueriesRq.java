package ru.serge2nd.octopussy.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import ru.serge2nd.octopussy.util.Queries;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Map;

import static ru.serge2nd.octopussy.util.Queries.mapToQueries;
import static ru.serge2nd.stream.util.Collecting.collect;

@Value
@Builder
public class QueriesRq {
    @Singular
    @NotNull @Size(min = 1)
    List<@NotNull @Valid QueryRq> queries;

    @Singular
    Map<@NotBlank String, Object> params;

    public QueriesRq(@JsonProperty("queries") List<QueryRq> queries,
                     @JsonProperty("params")  Map<String, Object> params) {
        this.queries = queries;
        this.params = params;
    }

    public Queries toQueries() {
        return queries.size() > 0
                ? collect(queries, mapToQueries(rq -> rq.toQuery(params), queries.size()))
                : Queries.EMPTY;
    }
}
