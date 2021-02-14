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
import ru.serge2nd.octopussy.service.DataKitProxy;
import ru.serge2nd.octopussy.service.InMemoryDataKits;
import ru.serge2nd.octopussy.spi.*;
import ru.serge2nd.octopussy.spi.DataKit.DataKitBuilder;
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
@SuppressWarnings("ContextJavaBeanUnresolvedMethodsInspection")
public class JpaKitConfig implements DataKitFactory, NativeQueryAdapterProvider {
    static final TransactionAttributeSource TX_ATTR_SRC = new AnnotationTransactionAttributeSource();

    final PersistenceUnitProvider pUnit;

    //region Beans

    @Bean(destroyMethod = "close")
    @SuppressWarnings("unchecked")
    <T extends DataKitService & DataKitExecutor> T dataKitService() {
        return (T)new InMemoryDataKits(new ConcurrentHashMap<>(), new JpaKitProto());
    }

    @Bean Function<EntityManagerFactory, PlatformTransactionManager> txManagerProvider() {
        return emf -> new HibernateTransactionManager(emf.unwrap(SessionFactory.class));
    }

    @Lazy@Bean CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(singleton(new ConcurrentMapCache(QUERY_ADAPTERS_CACHE)));
        return cacheManager;
    }
    @Lazy@Bean Cache queryAdaptersCache() { return cacheManager().getCache(QUERY_ADAPTERS_CACHE); }
    //endregion

    //region Implementations

    @Override
    public NativeQueryAdapter getQueryAdapter(String id) {
        return dataKitService().apply(id, EntityManagerFactory.class, emf -> {
            NativeQueryAdapter cached = queryAdaptersCache().get(id, NativeQueryAdapter.class);
            if (cached != null) return cached;

            PlatformTransactionManager tm = txManagerProvider().apply(emf);
            TransactionProxyFactoryBean txProxy = new TransactionProxyFactoryBean();

            txProxy.setTarget(new NativeQueryAdapterImpl(emf, pUnit.getResultTransformer()));
            txProxy.setTransactionAttributeSource(TX_ATTR_SRC);
            txProxy.setTransactionManager(tm);

            txProxy.setProxyTargetClass(false);
            txProxy.setFrozen(true);

            txProxy.afterPropertiesSet();
            NativeQueryAdapter queryAdapter = (NativeQueryAdapter)txProxy.getObject();

            queryAdaptersCache().putIfAbsent(id, queryAdapter);
            return queryAdapter;
        });
    }

    @Override
    public JpaKit newJpaKit(DataKitDefinition definition) {
        Map<String, String> keys = this.getPropertyMappings();
        Map<String, Object> vals = definition.getProperties();
        return new JpaKitImpl(definition,
            () -> pUnit.getDataSource(properties(
                keys.get(DATA_KIT_DRIVER_CLASS), vals.get(DATA_KIT_DRIVER_CLASS),
                keys.get(DATA_KIT_URL)         , vals.get(DATA_KIT_URL),
                keys.get(DATA_KIT_LOGIN)       , vals.get(DATA_KIT_LOGIN),
                keys.get(DATA_KIT_PASSWORD)    , vals.get(DATA_KIT_PASSWORD)).toMap()),
            ds -> pUnit.getEntityManagerFactory(ds, properties(
                DATA_KIT_DB, vals.get(DATA_KIT_DB),
                DATA_KIT_ID, definition.getKitId()).toMap()));
    }
    @Immutable@Bean @ConfigurationProperties("octps.data.kit.arg.mapping")
    public Map<String, String> getPropertyMappings() { return new HashMap<>(); }
    //endregion

    class JpaKitProto implements DataKit, DataKitBuilder {
        @Getter DataKitDefinition definition;
        public JpaKitProto definition(DataKitDefinition definition) { this.definition = definition; return this; }

        public DataKit     build()     { return jpaKitProxy(definition); }
        public JpaKitProto toBuilder() { return new JpaKitProto().definition(definition); }
        public void        close()     { /* NO-OP */ }
    }

    DataKit jpaKitProxy(DataKitDefinition definition) {
        return new DataKitProxy(definition, dataKitService(), this::newJpaKit) {
            @Override
            public void close() { if (!isClosed()) {
                super.close();
                queryAdaptersCache().evictIfPresent(definition.getKitId());
            }}
        };
    }
}

