package ru.serge2nd.octopussy.dataenv;

import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.io.Closeable;

public interface DataEnvironment extends Closeable {

    DataEnvironmentDefinition getDefinition();

    default boolean isClosed() { return true; }

    void close();
    DataSource getDataSource();
    EntityManagerFactory getEntityManagerFactory();
    PlatformTransactionManager getTransactionManager();
}
