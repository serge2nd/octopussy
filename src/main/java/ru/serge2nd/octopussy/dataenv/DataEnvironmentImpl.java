package ru.serge2nd.octopussy.dataenv;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import ru.serge2nd.octopussy.config.spi.DataSourceProvider;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.io.Closeable;
import java.util.Map;
import java.util.Properties;

import static lombok.AccessLevel.PROTECTED;
import static ru.serge2nd.octopussy.config.CommonConfig.*;

@Slf4j
@Getter
@RequiredArgsConstructor(access = PROTECTED)
public class DataEnvironmentImpl implements DataEnvironment {
    private final DataEnvironmentDefinition definition;
    private final DataSource dataSource;
    private final EntityManagerFactory entityManagerFactory;
    private final PlatformTransactionManager transactionManager;

    public DataEnvironmentImpl(DataEnvironmentDefinition definition, DataSourceProvider provider) {
        this.definition = definition;
        Map<String, String> propNames = provider.getPropertyNames();
        try {
            this.dataSource = provider.getDataSource(new Properties() {{
                set(DATA_ENV_DRIVER_CLASS, definition.getDriverClass(),  propNames, this);
                set(DATA_ENV_URL         , definition.getUrl(),          propNames, this);
                set(DATA_ENV_LOGIN       , definition.getLogin(),        propNames, this);
                set(DATA_ENV_PASSWORD    , definition.getPassword(),     propNames, this);
            }});
            this.entityManagerFactory = provider.getEntityManagerFactory(this.dataSource, new Properties() {{
                setProperty(DATA_ENV_DB, definition.getDatabase().toString());
                setProperty(DATA_ENV_ID, definition.getEnvId());
            }});
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

    private static void set(String mapKey, String value, Map<String, String> map, Properties target) {
        target.setProperty(map.get(mapKey), value);
    }
}
