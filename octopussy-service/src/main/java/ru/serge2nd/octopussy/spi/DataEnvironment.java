package ru.serge2nd.octopussy.spi;

import ru.serge2nd.octopussy.support.DataEnvironmentDefinition;

import java.io.Closeable;

import static java.lang.String.format;

/**
 * A data environment is something through that the app can store and query data.
 * This may be a JPA persistence unit, a message queue, etc.<br>
 * Has a custom configuration part readable via the {@link #getDefinition()} call.
 */
public interface DataEnvironment extends Closeable {
    String IFACE_NAME = DataEnvironment.class.getSimpleName();

    DataEnvironmentDefinition  getDefinition();

    void close();
    default boolean isClosed() { throw new UnsupportedOperationException(IFACE_NAME + ".isClosed()"); }

    default <T> T                  unwrap(Class<T> t) { if (t.isInstance(this)) return t.cast(this); throw errDataEnvUnwrap(getClass(), t); }
    default DataEnvironmentBuilder toBuilder()        { throw new UnsupportedOperationException(IFACE_NAME + ".toBuilder()"); }

    interface DataEnvironmentBuilder {
        DataEnvironmentBuilder definition(DataEnvironmentDefinition definition);
        DataEnvironment        build();
    }

    static IllegalArgumentException errDataEnvUnwrap(Class<?> src, Class<?> as) {
        return new IllegalArgumentException(format("cannot unwrap %s from %s", as.getName(), src.getName()));
    }
}
