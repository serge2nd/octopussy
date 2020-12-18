package ru.serge2nd.octopussy.spi;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Function;

/**
 * Manages (creates, processes, removes) {@link DataEnvironment} instances.
 */
public interface DataEnvironmentService {

    DataEnvironment             get(String envId);

    Optional<DataEnvironment>   find(String envId);

    Collection<DataEnvironment> getAll();

    DataEnvironment create(DataEnvironment toCreate);

    void delete(String envId);

    default <R> R doWith(String envId, Function<DataEnvironment, R> action) {
        return this.doWith(envId, DataEnvironment.class, action);
    }
    default <T, R> R doWith(String envId, Class<T> t, Function<T, R> action) {
        DataEnvironment dataEnv = this.get(envId);
        return action.apply(dataEnv.unwrap(t));
    }

    default <R> CompletionStage<R> asyncWith(String envId, Function<DataEnvironment, R> action) {
        return this.asyncWith(envId, DataEnvironment.class, action);
    }
    default <T, R> CompletionStage<R> asyncWith(String envId, Class<T> t, Function<T, R> action) {
        return CompletableFuture.supplyAsync(() -> this.doWith(envId, t, action));
    }

    default <R> CompletionStage<R> asyncWith(String envId, Function<DataEnvironment, R> action, Executor executor) {
        return this.asyncWith(envId, DataEnvironment.class, action, executor);
    }
    default <T, R> CompletionStage<R> asyncWith(String envId, Class<T> t, Function<T, R> action, Executor executor) {
        return CompletableFuture.supplyAsync(() -> this.doWith(envId, t, action), executor);
    }
}
