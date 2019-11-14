package ru.serge2nd.octopussy.dataenv;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import ru.serge2nd.octopussy.config.adapter.ApplicationContextAdapter;
import ru.serge2nd.octopussy.config.properties.HikariProperties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.SynchronizationType;
import javax.sql.DataSource;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

import static ru.serge2nd.octopussy.config.CommonConfig.QUERY_ADAPTERS_CACHE;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataEnvironmentServiceImpl implements DataEnvironmentService {
    private static final String DATA_ENV_SUFFIX = "DataEnvironment";

    private final ApplicationContextAdapter ctx;
    private final HikariProperties hikariProps;

    @Override
    public DataEnvironment get(String envId) {
        try {
            return ctx.getBean(dataEnvName(envId), DataEnvironment.class);
        } catch (NoSuchBeanDefinitionException | BeanNotOfRequiredTypeException e) {
            throw new DataEnvironmentNotFoundException(envId);
        }
    }

    @Override
    public Optional<DataEnvironment> find(String envId) {
        try {
            return Optional.of(ctx.getBean(dataEnvName(envId), DataEnvironment.class));
        } catch (NoSuchBeanDefinitionException | BeanNotOfRequiredTypeException e) {
            return Optional.empty();
        }
    }

    @Override
    public Collection<DataEnvironment> getAll() {
        return ctx.getBeans(DataEnvironment.class);
    }

    @Override
    public DataEnvironment create(DataEnvironment toCreate) throws BeansException {
        DataEnvironmentDefinition definition = toCreate.getDefinition();
        String envId = definition.getEnvId();

        DataSource dataSource = buildDataSource(definition);
        JpaVendorAdapter jpaVendorAdapter = buildJpaVendorAdapter(definition.getDatabase());

        EntityManagerFactory entityManagerFactory = buildEntityManagerFactory(envId, dataSource, jpaVendorAdapter);
        EntityManager entityManager = entityManagerFactory.createEntityManager(SynchronizationType.SYNCHRONIZED);
        PlatformTransactionManager transactionManager = buildTransactionManager(entityManagerFactory);

        DataEnvironment created = new DataEnvironment(definition, dataSource, entityManager, transactionManager);

        try {
            ctx.addBean(dataEnvName(envId), created, bd -> bd.setDestroyMethodName("close"));
        } catch (BeanDefinitionStoreException e) {
            tryClose(created);
            throw new DataEnvironmentExistsException(envId);
        }

        return created;
    }

    @Override
    @CacheEvict(QUERY_ADAPTERS_CACHE)
    public void delete(String envId) {
        try {
            ctx.removeBean(dataEnvName(envId));
        } catch (NoSuchBeanDefinitionException e) {
            throw new DataEnvironmentNotFoundException(envId);
        }
    }

    private DataSource buildDataSource(DataEnvironmentDefinition dataEnvDefinition) {
        HikariConfig hikariConfig = new HikariConfig(hikariProps);
        hikariConfig.setJdbcUrl(dataEnvDefinition.getUrl());
        hikariConfig.setDriverClassName(dataEnvDefinition.getDriverClassName());
        hikariConfig.setUsername(dataEnvDefinition.getLogin());
        hikariConfig.setPassword(dataEnvDefinition.getPassword());

        return new HikariDataSource(hikariConfig);
    }

    private JpaVendorAdapter buildJpaVendorAdapter(String database) {
        HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        jpaVendorAdapter.setDatabase(Database.valueOf(database));
        return jpaVendorAdapter;
    }

    private EntityManagerFactory buildEntityManagerFactory(String id, DataSource dataSource, JpaVendorAdapter jpaVendorAdapter) {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();

        emf.setDataSource(dataSource);
        emf.setJpaVendorAdapter(jpaVendorAdapter);
        emf.setPersistenceUnitName(id + "PersistenceUnit");
        emf.setPackagesToScan("ru.serge2nd.octopussy.data");

        emf.afterPropertiesSet();
        return emf.getObject();
    }

    private PlatformTransactionManager buildTransactionManager(EntityManagerFactory emf) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);
        return transactionManager;
    }

    private static void tryClose(DataEnvironment dataEnvironment) {
        try {
            dataEnvironment.close();
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    private static String dataEnvName(String envId) {
        return envId + DATA_ENV_SUFFIX;
    }
}
