package ru.serge2nd.octopussy.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.serge2nd.collection.Unmodifiable;
import ru.serge2nd.octopussy.spi.DataKit;
import ru.serge2nd.octopussy.spi.DataKitExecutor;
import ru.serge2nd.octopussy.spi.DataKitService;

import java.io.Closeable;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static ru.serge2nd.octopussy.service.ex.DataKitException.errDataKitExists;
import static ru.serge2nd.octopussy.service.ex.DataKitException.errDataKitNotFound;

/**
 * A {@link DataKitService} backed by the {@link Map} passed to the constructor.
 * The {@link #create(DataKit) create()}, {@link #on(String, Function) on()}, {@link #delete(String) delete()} operations
 * are <i>aimed to be atomic</i>: they use {@link Map#compute(Object, BiFunction)} to insert/pick/remove map entries,
 * so these operations are performed atomically in a blocking way if the underlying map is {@link java.util.concurrent.ConcurrentHashMap} or something like.
 */
@Slf4j
@RequiredArgsConstructor
public class InMemoryDataKits implements DataKitService, DataKitExecutor, Closeable {
    private final Map<String, DataKit> byId;
    private final DataKit prototype;

    @Override
    public DataKit             get(String id)  { return find(id).orElseThrow(()->errDataKitNotFound(id)); }
    @Override
    public Optional<DataKit>   find(String id) { return ofNullable(byId.get(id)); }
    @Override
    public Collection<DataKit> getAll()        { return Unmodifiable.ofCollection(byId.values()); }

    /**
     * Performs the provided action on the {@link DataKit} with the given ID.
     * This operation is <i>aimed to be atomic</i> (see the {@link InMemoryDataKits class-level doc}).
     */
    @Override
    @SuppressWarnings("unchecked")
    public <R> R on(String id, Function<? super DataKit, ? extends R> action) {
        log.trace("attempt to deal with the data kit {}", id);
        Object[] result = new Object[1];
        peek(dataKit -> result[0] =
                action.apply(ofNullable(dataKit)
                .orElseThrow(()->errDataKitNotFound(id))), id);
        return (R)result[0];
    }

    /**
     * Creates a {@link DataKit} using the passed definition.
     * This operation is <i>aimed to be atomic</i> (see the {@link InMemoryDataKits class-level doc}).
     */
    @Override
    public DataKit create(DataKit toCreate) {
        String id = toCreate.getDefinition().getKitId();
        log.trace("attempt to create the data kit {}", id);
        return compute(id, existing -> ofNullable(
                isNull(existing) ? toCreate : null)
                .map(raw -> prototype.toBuilder()
                        .definition(raw.getDefinition())
                        .build())
                .orElseThrow(()->errDataKitExists(id)));
    }

    /**
     * Deletes the {@link DataKit} with the specified ID.
     * This operation is <i>aimed to be atomic</i> (see the {@link InMemoryDataKits class-level doc}).
     */
    @Override
    public void delete(String id) {
        log.trace("attempt to delete the data kit {}", id);
        cut(existing ->
                ofNullable(existing)
                .orElseThrow(()->errDataKitNotFound(id))
                .close(), id);
    }

    @Override
    public void close() {
        log.debug("closing all the data kits...");
        for (DataKit dataKit : byId.values())
            dataKit.close();
    }

    protected DataKit cut(Consumer<DataKit> consumer, String id) {
        return compute(id, val -> {consumer.accept(val); return null;});
    }
    protected DataKit peek(Consumer<DataKit> consumer, String id) {
        return compute(id, val -> {consumer.accept(val); return val;});
    }
    protected DataKit compute(String id, UnaryOperator<DataKit> mapping) {
        return byId.compute(id, ($, val) -> mapping.apply(val));
    }
}
