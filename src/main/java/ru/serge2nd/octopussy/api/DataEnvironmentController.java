package ru.serge2nd.octopussy.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.serge2nd.octopussy.service.ex.DataEnvironmentException;
import ru.serge2nd.octopussy.spi.DataEnvironment;
import ru.serge2nd.octopussy.spi.DataEnvironmentService;
import ru.serge2nd.octopussy.support.DataEnvironmentDefinition;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static ru.serge2nd.octopussy.api.ErrorInfo.errorCode;
import static ru.serge2nd.stream.MappingCollectors.mapToList;
import static ru.serge2nd.stream.util.Collecting.collect;

@Validated
@RestController
@RequestMapping("dataEnvironments")
@RequiredArgsConstructor
public class DataEnvironmentController {
    private final DataEnvironmentService dataEnvService;

    @GetMapping("{envId}")
    DataEnvironmentDefinition getOne(@PathVariable String envId) {
        return dataEnvService.get(envId).getDefinition();
    }

    @GetMapping
    List<DataEnvironmentDefinition> getAll() {
        return collect(dataEnvService.getAll(), mapToList(DataEnvironment::getDefinition, 0));
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(CREATED)
    DataEnvironmentDefinition create(@Valid @RequestBody DataEnvironmentDefinition definition) {
        return dataEnvService.create(definition).getDefinition();
    }

    @DeleteMapping("{envId}")
    @ResponseStatus(NO_CONTENT)
    void delete(@PathVariable String envId) {
        dataEnvService.delete(envId);
    }
}
