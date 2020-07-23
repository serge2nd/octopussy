package ru.serge2nd.octopussy.service;

import ru.serge2nd.octopussy.spi.DataEnvironment;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

public interface DataEnvironmentService {

    DataEnvironment get(String envId);

    Optional<DataEnvironment> find(String envId);

    Collection<DataEnvironment> getAll();

    default <R> R doWith(String envId, Function<DataEnvironment, R> action) {
        return action.apply(this.get(envId));
    }

    DataEnvironment create(DataEnvironment toCreate);

    void delete(String envId);
}
