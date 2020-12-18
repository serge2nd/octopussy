package ru.serge2nd.octopussy.service;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import ru.serge2nd.octopussy.spi.DataEnvironment;
import ru.serge2nd.octopussy.spi.DataEnvironmentService;
import ru.serge2nd.octopussy.support.DataEnvironmentDefinition;

import java.util.function.Function;

import static ru.serge2nd.octopussy.service.ex.DataEnvironmentException.errDataEnvClosed;

@Slf4j
public class DataEnvironmentProxy implements DataEnvironment {
    private final DataEnvironmentDefinition definition;
    private final DataEnvironmentService service;
    private final Function<DataEnvironmentDefinition, DataEnvironment> dataEnvFactory;

    protected volatile DataEnvironment target;
    protected volatile boolean closed;

    @Builder(toBuilder = true)
    public DataEnvironmentProxy(DataEnvironmentDefinition definition,
                                DataEnvironmentService service,
                                Function<DataEnvironmentDefinition, DataEnvironment> dataEnvFactory) {
        this.definition = definition;
        this.service = service;
        this.dataEnvFactory = dataEnvFactory;
    }

    @Override
    public DataEnvironmentDefinition getDefinition()    { return this.definition; }
    @Override @SuppressWarnings("unchecked")
    public <T> T                     unwrap(Class<T> t) { return DataEnvironmentProxy.class.isAssignableFrom(t) ? (T)this : getTarget().unwrap(t); }

    @Override
    public boolean isClosed() { return this.closed; }
    @Override
    public void close() { if (!isClosed()) {
        String envId = definition.getEnvId();
        log.debug("closing the data env " + envId + "...");

        service.doWith(definition.getEnvId(), DataEnvironmentProxy.class, proxy -> {
            proxy.checkProxyActive(this);

            if (!proxy.isClosed()) {
                proxy.closed = true;
                DataEnvironment target = proxy.target;
                if (target != null) target.close();
            } else {
                log.debug(envId + " was closed by another thread");
            }

            return null;
        });
    }}

    protected DataEnvironment getTarget() {
        checkOpen();
        if (target != null)
            return target;

        String envId = definition.getEnvId();
        log.debug("trying on-demand initialization of the data env " + envId + "...");

        return service.doWith(envId, DataEnvironmentProxy.class, proxy -> {
            proxy.checkProxyActive(this);
            proxy.checkOpen();

            DataEnvironment target = proxy.target;

            if (target == null) {
                proxy.target = target = dataEnvFactory.apply(proxy.definition);
            } else {
                log.debug(envId + " was initialized by another thread");
            }

            return target;
        });
    }

    private void checkOpen() {
        if (isClosed()) throw errDataEnvClosed(definition.getEnvId());
    }
    private void checkProxyActive(DataEnvironment active) {
        if (this != active) throw new IllegalStateException("proxy not active");
    }

    public static class DataEnvironmentProxyBuilder implements DataEnvironmentBuilder {
        // lombok-generated code...
    }
}
