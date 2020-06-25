package ru.serge2nd.util.bean;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.core.type.MethodMetadata;
import org.springframework.stereotype.Component;
import ru.serge2nd.util.immutable.ImmutableProvider;

import java.lang.reflect.Type;

@Component
@RequiredArgsConstructor
public class ImmutableBeanPostProcessor implements BeanPostProcessor, Ordered {
    private final ImmutableProvider<Object> immutableProvider;
    private final ConfigurableListableBeanFactory beanFactory;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (!beanFactory.containsBeanDefinition(beanName)) {
            return bean;
        }

        BeanDefinition bd = beanFactory.getBeanDefinition(beanName);
        if (!(bd instanceof AnnotatedBeanDefinition)) {
            return bean;
        }

        AnnotatedBeanDefinition abd = (AnnotatedBeanDefinition) bd;
        MethodMetadata metadata = abd.getFactoryMethodMetadata();
        if ((metadata == null || !metadata.isAnnotated(Immutable.class.getName())) &&
            !abd.getMetadata().hasAnnotation(Immutable.class.getName())) {
            return bean;
        }

        Type beanType = bd.getResolvableType().getType();

        return immutableProvider.wrap(beanType)
                .map(w -> w.apply(bean))
                .orElse(bean);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
