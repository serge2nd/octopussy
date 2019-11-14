package ru.serge2nd.octopussy.dataenv;

import java.util.Collection;
import java.util.Optional;

public interface DataEnvironmentService {

    DataEnvironment get(String envId);

    Optional<DataEnvironment> find(String envId);

    Collection<DataEnvironment> getAll();

    DataEnvironment create(DataEnvironment toCreate);

    void delete(String envId);
}
