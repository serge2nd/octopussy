package ru.serge2nd.octopussy;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import ru.serge2nd.bean.processor.ImmutableFilter;
import ru.serge2nd.bean.processor.WrapBeanPostProcessor;
import ru.serge2nd.bean.processor.WrapBeanPostProcessor.BeanFilter;
import ru.serge2nd.collection.HardProperties;
import ru.serge2nd.function.DelegatingOperatorProvider;
import ru.serge2nd.function.OperatorProvider;
import ru.serge2nd.octopussy.api.Router;
import ru.serge2nd.octopussy.config.JpaConfig;
import ru.serge2nd.octopussy.config.JpaKitConfig;
import ru.serge2nd.octopussy.config.WebConfig;

import java.lang.annotation.Retention;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.BiFunction;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.Optional.of;

@Configuration
@Import(WrapBeanPostProcessor.class)
public class App {
    public static final String QUERY_ADAPTERS_CACHE = "nativeQueryAdapters";
    public @Retention(RUNTIME) @Qualifier(QUERY_ADAPTERS_CACHE) @interface QueryAdaptersCache {}

    public static final String DATA_KIT_ID = "octps.data.kit.id";
    public static final String DATA_KIT_PKGS = "octps.data.kit.pkgs";

    public static final String DATA_KIT_DB = "database";
    public static final String DATA_KIT_DRIVER_CLASS = "driverClass";
    public static final String DATA_KIT_URL = "url";
    public static final String DATA_KIT_LOGIN = "login";
    public static final String DATA_KIT_PASSWORD = "password";

    public static void main(String[] args) {
        new SpringApplicationBuilder(App.class,
                JpaConfig.class, JpaKitConfig.class,
                WebConfig.class, Router.class,
                ServletWebServerFactoryAutoConfiguration.class,
                DispatcherServletAutoConfiguration.class)
                .listeners(new ApplicationPidFileWriter("octopussy.pid"))
                .build()
                .run(args);
    }

    @Bean(QUERY_ADAPTERS_CACHE) Cache queryAdaptersCache() {
        return new ConcurrentMapCache(QUERY_ADAPTERS_CACHE, false);
    }

    @Bean BiFunction<Type, Object, ?> wrapper() {
        return (type, bean) -> type != null
                ? wrapperProvider().forType(type)
                    .map(w -> w.apply(bean))
                    .orElse(bean)
                : bean;
    }

    @Bean OperatorProvider<Object> wrapperProvider() { return new DelegatingOperatorProvider<Object>() {{
        addDelegate(Properties.class  , $ -> of(HardProperties::of));
        addDelegate(NavigableMap.class, $ -> of(Collections::<Object, Object>unmodifiableNavigableMap));
        addDelegate(SortedMap.class   , $ -> of(Collections::<Object, Object>unmodifiableSortedMap));
        addDelegate(Map.class         , $ -> of(Collections::<Object, Object>unmodifiableMap));
    }}; }

    @Bean BeanFilter beanFilter() { return ImmutableFilter.INSTANCE; }
}
