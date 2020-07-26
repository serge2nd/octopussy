package ru.serge2nd.octopussy.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.serge2nd.octopussy.spi.DataEnvironment;
import ru.serge2nd.octopussy.support.DataEnvironmentDefinition;
import ru.serge2nd.octopussy.spi.DataEnvironmentService;

import javax.validation.Valid;
import java.util.List;
import static java.util.stream.Collectors.toList;

@Validated
@RestController
@RequestMapping("dataEnvironments")
@RequiredArgsConstructor
public class DataEnvironmentController {
    private final DataEnvironmentService dataEnvService;

    @GetMapping("{envId}")
    public DataEnvironmentDefinition getOne(@PathVariable String envId) {
        return dataEnvService.get(envId).getDefinition();
    }

    @GetMapping
    public List<DataEnvironmentDefinition> getAll() {
        return dataEnvService.getAll().stream()
                .map(DataEnvironment::getDefinition)
                .collect(toList());
    }

    @PostMapping
    public DataEnvironmentDefinition create(@Valid @RequestBody DataEnvironmentDefinition definition) {
        return dataEnvService.create(definition).getDefinition();
    }

    @DeleteMapping("{envId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String envId) {
        dataEnvService.delete(envId);
    }
}
