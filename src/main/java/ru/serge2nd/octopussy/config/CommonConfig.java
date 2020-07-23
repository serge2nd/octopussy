package ru.serge2nd.octopussy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.serge2nd.octopussy.spi.DataEnvironment;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Configuration
public class CommonConfig {
    public static final String QUERY_ADAPTERS_CACHE = "nativeQueryAdapters";

    public static final String DATA_ENV_ID = "envId";
    public static final String DATA_ENV_DB = "database";
    public static final String DATA_ENV_DRIVER_CLASS = "driverClass";
    public static final String DATA_ENV_URL = "url";
    public static final String DATA_ENV_LOGIN = "login";
    public static final String DATA_ENV_PASSWORD = "password";

    @Bean
    Supplier<Map<String, DataEnvironment>> dataEnvRepositoryProvider() {
        return ConcurrentHashMap::new;
    }
}
