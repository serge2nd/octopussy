package ru.serge2nd.octopussy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import ru.serge2nd.bean.processor.ImmutableFilter;
import ru.serge2nd.bean.processor.WrapBeanPostProcessor;
import ru.serge2nd.bean.processor.WrapBeanPostProcessor.BeanFilter;
import ru.serge2nd.bean.processor.WrapBeanPostProcessor.Wrapper;
import ru.serge2nd.collection.HardProperties;
import ru.serge2nd.function.DelegatingOperatorProvider;
import ru.serge2nd.function.OperatorProvider;

import java.util.*;

import static java.util.Optional.of;

@Configuration
@Import(WrapBeanPostProcessor.class)
public class ImmutableWrapperConfig {

    @Bean BeanFilter beanFilter() {
        return ImmutableFilter.INSTANCE;
    }

    @Bean Wrapper wrapper(OperatorProvider<Object> wrapperProvider) {
        return (type, bean) -> type != null
            ? wrapperProvider.forType(type)
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
}
