package ru.serge2nd.octopussy.dataenv;

import java.util.Collection;

public interface DataEnvironmentService {

    DataEnvironment get(String envId);

    Collection<DataEnvironment> getAll();

    DataEnvironment create(DataEnvironment toCreate);

    void delete(String envId);
}
