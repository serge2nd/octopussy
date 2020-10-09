package ru.serge2nd.octopussy.service;

import lombok.RequiredArgsConstructor;
import ru.serge2nd.collection.Unmodifiable;
import ru.serge2nd.octopussy.spi.DataEnvironment;
import ru.serge2nd.octopussy.spi.DataEnvironmentService;

import javax.annotation.PreDestroy;
import java.io.Closeable;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static ru.serge2nd.octopussy.service.ex.DataEnvironmentException.errDataEnvExists;
import static ru.serge2nd.octopussy.service.ex.DataEnvironmentException.errDataEnvNotFound;

@RequiredArgsConstructor
public class InMemoryDataEnvironmentService implements DataEnvironmentService, Closeable {
    private final Map<String, DataEnvironment> byId;
    private final DataEnvironment dataEnvPrototype;
    private final UnaryOperator<DataEnvironment> preClose;

    @Override
    public DataEnvironment             get(String envId)  { return find(envId).orElseThrow(()-> errDataEnvNotFound(envId)); }
    @Override
    public Optional<DataEnvironment>   find(String envId) { return ofNullable(byId.get(envId)); }
    @Override
    public Collection<DataEnvironment> getAll()           { return Unmodifiable.ofCollection(byId.values()); }

    @Override
    @SuppressWarnings("unchecked")
    public <T, R> R doWith(String envId, Class<T> t, Function<T, R> action) {
        Object[] result = new Object[1];
        this.compute(dataEnv -> result[0] =
                action.apply(unwrap(ofNullable(dataEnv)
                .orElseThrow(()->errDataEnvNotFound(envId)), t)), envId);
        return (R)result[0];
    }

    @Override
    public DataEnvironment create(DataEnvironment toCreate) {
        String envId = toCreate.getDefinition().getEnvId();
        return this.compute(envId, existing -> ofNullable(
                isNull(existing) ? toCreate : null)
                .map(raw -> dataEnvPrototype.toBuilder()
                        .definition(raw.getDefinition())
                        .build())
                .orElseThrow(()->errDataEnvExists(envId)));
    }

    @Override
    public void delete(String envId) {
        this.computeAndRemove(existing ->
                ofNullable(existing).map(preClose)
                .orElseThrow(()->errDataEnvNotFound(envId))
                .close(), envId);
    }

    @Override @PreDestroy
    public void close() { for (DataEnvironment dataEnv : byId.values()) preClose.apply(dataEnv).close(); }

    protected DataEnvironment computeAndRemove(Consumer<DataEnvironment> consumer, String envId) {
        return this.compute(envId, val -> {consumer.accept(val); return null;});
    }
    protected DataEnvironment compute(Consumer<DataEnvironment> consumer, String envId) {
        return this.compute(envId, val -> {consumer.accept(val); return val;});
    }
    protected DataEnvironment compute(String envId, UnaryOperator<DataEnvironment> mapping) {
        return byId.compute(envId, ($, val) -> mapping.apply(val));
    }

    @SuppressWarnings("unchecked")
    private static <T> T unwrap(DataEnvironment dataEnv, Class<T> t) {
        if (DataEnvironmentProxy.class.isAssignableFrom(t))
            return (T)dataEnv;
        return dataEnv.unwrap(t);
    }
}
