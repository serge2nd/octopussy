package ru.serge2nd.octopussy.spi;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Function;

/**
 * Creates, obtains, removes {@link DataKit} instances.
 */
public interface DataKitService {

    DataKit             get(String id);

    Optional<DataKit>   find(String id);

    Collection<DataKit> getAll();

    DataKit create(DataKit toCreate);

    void delete(String id);
}
