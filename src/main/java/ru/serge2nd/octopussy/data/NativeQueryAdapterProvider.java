package ru.serge2nd.octopussy.data;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionProxyFactoryBean;
import ru.serge2nd.octopussy.dataenv.DataEnvironment;
import ru.serge2nd.octopussy.dataenv.DataEnvironmentService;

import javax.persistence.EntityManager;
import static ru.serge2nd.octopussy.config.CommonConfig.QUERY_ADAPTERS_CACHE;

@Service
@RequiredArgsConstructor
public class NativeQueryAdapterProvider {
    private final DataEnvironmentService dataEnvironmentService;

    @Cacheable(QUERY_ADAPTERS_CACHE)
    public NativeQueryAdapter getQueryAdapter(String envId) {
        DataEnvironment dataEnvironment = dataEnvironmentService.get(envId);
        return buildQueryAdapter(dataEnvironment);
    }

    private NativeQueryAdapter buildQueryAdapter(DataEnvironment dataEnvironment) {
        EntityManager entityManager = dataEnvironment.getEntityManager();
        PlatformTransactionManager transactionManager = dataEnvironment.getTransactionManager();
        TransactionProxyFactoryBean transactionProxyFactoryBean = new TransactionProxyFactoryBean();

        transactionProxyFactoryBean.setTarget(new NativeQueryAdapterImpl(entityManager));
        transactionProxyFactoryBean.setTransactionAttributeSource(new AnnotationTransactionAttributeSource());
        transactionProxyFactoryBean.setTransactionManager(transactionManager);
        transactionProxyFactoryBean.setProxyTargetClass(true);

        transactionProxyFactoryBean.afterPropertiesSet();
        return (NativeQueryAdapter) transactionProxyFactoryBean.getObject();
    }
}
