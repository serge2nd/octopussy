package ru.serge2nd.octopussy.spi;

import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Map;

public interface PersistenceUnitProvider {

    DataSource                 getDataSource(Map<String, Object> props);

    EntityManagerFactory       getEntityManagerFactory(DataSource dataSource, Map<String, Object> props);

    PlatformTransactionManager getTransactionManager(EntityManagerFactory emf);
}
