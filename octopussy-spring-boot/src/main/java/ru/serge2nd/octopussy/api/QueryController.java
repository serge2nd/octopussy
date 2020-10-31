package ru.serge2nd.octopussy.api;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.serge2nd.octopussy.spi.NativeQueryAdapter;
import ru.serge2nd.octopussy.spi.NativeQueryAdapterProvider;
import ru.serge2nd.octopussy.util.QueryWithParams;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Validated
@RestController
@RequestMapping("dataEnvironments/{envId}")
@RequiredArgsConstructor
public class QueryController {
    private final NativeQueryAdapterProvider queryAdapterFactory;

    @PostMapping(path = "query", consumes = APPLICATION_JSON_VALUE)
    List<?> executeQuery(@NotBlank @PathVariable String envId,
                         @Valid    @RequestBody  QueriesRq rq) {
        QueryWithParams q = rq.toQueries().get(0);
        return queryAdapter(envId).execute(q.getQuery(), q.getParams());
    }

    @PostMapping(path = "update", consumes = APPLICATION_JSON_VALUE)
    int[] executeUpdate(@NotBlank @PathVariable String envId,
                        @Valid    @RequestBody  QueriesRq rq) {
        return queryAdapter(envId).executeUpdate(rq.toQueries());
    }

    NativeQueryAdapter queryAdapter(String envId) { return queryAdapterFactory.getQueryAdapter(envId); }
}
