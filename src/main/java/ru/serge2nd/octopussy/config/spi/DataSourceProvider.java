package ru.serge2nd.octopussy.config.spi;

import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Map;
import java.util.Properties;

public interface DataSourceProvider {

    DataSource getDataSource(Properties props);

    EntityManagerFactory getEntityManagerFactory(DataSource dataSource, Properties props);

    PlatformTransactionManager getTransactionManager(EntityManagerFactory emf);

    Map<String, String> getPropertyNames();
}
