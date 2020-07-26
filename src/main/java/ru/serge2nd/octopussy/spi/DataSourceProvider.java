package ru.serge2nd.octopussy.spi;

import org.springframework.transaction.PlatformTransactionManager;
import ru.serge2nd.octopussy.support.DataEnvironmentDefinition;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Map;

public interface DataSourceProvider {

    DataEnvironment getDataEnvironment(DataEnvironmentDefinition definition);

    DataEnvironmentService getDataEnvironmentService();

    Map<String, String> getPropertyNames();

    DataSource getDataSource(Map<String, String> props);

    EntityManagerFactory getEntityManagerFactory(DataSource dataSource, Map<String, String> props);

    PlatformTransactionManager getTransactionManager(EntityManagerFactory emf);
}
