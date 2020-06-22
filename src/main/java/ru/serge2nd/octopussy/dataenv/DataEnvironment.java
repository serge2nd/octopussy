package ru.serge2nd.octopussy.dataenv;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import ru.serge2nd.octopussy.config.spi.DataSourceProvider;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import static ru.serge2nd.octopussy.config.CommonConfig.*;

@Slf4j
@Getter
@Builder
@RequiredArgsConstructor
public class DataEnvironment implements Closeable {
    private final DataEnvironmentDefinition definition;
    private final DataSource dataSource;
    private final EntityManagerFactory entityManagerFactory;
    private final PlatformTransactionManager transactionManager;

    public DataEnvironment(DataEnvironmentDefinition definition, DataSourceProvider provider) {
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
            tryClose();
            throw e;
        }
    }

    public void tryClose() {
        try {
            this.close();
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        }
    }

    public void close() throws IOException {
        if (entityManagerFactory != null)
            entityManagerFactory.close();

        if (dataSource instanceof Closeable) {
            ((Closeable)dataSource).close();
        }
    }

    private static void set(String mapKey, String value, Map<String, String> map, Properties target) {
        target.setProperty(map.get(mapKey), value);
    }
}
