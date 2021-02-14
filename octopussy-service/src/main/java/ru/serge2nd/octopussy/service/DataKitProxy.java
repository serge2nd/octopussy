package ru.serge2nd.octopussy.service;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import ru.serge2nd.octopussy.spi.DataKit;
import ru.serge2nd.octopussy.spi.DataKitExecutor;
import ru.serge2nd.octopussy.support.DataKitDefinition;

import java.util.function.Function;

import static ru.serge2nd.octopussy.service.ex.DataKitException.errDataKitClosed;

@Slf4j
public class DataKitProxy implements DataKit {
    private final DataKitDefinition definition;
    private final DataKitExecutor executor;
    private final Function<DataKitDefinition, DataKit> dataKitFactory;

    protected volatile DataKit target;
    protected volatile boolean closed;

    @Builder(toBuilder = true)
    public DataKitProxy(DataKitDefinition definition,
                        DataKitExecutor executor,
                        Function<DataKitDefinition, DataKit> dataKitFactory) {
        this.definition = definition;
        this.executor = executor;
        this.dataKitFactory = dataKitFactory;
    }

    @Override
    public DataKitDefinition getDefinition()    { return this.definition; }
    @Override
    public <T>             T unwrap(Class<T> t) { return DataKitProxy.class.isAssignableFrom(t) ? t.cast(this) : getTarget().unwrap(t); }
    @Override
    public boolean           isClosed()         { return this.closed; }
    @Override
    public void              close()            {
        if (!isClosed()) {
            String id = definition.getKitId();
            log.debug("closing the data kit " + id + "...");

            executor.apply(id, DataKitProxy.class, proxy -> {
                proxy.checkProxyActive(this, id);

                if (!proxy.isClosed()) {
                    proxy.closed = true;
                    DataKit target = proxy.target;
                    if (target != null) target.close();
                } else {
                    log.debug(id + " was closed by another thread");
                }

                return null;
            });
        }
    }

    protected DataKit getTarget() {
        checkOpen();
        if (target != null) return target;

        String id = definition.getKitId();
        log.debug("trying on-demand initialization of the data kit " + id + "...");

        return executor.apply(id, DataKitProxy.class, proxy -> {
            proxy.checkProxyActive(this, id);
            proxy.checkOpen();

            DataKit target = proxy.target;

            if (target == null) {
                proxy.target = target = dataKitFactory.apply(proxy.definition);
            } else {
                log.debug(id + " was initialized by another thread");
            }

            return target;
        });
    }

    private void checkOpen() {
        if (isClosed()) throw errDataKitClosed(definition.getKitId());
    }
    private void checkProxyActive(DataKit active, String id) {
        if (this != active) throw new IllegalStateException(id + ": proxy not active");
    }

    public static class DataKitProxyBuilder implements DataKitBuilder {
        // lombok-generated code...
    }
}
