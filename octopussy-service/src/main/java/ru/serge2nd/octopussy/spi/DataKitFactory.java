package ru.serge2nd.octopussy.spi;

import ru.serge2nd.octopussy.support.DataKitDefinition;

/**
 * A factory of {@link DataKit} instances.
 */
public interface DataKitFactory {

    JpaKit newJpaKit(DataKitDefinition definition);
}
