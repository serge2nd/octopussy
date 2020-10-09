package ru.serge2nd.octopussy.service;

import lombok.Builder;
import ru.serge2nd.octopussy.spi.DataEnvironment;
import ru.serge2nd.octopussy.spi.DataEnvironmentService;
import ru.serge2nd.octopussy.support.DataEnvironmentDefinition;

import java.util.function.Function;
import java.util.function.Supplier;

import static ru.serge2nd.octopussy.service.ex.DataEnvironmentException.errDataEnvClosed;

public class DataEnvironmentProxy implements DataEnvironment {
    private final DataEnvironmentDefinition definition;
    private final Supplier<DataEnvironmentService> service;
    private final Function<DataEnvironmentDefinition, DataEnvironment> dataEnvFactory;

    protected volatile DataEnvironment target;
    protected volatile boolean closed;

    @Builder(toBuilder = true)
    public DataEnvironmentProxy(DataEnvironmentDefinition definition,
                                Supplier<DataEnvironmentService> service,
                                Function<DataEnvironmentDefinition, DataEnvironment> dataEnvFactory) {
        this.definition = definition;
        this.service = service;
        this.dataEnvFactory = dataEnvFactory;
    }

    @Override public DataEnvironmentDefinition getDefinition()           { return this.definition; }
    @Override public <T> T                     unwrap(Class<T> cls)      { return getTarget().unwrap(cls); }
    @Override public boolean                   isClosed()                { return this.closed; }
    @Override public void                      close()                   { if (!isClosed()) {
        service.get().doWith(definition.getEnvId(), DataEnvironmentProxy.class, proxy -> {
            proxy.checkProxyActive(this);

            if (!proxy.isClosed()) {
                proxy.closed = true;
                DataEnvironment target = proxy.target;
                if (target != null) target.close();
            }

            return null;
        });
    }}

    protected DataEnvironment getTarget() {
        checkOpen();
        if (target != null)
            return target;

        return service.get().doWith(definition.getEnvId(), DataEnvironmentProxy.class, proxy -> {
            proxy.checkProxyActive(this);
            proxy.checkOpen();

            DataEnvironment target = proxy.target;

            if (target == null)
                proxy.target = target = dataEnvFactory.apply(proxy.definition);

            return target;
        });
    }

    private void checkOpen() {
        if (isClosed()) throw errDataEnvClosed(definition.getEnvId());
    }
    private void checkProxyActive(DataEnvironment active) {
        if (this != active) throw new IllegalStateException("proxy not active");
    }

    public static class DataEnvironmentProxyBuilder implements DataEnvironment.DataEnvironmentBuilder {
        // lombok-generated code...
    }
}
