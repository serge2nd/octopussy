package ru.serge2nd.octopussy.spi;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

/**
 * A JPA environment aggregates JPA persisting unit components such as a {@link DataSource}, an {@link EntityManagerFactory}.<br>
 * The {@link #close()} method destroys the corresponding persistence unit by closing all the components that can be closed.
 */
public interface JpaEnvironment extends DataEnvironment {
    DataSource                 getDataSource();
    EntityManagerFactory       getEntityManagerFactory();
}
