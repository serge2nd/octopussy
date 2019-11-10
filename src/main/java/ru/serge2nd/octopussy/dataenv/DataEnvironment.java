package ru.serge2nd.octopussy.dataenv;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.io.Closeable;
import java.io.IOException;

@Getter
@Builder
@RequiredArgsConstructor
public class DataEnvironment implements Closeable {
    private final DataEnvironmentDefinition definition;
    private final DataSource dataSource;
    private final EntityManager entityManager;
    private final PlatformTransactionManager transactionManager;

    public void close() throws IOException {
        EntityManagerFactory entityManagerFactory = entityManager.getEntityManagerFactory();
        entityManagerFactory.close();

        if (dataSource instanceof Closeable) {
            ((Closeable)dataSource).close();
        }
    }
}
