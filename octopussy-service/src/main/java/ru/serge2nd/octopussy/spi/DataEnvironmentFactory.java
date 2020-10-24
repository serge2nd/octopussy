package ru.serge2nd.octopussy.spi;

import ru.serge2nd.octopussy.support.DataEnvironmentDefinition;

import java.util.Map;

/**
 * A factory of {@link DataEnvironment} instances.
 */
public interface DataEnvironmentFactory {

    JpaEnvironment newJpaEnvironment(DataEnvironmentDefinition definition);

    Map<String, String> getPropertyMappings();
}
