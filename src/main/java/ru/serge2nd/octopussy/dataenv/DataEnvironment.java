package ru.serge2nd.octopussy.dataenv;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import ru.serge2nd.octopussy.config.properties.HikariProperties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.io.Closeable;
import java.io.IOException;

@Slf4j
@Getter
@Builder
@RequiredArgsConstructor
public class DataEnvironment implements Closeable {
    private final DataEnvironmentDefinition definition;
    private final DataSource dataSource;
    private final EntityManagerFactory entityManagerFactory;
    private final PlatformTransactionManager transactionManager;

    public DataEnvironment(DataEnvironmentDefinition definition, HikariProperties hikariProps) {
        this.definition = definition;
        try {
            this.dataSource = buildDataSource(hikariProps);
            this.entityManagerFactory = buildEntityManagerFactory(buildJpaVendorAdapter(definition.getDatabase()));
            this.transactionManager = buildTransactionManager();
        } catch (Exception e) {
            tryClose();
            throw e;
        }
    }

    private DataSource buildDataSource(HikariProperties hikariProps) {
        HikariConfig hikariConfig = new HikariConfig(hikariProps);
        hikariConfig.setJdbcUrl(definition.getUrl());
        hikariConfig.setDriverClassName(definition.getDriverClass());
        hikariConfig.setUsername(definition.getLogin());
        hikariConfig.setPassword(definition.getPassword());

        return new HikariDataSource(hikariConfig);
    }

    private JpaVendorAdapter buildJpaVendorAdapter(Database database) {
        HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        jpaVendorAdapter.setDatabase(database);
        return jpaVendorAdapter;
    }

    private EntityManagerFactory buildEntityManagerFactory(JpaVendorAdapter jpaVendorAdapter) {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();

        emf.setDataSource(dataSource);
        emf.setJpaVendorAdapter(jpaVendorAdapter);
        emf.setPersistenceUnitName(definition.getEnvId() + "PersistenceUnit");
        emf.setPackagesToScan("ru.serge2nd.octopussy.data");

        emf.afterPropertiesSet();
        return emf.getObject();
    }

    private PlatformTransactionManager buildTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;
    }

    public void tryClose() {
        try {
            this.close();
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        }
    }

    public void close() throws IOException {
        if (entityManagerFactory != null)
            entityManagerFactory.close();

        if (dataSource instanceof Closeable) {
            ((Closeable)dataSource).close();
        }
    }
}
