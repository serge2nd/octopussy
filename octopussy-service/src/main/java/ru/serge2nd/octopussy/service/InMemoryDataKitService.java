package ru.serge2nd.octopussy.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.serge2nd.collection.Unmodifiable;
import ru.serge2nd.octopussy.spi.DataKit;
import ru.serge2nd.octopussy.spi.DataKitService;

import java.io.Closeable;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static ru.serge2nd.octopussy.service.ex.DataKitException.errDataKitExists;
import static ru.serge2nd.octopussy.service.ex.DataKitException.errDataKitNotFound;

@Slf4j
@RequiredArgsConstructor
public class InMemoryDataKitService implements DataKitService, Closeable {
    private final Map<String, DataKit> byId;
    private final DataKit dataKitPrototype;

    @Override
    public DataKit             get(String kitId)  { return find(kitId).orElseThrow(()->errDataKitNotFound(kitId)); }
    @Override
    public Optional<DataKit>   find(String kitId) { return ofNullable(byId.get(kitId)); }
    @Override
    public Collection<DataKit> getAll()           { return Unmodifiable.ofCollection(byId.values()); }

    @Override
    @SuppressWarnings("unchecked")
    public <T, R> R doWith(String kitId, Class<T> t, Function<? super T, R> action) {
        Object[] result = new Object[1];
        peek(dataKit -> result[0] =
                action.apply(ofNullable(dataKit)
                .orElseThrow(()->errDataKitNotFound(kitId))
                .unwrap(t)), kitId);
        return (R)result[0];
    }

    @Override
    public DataKit create(DataKit toCreate) {
        String kitId = toCreate.getDefinition().getKitId();
        return compute(kitId, existing -> ofNullable(
                isNull(existing) ? toCreate : null)
                .map(raw -> dataKitPrototype.toBuilder()
                        .definition(raw.getDefinition())
                        .build())
                .orElseThrow(()->errDataKitExists(kitId)));
    }

    @Override
    public void delete(String kitId) {
        cut(existing ->
                ofNullable(existing)
                .orElseThrow(()->errDataKitNotFound(kitId))
                .close(), kitId);
    }

    @Override
    public void close() {
        log.debug("closing all the data kits...");
        for (DataKit dataKit : byId.values())
            dataKit.close();
    }

    protected DataKit cut(Consumer<DataKit> consumer, String kitId) {
        return this.compute(kitId, val -> {consumer.accept(val); return null;});
    }
    protected DataKit peek(Consumer<DataKit> consumer, String kitId) {
        return this.compute(kitId, val -> {consumer.accept(val); return val;});
    }
    protected DataKit compute(String kitId, UnaryOperator<DataKit> mapping) {
        return byId.compute(kitId, ($, val) -> mapping.apply(val));
    }
}
