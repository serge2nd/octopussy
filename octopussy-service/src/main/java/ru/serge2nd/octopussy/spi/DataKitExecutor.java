package ru.serge2nd.octopussy.spi;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Function;

import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * Performs custom actions on {@link DataKit} instances being chosen by ID.
 * Actions can be executed in the current thread or using the provided {@link Executor}.<br>
 * Note that {@link #on(String, Function) on()} call may block the calling thread
 * until the selected resource is available.
 */
public interface DataKitExecutor {

    <R> R on(String id, Function<? super DataKit, ? extends R> action);

    default <T, R> R on(String id, Class<T> target, Function<? super T, ? extends R> action) {
        return on(id, dataKit -> action.apply(dataKit.unwrap(target)));
    }

    default <R> CompletionStage<R> let(String id, Function<? super DataKit, ? extends R> action) {
        return supplyAsync(() -> on(id, action));
    }
    default <T, R> CompletionStage<R> let(String id, Class<T> target, Function<? super T, ? extends R> action) {
        return supplyAsync(() -> on(id, target, action));
    }

    default <R> CompletionStage<R> let(String id, Function<? super DataKit, ? extends R> action, Executor executor) {
        return supplyAsync(() -> on(id, action), executor);
    }
    default <T, R> CompletionStage<R> let(String id, Class<T> target, Function<? super T, ? extends R> action, Executor executor) {
        return supplyAsync(() -> on(id, target, action), executor);
    }
}
