package ru.serge2nd.octopussy.spi;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Function;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public interface DataKitExecutor {

    <T, R> R apply(String id, Class<T> t, Function<? super T, ? extends R> action);

    default <R> R apply(String id, Function<? super DataKit, ? extends R> action) {
        return this.apply(id, DataKit.class, action);
    }

    default <R> CompletionStage<R> applyAsync(String id, Function<? super DataKit, ? extends R> action) {
        return this.applyAsync(id, DataKit.class, action);
    }
    default <T, R> CompletionStage<R> applyAsync(String id, Class<T> t, Function<? super T, ? extends R> action) {
        return supplyAsync(() -> this.apply(id, t, action));
    }

    default <R> CompletionStage<R> applyAsync(String id, Function<? super DataKit, ? extends R> action, Executor executor) {
        return this.applyAsync(id, DataKit.class, action, executor);
    }
    default <T, R> CompletionStage<R> applyAsync(String id, Class<T> t, Function<? super T, ? extends R> action, Executor executor) {
        return supplyAsync(() -> this.apply(id, t, action), executor);
    }
}
