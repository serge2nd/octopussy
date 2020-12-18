package ru.serge2nd.octopussy.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hibernate.SessionFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionProxyFactoryBean;
import ru.serge2nd.bean.processor.Immutable;
import ru.serge2nd.octopussy.service.DataEnvironmentProxy;
import ru.serge2nd.octopussy.service.InMemoryDataEnvironmentService;
import ru.serge2nd.octopussy.spi.*;
import ru.serge2nd.octopussy.spi.DataEnvironment.DataEnvironmentBuilder;
import ru.serge2nd.octopussy.support.*;

import javax.persistence.EntityManagerFactory;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static java.util.Collections.singleton;
import static ru.serge2nd.collection.HardProperties.properties;
import static ru.serge2nd.octopussy.App.*;

@Configuration
@RequiredArgsConstructor
public class JpaEnvConfig implements DataEnvironmentFactory, NativeQueryAdapterProvider {
    final PersistenceUnitProvider puProvider;

    //region Beans

    @Bean(destroyMethod = "close")
    DataEnvironmentService dataEnvironmentService() {
        return new InMemoryDataEnvironmentService(new ConcurrentHashMap<>(), new JpaEnvProto());
    }

    @Bean Function<EntityManagerFactory, PlatformTransactionManager> transactionManagerProvider() {
        return emf -> new HibernateTransactionManager(emf.unwrap(SessionFactory.class));
    }

    @Bean ResultTransformer resultTransformer() { return new ToListResultTransformer(); }

    @Bean CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(singleton(new ConcurrentMapCache(QUERY_ADAPTERS_CACHE)));
        return cacheManager;
    }
    @Lazy@Bean Cache queryAdaptersCache() { return cacheManager().getCache(QUERY_ADAPTERS_CACHE); }
    //endregion

    //region Implementations

    @Override
    public NativeQueryAdapter getQueryAdapter(String envId) {
        return dataEnvironmentService().doWith(envId, JpaEnvironment.class, jpaEnv -> {
            NativeQueryAdapter cached = queryAdaptersCache().get(envId, NativeQueryAdapter.class);
            if (cached != null) return cached;

            PlatformTransactionManager tm = transactionManagerProvider().apply(jpaEnv.getEntityManagerFactory());
            TransactionProxyFactoryBean txProxy = new TransactionProxyFactoryBean();

            txProxy.setTarget(new NativeQueryAdapterImpl(jpaEnv, new org.hibernate.transform.ResultTransformer() {
                final ResultTransformer delegate = resultTransformer();
                public Object transformTuple(Object[] tuple, String[] aliases) { return delegate.transform(tuple, aliases); }
                public List<?> transformList(List list)                        { return delegate.transform(list); }
            }));
            txProxy.setTransactionAttributeSource(TX_ATTR_SRC);
            txProxy.setTransactionManager(tm);

            txProxy.setProxyTargetClass(false);
            txProxy.setFrozen(true);

            txProxy.afterPropertiesSet();
            NativeQueryAdapter queryAdapter = (NativeQueryAdapter)txProxy.getObject();

            queryAdaptersCache().putIfAbsent(envId, queryAdapter);
            return queryAdapter;
        });
    }

    @Override
    public JpaEnvironment newJpaEnvironment(DataEnvironmentDefinition definition) {
        Map<String, String> keys = this.getPropertyMappings();
        Map<String, Object> vals = definition.getProperties();
        return new JpaEnvironmentImpl(definition,
            () -> puProvider.getDataSource(properties(
                keys.get(DATA_ENV_DRIVER_CLASS), vals.get(DATA_ENV_DRIVER_CLASS),
                keys.get(DATA_ENV_URL)         , vals.get(DATA_ENV_URL),
                keys.get(DATA_ENV_LOGIN)       , vals.get(DATA_ENV_LOGIN),
                keys.get(DATA_ENV_PASSWORD)    , vals.get(DATA_ENV_PASSWORD)).toMap()),
            ds -> puProvider.getEntityManagerFactory(ds, properties(
                DATA_ENV_DB, vals.get(DATA_ENV_DB),
                DATA_ENV_ID, definition.getEnvId()).toMap()));
    }
    @Immutable@Bean @ConfigurationProperties("octps.data.env.arg.mapping")
    public Map<String, String> getPropertyMappings() { return new HashMap<>(); }
    //endregion

    static final TransactionAttributeSource TX_ATTR_SRC = new AnnotationTransactionAttributeSource();

    class JpaEnvProto implements DataEnvironment, DataEnvironmentBuilder {
        @Getter DataEnvironmentDefinition definition;
        public JpaEnvProto definition(DataEnvironmentDefinition definition) { this.definition = definition; return this; }

        public DataEnvironment build()   { return jpaEnvProxy(definition); }
        public JpaEnvProto   toBuilder() { return new JpaEnvProto().definition(definition); }
        public void            close()   { /* NO-OP */ }
    }

    DataEnvironment jpaEnvProxy(DataEnvironmentDefinition definition) {
        return new DataEnvironmentProxy(definition, dataEnvironmentService(), this::newJpaEnvironment) {
            @Override
            public void close() { if (!isClosed()) {
                super.close();
                queryAdaptersCache().evictIfPresent(definition.getEnvId());
            }}
        };
    }
}

