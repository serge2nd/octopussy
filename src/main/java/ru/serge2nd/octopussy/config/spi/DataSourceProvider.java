package ru.serge2nd.octopussy.config.spi;

import org.springframework.transaction.PlatformTransactionManager;
import ru.serge2nd.octopussy.dataenv.DataEnvironment;
import ru.serge2nd.octopussy.dataenv.DataEnvironmentDefinition;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Map;
import java.util.Properties;

public interface DataSourceProvider {

    DataEnvironment getDataEnvironment(DataEnvironmentDefinition definition);

    Map<String, String> getPropertyNames();

    DataSource getDataSource(Properties props);

    EntityManagerFactory getEntityManagerFactory(DataSource dataSource, Properties props);

    PlatformTransactionManager getTransactionManager(EntityManagerFactory emf);
}
