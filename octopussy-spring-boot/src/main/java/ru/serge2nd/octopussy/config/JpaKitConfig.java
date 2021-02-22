package ru.serge2nd.octopussy.config;

import org.hibernate.SessionFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.Cache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionProxyFactoryBean;
import ru.serge2nd.bean.processor.Immutable;
import ru.serge2nd.octopussy.App.QueryAdaptersCache;
import ru.serge2nd.octopussy.service.InMemoryDataKits;
import ru.serge2nd.octopussy.spi.DataKitExecutor;
import ru.serge2nd.octopussy.spi.DataKitService;
import ru.serge2nd.octopussy.spi.NativeQueryAdapter;
import ru.serge2nd.octopussy.spi.NativeQueryAdapterProvider;
import ru.serge2nd.octopussy.support.NativeQueryAdapterImpl;

import javax.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Configuration
@SuppressWarnings("ContextJavaBeanUnresolvedMethodsInspection,SpringJavaInjectionPointsAutowiringInspection")
public class JpaKitConfig implements NativeQueryAdapterProvider {
    static final TransactionAttributeSource TX_ATTR_SRC = new AnnotationTransactionAttributeSource();

    final PersistenceUnitProvider pUnits;
    final Cache queryAdaptersCache;

    public JpaKitConfig(PersistenceUnitProvider pUnits, @QueryAdaptersCache Cache queryAdaptersCache) {
        this.pUnits = pUnits;
        this.queryAdaptersCache = queryAdaptersCache;
    }

    //region Beans

    @Immutable@Bean @ConfigurationProperties("octps.data.kit.arg.mapping")
    Map<String, String> propertyMappings() { return new HashMap<>(); }

    @Bean(destroyMethod = "close")
    @SuppressWarnings("unchecked")
    <T extends DataKitService & DataKitExecutor> T dataKitService() {
        return (T)new InMemoryDataKits(new ConcurrentHashMap<>(), new JpaKitProto(this));
    }

    @Bean Function<EntityManagerFactory, PlatformTransactionManager> txManagerProvider() {
        return emf -> new HibernateTransactionManager(emf.unwrap(SessionFactory.class));
    }
    //endregion

    //region Implementations

    @Override
    public NativeQueryAdapter getQueryAdapter(String id) {
        return dataKitService().on(id, EntityManagerFactory.class, emf -> {
            NativeQueryAdapter cached = queryAdaptersCache.get(id, NativeQueryAdapter.class);
            if (cached != null) return cached;

            PlatformTransactionManager tm = txManagerProvider().apply(emf);
            TransactionProxyFactoryBean txProxy = new TransactionProxyFactoryBean();

            txProxy.setTarget(new NativeQueryAdapterImpl(emf, pUnits.getResultTransformer()));
            txProxy.setTransactionAttributeSource(TX_ATTR_SRC);
            txProxy.setTransactionManager(tm);

            txProxy.setProxyTargetClass(false);
            txProxy.setFrozen(true);

            txProxy.afterPropertiesSet();
            NativeQueryAdapter queryAdapter = (NativeQueryAdapter)txProxy.getObject();

            queryAdaptersCache.putIfAbsent(id, queryAdapter);
            return queryAdapter;
        });
    }
    //endregion
}

