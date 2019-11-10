package ru.serge2nd.octopussy.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import ru.serge2nd.octopussy.data.NativeQueryAdapter;
import ru.serge2nd.octopussy.data.NativeQueryAdapterProvider;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static java.util.Objects.requireNonNull;

@Validated
@RestController
@RequiredArgsConstructor
public class QueryController {
    private static final TypeReference<?> RESULT_TARGET_TYPE = new TypeReference<ArrayList>() {};

    private final NativeQueryAdapterProvider queryAdapterFactory;
    private final ObjectMapper objectMapper;

    @PostMapping("executeQuery")
    public @ResponseBody List<?> executeQuery(@Valid @RequestBody QueryRq rq) {
        NativeQueryAdapter queryAdapter = queryAdapterFactory.getQueryAdapter(rq.getEnvId());
        List<?> result = queryAdapter.execute(rq.getQueries().get(0));

        return objectMapper.convertValue(result, RESULT_TARGET_TYPE);
    }

    @PostMapping("executeUpdate")
    public @ResponseBody Map<String, Integer> executeUpdate(@Valid @RequestBody QueryRq rq) {
        NativeQueryAdapter queryAdapter = queryAdapterFactory.getQueryAdapter(rq.getEnvId());
        int processedRowsCount = queryAdapter.executeUpdate(rq.getQueries());

        return singletonMap("count", processedRowsCount);
    }
}
