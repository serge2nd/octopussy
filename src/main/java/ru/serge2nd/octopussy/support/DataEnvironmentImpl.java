package ru.serge2nd.octopussy.support;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import ru.serge2nd.octopussy.spi.DataEnvironment;
import ru.serge2nd.octopussy.spi.DataSourceProvider;
import ru.serge2nd.util.HardProperties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.io.Closeable;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static lombok.AccessLevel.PROTECTED;
import static ru.serge2nd.octopussy.App.*;

@Slf4j
@Getter
@RequiredArgsConstructor(access = PROTECTED)
public class DataEnvironmentImpl implements DataEnvironment {
    private final DataEnvironmentDefinition definition;
    private final DataSource dataSource;
    private final EntityManagerFactory entityManagerFactory;
    private final PlatformTransactionManager transactionManager;

    @Builder
    public DataEnvironmentImpl(DataEnvironmentDefinition definition, DataSourceProvider provider) {
        this.definition = definition;
        Map<String, String> keys = provider.getPropertyNames();
        try {
            this.dataSource = provider.getDataSource(HardProperties.from(
                    singletonMap(keys.get(DATA_ENV_DRIVER_CLASS), definition.getDriverClass()),
                    singletonMap(keys.get(DATA_ENV_URL), definition.getUrl()),
                    singletonMap(keys.get(DATA_ENV_LOGIN), definition.getLogin()),
                    singletonMap(keys.get(DATA_ENV_PASSWORD), definition.getPassword())
            ).toMap());
            this.entityManagerFactory = provider.getEntityManagerFactory(this.dataSource, HardProperties.from(
                    singletonMap(DATA_ENV_DB, definition.getDatabase().toString()),
                    singletonMap(DATA_ENV_ID, definition.getEnvId())
            ).toMap());
            this.transactionManager = provider.getTransactionManager(this.entityManagerFactory);
        } catch (Exception e) {
            this.close();
            throw e;
        }
    }

    @Override
    public boolean isClosed() { return entityManagerFactory != null && !entityManagerFactory.isOpen(); }

    @Override
    public void close() {
        if (!isClosed()) { try {
            try (Closeable ignored = dataSource instanceof Closeable ? (Closeable)dataSource : DUMMY) {
                if (entityManagerFactory != null)
                    entityManagerFactory.close();
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }}
    }

    private static final Closeable DUMMY = () -> {};

    public static class DataEnvironmentImplBuilder implements DataEnvironment.DataEnvironmentBuilder {
        // lombok-generated code...

        @Override
        public DataEnvironmentBuilder copy() {
            return DataEnvironmentImpl.builder()
                    .definition(this.definition)
                    .provider(this.provider);
        }
    }
}
