package ru.serge2nd.octopussy.spi;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Function;

/**
 * Creates, obtains, performs actions on, removes {@link DataKit} instances.
 */
public interface DataKitService {

    DataKit             get(String kitId);

    Optional<DataKit>   find(String kitId);

    Collection<DataKit> getAll();

    DataKit create(DataKit toCreate);

    void delete(String kitId);

    default <R> R doWith(String kitId, Function<DataKit, R> action) {
        return this.doWith(kitId, DataKit.class, action);
    }
    default <T, R> R doWith(String kitId, Class<T> t, Function<? super T, R> action) {
        return action.apply(this.get(kitId).unwrap(t));
    }

    default <R> CompletionStage<R> asyncWith(String kitId, Function<DataKit, R> action) {
        return this.asyncWith(kitId, DataKit.class, action);
    }
    default <T, R> CompletionStage<R> asyncWith(String kitId, Class<T> t, Function<? super T, R> action) {
        return CompletableFuture.supplyAsync(() -> this.doWith(kitId, t, action));
    }

    default <R> CompletionStage<R> asyncWith(String kitId, Function<DataKit, R> action, Executor executor) {
        return this.asyncWith(kitId, DataKit.class, action, executor);
    }
    default <T, R> CompletionStage<R> asyncWith(String kitId, Class<T> t, Function<? super T, R> action, Executor executor) {
        return CompletableFuture.supplyAsync(() -> this.doWith(kitId, t, action), executor);
    }
}
