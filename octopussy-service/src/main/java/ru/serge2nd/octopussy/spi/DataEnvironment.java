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

    DataEnvironmentDefinition  getDefinition();

    <T> T unwrap(Class<T> cls);

    boolean isClosed();
    void    close();

    default DataEnvironmentBuilder toBuilder() { throw new UnsupportedOperationException("toBuilder()"); }

    interface DataEnvironmentBuilder {
        DataEnvironmentBuilder definition(DataEnvironmentDefinition definition);
        DataEnvironment        build();
    }

    static IllegalArgumentException errDataEnvUnwrap(Class<?> src, Class<?> as) {
        return new IllegalArgumentException(format("cannot unwrap %s from %s", as.getName(), src.getName()));
    }
}
