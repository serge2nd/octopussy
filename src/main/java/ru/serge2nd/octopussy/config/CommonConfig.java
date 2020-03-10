package ru.serge2nd.octopussy.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CommonConfig {
    public static final String QUERY_ADAPTERS_CACHE = "nativeQueryAdapters";
}
