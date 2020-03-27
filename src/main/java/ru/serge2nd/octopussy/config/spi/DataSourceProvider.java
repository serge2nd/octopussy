package ru.serge2nd.octopussy.config.spi;

import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

public interface DataSourceProvider {

    String envIdPropertyName();
    String dbPropertyName();
    String driverClassPropertyName();
    String urlPropertyName();
    String loginPropertyName();
    String passwordPropertyName();

    DataSource getDataSource(Properties props);

    EntityManagerFactory getEntityManagerFactory(DataSource dataSource, Properties props);

    PlatformTransactionManager getTransactionManager(EntityManagerFactory emf);
}
