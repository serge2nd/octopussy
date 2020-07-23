package ru.serge2nd.octopussy.dataenv;

import org.springframework.beans.BeansException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import ru.serge2nd.octopussy.config.spi.DataSourceProvider;

import javax.annotation.PreDestroy;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static ru.serge2nd.octopussy.config.CommonConfig.QUERY_ADAPTERS_CACHE;

@Service
public class InMemoryDataEnvironmentService implements DataEnvironmentService, Closeable {

    private final Map<String, DataEnvironment> byId;
    private final DataSourceProvider dataSourceProvider;

    public InMemoryDataEnvironmentService(DataSourceProvider dataSourceProvider,
                                          Supplier<Map<String, DataEnvironment>> repositoryProvider) {
        this.byId = repositoryProvider.get();
        this.dataSourceProvider = dataSourceProvider;
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
    public DataEnvironment create(DataEnvironment toCreate) throws BeansException {
        String envId = toCreate.getDefinition().getEnvId();

        return this.compute(envId, existing -> ofNullable(
                isNull(existing) ? toCreate : null)
                .map(DataEnvironment::getDefinition)
                .map(DataEnvironmentProxy::new)
                .orElseThrow(() -> new DataEnvironmentExistsException(envId)));
    }

    @Override
    @CacheEvict(value = QUERY_ADAPTERS_CACHE, beforeInvocation = true)
    public void delete(String envId) {
        this.compute(existing -> ofNullable(existing)
                .orElseThrow(() -> new DataEnvironmentNotFoundException(envId))
                .close(), envId);
    }

    @Override
    @PreDestroy
    public void close() {
        for (DataEnvironment dataEnv : byId.values())
            dataEnv.close();
    }

    protected DataEnvironment compute(Consumer<DataEnvironment> consumer, String envId) {
        return this.compute(envId, existing -> {consumer.accept(existing); return null;});
    }

    protected DataEnvironment compute(String envId, UnaryOperator<DataEnvironment> mapping) {
        return byId.compute(envId, ($, existing) -> mapping.apply(existing));
    }

    protected DataEnvironment getProxyTarget(String envId) {
        DataEnvironment[] t = new DataEnvironment[1];

        byId.compute(envId, ($, val) -> {
            if (!(val instanceof DataEnvironmentProxy))
                throw new IllegalStateException("calling getTarget() on a non-persistent proxy");

            DataEnvironmentProxy proxy = (DataEnvironmentProxy)val;
            t[0] = proxy.target;

            if (t[0] == null)
                proxy.target = t[0] = dataSourceProvider.getDataEnvironment(proxy.getDefinition());

            return proxy;
        });

        return t[0];
    }

    class DataEnvironmentProxy implements DataEnvironment {
        final DataEnvironmentDefinition def;
        volatile DataEnvironment target;
        volatile boolean closed;

        DataEnvironmentProxy(DataEnvironmentDefinition def) { this.def = def; }

        DataEnvironment getTarget() {
            if (isClosed())
                throw new IllegalStateException(format("data environment %s is closed", def.getEnvId()));
            return getProxyTarget(def.getEnvId());
        }

        @Override public DataEnvironmentDefinition getDefinition() { return this.def; }
        @Override public DataSource getDataSource() { return getTarget().getDataSource(); }
        @Override public EntityManagerFactory getEntityManagerFactory() { return getTarget().getEntityManagerFactory(); }
        @Override public PlatformTransactionManager getTransactionManager() { return getTarget().getTransactionManager(); }
        @Override public boolean isClosed() { return this.closed; }
        @Override
        public void close() {
            if (!isClosed()) {
                this.closed = true;
                DataEnvironment target = this.target;
                if (target != null) target.close();
            }
        }
    }
}
