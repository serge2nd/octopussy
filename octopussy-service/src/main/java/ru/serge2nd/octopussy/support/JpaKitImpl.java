package ru.serge2nd.octopussy.support;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.serge2nd.octopussy.spi.JpaKit;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.io.Closeable;
import java.util.function.Function;
import java.util.function.Supplier;

import static lombok.AccessLevel.PROTECTED;

@Slf4j
@Getter
@RequiredArgsConstructor(access = PROTECTED)
public class JpaKitImpl implements JpaKit {
    private final DataKitDefinition definition;
    private final DataSource dataSource;
    private final EntityManagerFactory entityManagerFactory;

    public JpaKitImpl(DataKitDefinition definition,
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
        if (DataSource.class == t)           return (T)getDataSource();
        if (EntityManagerFactory.class == t) return (T)getEntityManagerFactory();
        return JpaKit.super.unwrap(t);
    }

    @Override
    public boolean isClosed() { return entityManagerFactory != null && !entityManagerFactory.isOpen(); }

    @Override
    @SuppressWarnings("unused")
    public void close() { if (!isClosed()) {
        try (Closeable $ = dataSource instanceof Closeable ? (Closeable)dataSource : NOOP) {
            if (entityManagerFactory != null)
                entityManagerFactory.close();
        } catch (Exception e) {
            log.warn("the data kit {} probably already closed ({})", definition.getKitId(), e.getMessage(), e);
        }
    }}

    private static final Closeable NOOP = () -> {};
}
