package ru.serge2nd.octopussy.service;

import org.springframework.beans.BeansException;
import org.springframework.cache.annotation.CacheEvict;
import ru.serge2nd.octopussy.spi.DataEnvironment;
import ru.serge2nd.octopussy.spi.DataEnvironment.DataEnvironmentBuilder;
import ru.serge2nd.octopussy.spi.DataEnvironmentService;
import ru.serge2nd.octopussy.service.ex.DataEnvironmentExistsException;
import ru.serge2nd.octopussy.service.ex.DataEnvironmentNotFoundException;

import javax.annotation.PreDestroy;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.*;

import static java.util.Collections.unmodifiableCollection;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static ru.serge2nd.octopussy.App.QUERY_ADAPTERS_CACHE;

public class InMemoryDataEnvironmentService implements DataEnvironmentService, Closeable {

    private final Map<String, DataEnvironment> byId;
    private final DataEnvironmentBuilder dataEnvBuilder;

    public InMemoryDataEnvironmentService(Map<String, DataEnvironment> repository,
                                          DataEnvironmentBuilder dataEnvBuilder) {
        this.byId = repository;
        this.dataEnvBuilder = dataEnvBuilder;
    }

    @Override
    public DataEnvironment get(String envId) {
        return find(envId).orElseThrow(() -> new DataEnvironmentNotFoundException(envId));
    }

    @Override
    public Optional<DataEnvironment> find(String envId) {
        return ofNullable(byId.get(envId));
    }

    @Override
    public Collection<DataEnvironment> getAll() {
        return unmodifiableCollection(new ArrayList<>(byId.values()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> R doWith(String envId, Function<DataEnvironment, R> action) {
        Object[] result = new Object[1];
        this.compute(dataEnv -> result[0] = action.apply(dataEnv), envId);
        return (R)result[0];
    }

    @Override
    public DataEnvironment create(DataEnvironment toCreate) throws BeansException {
        String envId = toCreate.getDefinition().getEnvId();

        return this.compute(envId, existing -> ofNullable(
                isNull(existing) ? toCreate : null)
                .map(DataEnvironment::getDefinition)
                .map(def -> dataEnvBuilder.copy()
                        .definition(def)
                        .build())
                .orElseThrow(() -> new DataEnvironmentExistsException(envId)));
    }

    @Override
    @CacheEvict(value = QUERY_ADAPTERS_CACHE, beforeInvocation = true)
    public void delete(String envId) {
        this.computeAndRemove(existing -> ofNullable(existing)
                .orElseThrow(() -> new DataEnvironmentNotFoundException(envId))
                .close(), envId);
    }

    @Override
    @PreDestroy
    public void close() {
        for (DataEnvironment dataEnv : byId.values())
            dataEnv.close();
    }

    protected DataEnvironment computeAndRemove(Consumer<DataEnvironment> consumer, String envId) {
        return this.compute(envId, val -> {consumer.accept(val); return null;});
    }
    protected DataEnvironment compute(Consumer<DataEnvironment> consumer, String envId) {
        return this.compute(envId, val -> {consumer.accept(val); return val;});
    }
    protected DataEnvironment compute(String envId, UnaryOperator<DataEnvironment> mapping) {
        return byId.compute(envId, ($, val) -> mapping.apply(val));
    }
}
