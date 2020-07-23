package ru.serge2nd.octopussy.api;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import ru.serge2nd.octopussy.spi.NativeQueryAdapter;
import ru.serge2nd.octopussy.support.NativeQueryAdapterProviderImpl;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonMap;

@Validated
@RestController
@RequiredArgsConstructor
public class QueryController {
    private final NativeQueryAdapterProviderImpl queryAdapterFactory;

    @PostMapping("executeQuery")
    public @ResponseBody List<?> executeQuery(@Valid @RequestBody QueryRq rq) {
        NativeQueryAdapter queryAdapter = queryAdapterFactory.getQueryAdapter(rq.getEnvId());
        String query = rq.getQueries().get(0);

        return queryAdapter.execute(query);
    }

    @PostMapping("executeUpdate")
    public @ResponseBody Map<String, Integer> executeUpdate(@Valid @RequestBody QueryRq rq) {
        NativeQueryAdapter queryAdapter = queryAdapterFactory.getQueryAdapter(rq.getEnvId());
        int processedRowsCount = queryAdapter.executeUpdate(rq.getQueries());

        return singletonMap("count", processedRowsCount);
    }
}
