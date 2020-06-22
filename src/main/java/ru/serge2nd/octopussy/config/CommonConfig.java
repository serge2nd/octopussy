package ru.serge2nd.octopussy.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.GenericApplicationContext;
import ru.serge2nd.octopussy.config.adapter.ApplicationContextAdapter;
import ru.serge2nd.octopussy.config.adapter.ApplicationContextAdapterFactory;
import ru.serge2nd.octopussy.config.adapter.ApplicationContextAdapterImpl;

@Configuration
@EnableCaching
public class CommonConfig {
    public static final String DATA_ENV_CTX = "dataEnvContext";
    public static final String QUERY_ADAPTERS_CACHE = "nativeQueryAdapters";

    public static final String DATA_ENV_ID = "envId";
    public static final String DATA_ENV_DB = "database";
    public static final String DATA_ENV_DRIVER_CLASS = "driverClass";
    public static final String DATA_ENV_URL = "url";
    public static final String DATA_ENV_LOGIN = "login";
    public static final String DATA_ENV_PASSWORD = "password";

    @Bean(DATA_ENV_CTX)
    public ApplicationContext dataEnvContext(ApplicationContext root) {
        return new GenericApplicationContext(root);
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public ApplicationContextAdapter appContextAdapter(ApplicationContext ctx) {
        return new ApplicationContextAdapterImpl(ctx);
    }

    @Bean
    public ApplicationContextAdapterFactory appContextAdapterFactory() {
        return this::appContextAdapter;
    }
}
