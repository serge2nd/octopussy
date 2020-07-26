package ru.serge2nd.octopussy.service;

import lombok.Builder;
import org.springframework.transaction.PlatformTransactionManager;
import ru.serge2nd.octopussy.service.ex.DataEnvironmentClosedException;
import ru.serge2nd.octopussy.spi.DataEnvironment;
import ru.serge2nd.octopussy.spi.DataSourceProvider;
import ru.serge2nd.octopussy.support.DataEnvironmentDefinition;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

public class DataEnvironmentProxy implements DataEnvironment {
    private final DataEnvironmentDefinition definition;
    private final DataSourceProvider provider;

    private volatile DataEnvironment target;
    private volatile boolean closed;

    @Builder
    public DataEnvironmentProxy(DataEnvironmentDefinition definition, DataSourceProvider provider) {
        this.definition = definition;
        this.provider = provider;
    }

    @Override public DataEnvironmentDefinition getDefinition() { return this.definition; }
    @Override public DataSource getDataSource() { return getTarget().getDataSource(); }
    @Override public EntityManagerFactory getEntityManagerFactory() { return getTarget().getEntityManagerFactory(); }
    @Override public PlatformTransactionManager getTransactionManager() { return getTarget().getTransactionManager(); }
    @Override public boolean isClosed() { return this.closed; }
    @Override
    public void close() {
        if (!isClosed()) {
            provider.getDataEnvironmentService().doWith(definition.getEnvId(), val -> {
                this.checkProxyActive(val);

                if (!isClosed()) {
                    this.closed = true;
                    DataEnvironment target = this.target;
                    if (target != null) target.close();
                }

                return null;
            });
        }
    }

    protected DataEnvironment getTarget() {
        checkOpen();

        return provider.getDataEnvironmentService().doWith(definition.getEnvId(), val -> {
            this.checkProxyActive(val);
            this.checkOpen();

            DataEnvironment target = this.target;

            if (target == null)
                this.target = target = provider.getDataEnvironment(this.definition);

            return target;
        });
    }

    private void checkOpen() {
        if (isClosed()) throw new DataEnvironmentClosedException(definition.getEnvId());
    }
    private void checkProxyActive(DataEnvironment active) {
        if (this != active) throw new IllegalStateException("proxy not active");
    }

    public static class DataEnvironmentProxyBuilder implements DataEnvironment.DataEnvironmentBuilder {
        // lombok-generated code...

        @Override
        public DataEnvironmentProxyBuilder copy() {
            return new DataEnvironmentProxyBuilder()
                    .definition(this.definition)
                    .provider(this.provider);
        }
    }
}
