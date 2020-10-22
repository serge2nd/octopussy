package ru.serge2nd.octopussy.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionProxyFactoryBean;
import ru.serge2nd.bean.processor.Immutable;
import ru.serge2nd.octopussy.service.InMemoryDataEnvironmentService;
import ru.serge2nd.octopussy.service.DataEnvironmentProxy;
import ru.serge2nd.octopussy.spi.*;
import ru.serge2nd.octopussy.support.DataEnvironmentDefinition;
import ru.serge2nd.octopussy.support.JpaEnvironmentImpl;
import ru.serge2nd.octopussy.support.NativeQueryAdapterImpl;
import ru.serge2nd.octopussy.support.ToListResultTransformer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;

import static ru.serge2nd.collection.HardProperties.properties;
import static ru.serge2nd.octopussy.App.*;

@Configuration @EnableCaching
@RequiredArgsConstructor
public class DataEnvConfig implements DataEnvironmentFactory, NativeQueryAdapterProvider {
    final PersistenceUnitProvider puProvider;

    @Bean(destroyMethod = "close")
    @SuppressWarnings("ContextJavaBeanUnresolvedMethodsInspection")
    DataEnvironmentService dataEnvironmentService() { return new InMemoryDataEnvironmentService(
        new ConcurrentHashMap<>(),
        DataEnvironmentProxy.builder()
            .service(this::dataEnvironmentService)
            .dataEnvFactory(this::newJpaEnvironment)
            .build(),
        dataEnvPreClose()
    );}
    @Bean UnaryOperator<DataEnvironment> dataEnvPreClose() { return new UnaryOperator<DataEnvironment>() {
        @CacheEvict(
                cacheNames = QUERY_ADAPTERS_CACHE,
                key = "#p0.definition.envId")
        public DataEnvironment apply(DataEnvironment dataEnv) { return dataEnv; }
    };}

    @Override
    @Cacheable(QUERY_ADAPTERS_CACHE)
    public NativeQueryAdapter getQueryAdapter(String envId) { return dataEnvironmentService().doWith(envId, JpaEnvironment.class, jpaEnv -> {
        PlatformTransactionManager tm = jpaEnv.getTransactionManager();
        TransactionProxyFactoryBean txProxy = new TransactionProxyFactoryBean();

        txProxy.setTarget(new NativeQueryAdapterImpl(jpaEnv, ToListResultTransformer.INSTANCE));
        txProxy.setTransactionAttributeSource(TX_ATTR_SRC);
        txProxy.setTransactionManager(tm);

        txProxy.setProxyTargetClass(false);
        txProxy.setFrozen(true);
        txProxy.setOptimize(true);

        txProxy.afterPropertiesSet();
        return (NativeQueryAdapter)txProxy.getObject();
    });}

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
                DATA_ENV_ID, definition.getEnvId()).toMap()),
            puProvider::getTransactionManager);
    }
    @Override @Immutable@Bean @ConfigurationProperties("octps.data.env.arg.mapping")
    public Map<String, String> getPropertyMappings() { return new HashMap<>(); }

    static final TransactionAttributeSource TX_ATTR_SRC = new AnnotationTransactionAttributeSource();
}
