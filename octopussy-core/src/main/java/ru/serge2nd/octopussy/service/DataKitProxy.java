package ru.serge2nd.octopussy.service;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import ru.serge2nd.octopussy.spi.DataKit;
import ru.serge2nd.octopussy.spi.DataKitExecutor;
import ru.serge2nd.octopussy.support.DataKitDefinition;

import java.util.function.Function;

import static ru.serge2nd.octopussy.service.ex.DataKitException.errDataKitClosed;

/**
 * A {@link DataKit} proxy lazily instantiating its target.
 */
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
    public final DataKitDefinition getDefinition()    { return this.definition; }
    @Override
    public final boolean           isClosed()         { return this.closed; }
    @Override
    public final <T> T             unwrap(Class<T> t) { return DataKitProxy.class.isAssignableFrom(t) ? t.cast(this) : getTarget().unwrap(t); }

    @Override
    public void close() {
        if (!isClosed()) {
            String id = definition.getKitId();
            log.trace("acquiring the data kit {} to close...", id);
            executor.on(id, DataKitProxy.class, p -> {doClose(p); return null;});
        }
    }
    /** Run in {@link #executor} */
    protected void doClose(DataKitProxy proxy) {
        String id = definition.getKitId();
        proxy.checkSame(this, id);

        if (!proxy.isClosed()) {
            log.debug("closing the data kit {}...", id);
            proxy.closed = true;
            DataKit target = proxy.target;
            if (target != null) target.close();
        } else {
            log.trace("{} was closed by another thread", id);
        }
    }

    protected DataKit getTarget() {
        checkOpen();
        if (target != null) return target;

        String id = definition.getKitId();
        log.trace("acquiring the data kit {} to init...", id);
        return executor.on(id, DataKitProxy.class, this::doGetTarget);
    }
    /** Run in {@link #executor} */
    protected DataKit doGetTarget(DataKitProxy proxy) {
        String id = definition.getKitId();
        proxy.checkSame(this, id);
        proxy.checkOpen();

        DataKit target = proxy.target;

        if (target == null) {
            log.debug("initializing the data kit {}...", id);
            proxy.target = target = dataKitFactory.apply(proxy.definition);
        } else {
            log.trace("{} was initialized by another thread", id);
        }

        return target;
    }

    private void checkOpen() {
        if (isClosed()) throw errDataKitClosed(definition.getKitId());
    }
    private void checkSame(DataKit active, String id) {
        if (this != active) throw new IllegalStateException(id + ": multiple proxies");
    }

    public static class DataKitProxyBuilder implements DataKitBuilder {
        // lombok-generated code...
    }
}
