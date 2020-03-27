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
import java.util.Properties;

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
        try {
            this.dataSource = provider.getDataSource(new Properties() {{
                setProperty(provider.driverClassPropertyName(), definition.getDriverClass());
                setProperty(provider.urlPropertyName(), definition.getUrl());
                setProperty(provider.loginPropertyName(), definition.getLogin());
                setProperty(provider.passwordPropertyName(), definition.getPassword());
            }});
            this.entityManagerFactory = provider.getEntityManagerFactory(this.dataSource, new Properties() {{
                setProperty(provider.dbPropertyName(), definition.getDatabase().toString());
                setProperty(provider.envIdPropertyName(), definition.getEnvId());
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
}
