package ru.serge2nd.octopussy.support;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionProxyFactoryBean;
import ru.serge2nd.octopussy.spi.DataEnvironment;
import ru.serge2nd.octopussy.spi.NativeQueryAdapter;
import ru.serge2nd.octopussy.spi.NativeQueryAdapterProvider;

public class NativeQueryAdapterProviderImpl implements NativeQueryAdapterProvider {

    public NativeQueryAdapter getQueryAdapter(DataEnvironment dataEnv) {
        PlatformTransactionManager transactionManager = dataEnv.getTransactionManager();
        TransactionProxyFactoryBean transactionProxyFactoryBean = new TransactionProxyFactoryBean();

        transactionProxyFactoryBean.setTarget(new NativeQueryAdapterImpl(dataEnv));
        transactionProxyFactoryBean.setTransactionAttributeSource(new AnnotationTransactionAttributeSource());
        transactionProxyFactoryBean.setTransactionManager(transactionManager);

        transactionProxyFactoryBean.setProxyTargetClass(false);
        transactionProxyFactoryBean.setProxyInterfaces(new Class[] {NativeQueryAdapter.class});

        transactionProxyFactoryBean.afterPropertiesSet();
        return (NativeQueryAdapter) transactionProxyFactoryBean.getObject();
    }
}
