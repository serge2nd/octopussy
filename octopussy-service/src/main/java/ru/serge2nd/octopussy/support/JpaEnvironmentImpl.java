package ru.serge2nd.octopussy.support;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.serge2nd.octopussy.spi.JpaEnvironment;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.io.Closeable;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.lang.String.format;
import static lombok.AccessLevel.PROTECTED;
import static ru.serge2nd.octopussy.spi.DataEnvironment.errDataEnvUnwrap;

@Slf4j
@Getter
@RequiredArgsConstructor(access = PROTECTED)
public class JpaEnvironmentImpl implements JpaEnvironment {
    private final DataEnvironmentDefinition definition;
    private final DataSource dataSource;
    private final EntityManagerFactory entityManagerFactory;

    public JpaEnvironmentImpl(DataEnvironmentDefinition definition,
                              Supplier<DataSource> dataSource,
                              Function<DataSource, EntityManagerFactory> emfProvider) {
        try {
            this.dataSource = dataSource.get();
            this.entityManagerFactory = emfProvider.apply(this.dataSource);
        } catch (Exception e) {
            this.close();
            throw e;
        }
        this.definition = definition;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T unwrap(Class<T> t) {
        if (t.isAssignableFrom(this.getClass())) return (T)this;
        if (DataSource.class == t)               return (T)getDataSource();
        if (EntityManagerFactory.class == t)     return (T)getEntityManagerFactory();
        throw errDataEnvUnwrap(this.getClass(), t);
    }

    @Override
    public boolean isClosed() { return entityManagerFactory != null && !entityManagerFactory.isOpen(); }

    @Override @SuppressWarnings("unused")
    public void close() {
        if (!isClosed()) { try {
            try (Closeable $ = dataSource instanceof Closeable ? (Closeable)dataSource : NOOP) {
                if (entityManagerFactory != null)
                    entityManagerFactory.close();
            }
        } catch (Exception e) {
            log.warn(format("data env %s probably already closed (%s)", getDefinition().getEnvId(), e.getMessage()), e);
        }}
    }

    private static final Closeable NOOP = () -> {};
}
