package ru.serge2nd.octopussy.spi;

import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import static ru.serge2nd.octopussy.spi.DataEnvironment.errDataEnvUnwrap;

/**
 * A JPA environment aggregates JPA persisting unit components
 * such as a {@link DataSource}, an {@link EntityManagerFactory}, a {@link PlatformTransactionManager}.<br>
 * The {@link #close()} method destroys the corresponding persistence unit by closing all the components that can be closed.
 */
public interface JpaEnvironment extends DataEnvironment {
    DataSource                 getDataSource();
    EntityManagerFactory       getEntityManagerFactory();
    PlatformTransactionManager getTransactionManager();
}
