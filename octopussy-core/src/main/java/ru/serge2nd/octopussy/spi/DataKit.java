package ru.serge2nd.octopussy.spi;

import ru.serge2nd.octopussy.support.DataKitDefinition;

import java.io.Closeable;

/**
 * A data kit is something the app can store and query data through.
 * This may be a JPA persistence unit, a message queue, etc.<br>
 * Has a custom static configuration part readable by {@link #getDefinition()}.
 */
public interface DataKit extends Closeable {
    String IFACE_NAME = DataKit.class.getSimpleName();

    /**
     * Gets the fixed immutable definition of this data kit.
     * @see DataKitDefinition
     */
    DataKitDefinition getDefinition();

    void close();
    default boolean isClosed() { throw new UnsupportedOperationException(IFACE_NAME + ".isClosed()"); }

    @SuppressWarnings("unchecked")
    default <T> T unwrap(Class<T> t) {
        if (DataKitDefinition.class == t) return (T)getDefinition();
        if (t.isInstance(this))           return (T)this;
        throw errDataKitUnwrap(getClass(), t);
    }

    default DataKitBuilder toBuilder() { throw new UnsupportedOperationException(IFACE_NAME + ".toBuilder()"); }

    interface DataKitBuilder {
        DataKitBuilder definition(DataKitDefinition definition);
        DataKit        build();
    }

    static IllegalArgumentException errDataKitUnwrap(Class<?> src, Class<?> as) {
        return new IllegalArgumentException("cannot unwrap " + as.getName() + " from " + src.getName());
    }
}
