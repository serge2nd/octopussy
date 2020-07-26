package ru.serge2nd.octopussy.spi;

import org.springframework.transaction.PlatformTransactionManager;
import ru.serge2nd.octopussy.support.DataEnvironmentDefinition;

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

    interface DataEnvironmentBuilder {
        DataEnvironmentBuilder definition(DataEnvironmentDefinition definition);
        DataEnvironment build();
        DataEnvironmentBuilder copy();
    }
}
