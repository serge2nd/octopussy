package ru.serge2nd.octopussy.config;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.serge2nd.bean.processor.ImmutableFilter;
import ru.serge2nd.bean.processor.WrapBeanPostProcessor;
import ru.serge2nd.collection.HardProperties;
import ru.serge2nd.function.DelegatingOperatorProvider;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.BiFunction;

import static java.util.Optional.of;

@Configuration
public class ImmutableWrapperConfig {

    @Bean BeanPostProcessor beanWrapperPostProcessor(ConfigurableListableBeanFactory beanFactory) {
        return new WrapBeanPostProcessor(ImmutableFilter.INSTANCE, WRAPPER, beanFactory);
    }

    static final DelegatingOperatorProvider<Object> PROVIDER = new DelegatingOperatorProvider<Object>() {{
        addDelegate(Properties.class  , $ -> of(HardProperties::of));
        addDelegate(NavigableMap.class, $ -> of(Collections::<Object, Object>unmodifiableNavigableMap));
        addDelegate(SortedMap.class   , $ -> of(Collections::<Object, Object>unmodifiableSortedMap));
        addDelegate(Map.class         , $ -> of(Collections::<Object, Object>unmodifiableMap));
    }};

    static final BiFunction<Type, Object, Object> WRAPPER = (type, bean) ->
            type != null ? PROVIDER.forType(type)
            .map(wrapper -> wrapper.apply(bean))
            .orElse(bean) : bean;
}
