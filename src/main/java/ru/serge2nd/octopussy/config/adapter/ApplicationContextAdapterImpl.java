package ru.serge2nd.octopussy.config.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinitionCustomizer;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class ApplicationContextAdapterImpl implements ApplicationContextAdapter {
    private final GenericApplicationContext ctx;

    public void addBean(String beanName, Object bean, BeanDefinitionCustomizer... customizers) throws BeanDefinitionStoreException {
        AbstractBeanDefinition bd = BeanDefinitionBuilder
                .genericBeanDefinition(bean.getClass())
                .applyCustomizers(customizers)
                .getRawBeanDefinition();
        bd.setInstanceSupplier(() -> ctx.getBeanFactory().initializeBean(bean, beanName));

        ctx.registerBeanDefinition(beanName, bd);
    }

    public void removeBean(String beanName) throws NoSuchBeanDefinitionException {
        ctx.removeBeanDefinition(beanName);
    }

    public <T> T getBean(String beanName, Class<T> beanClass) throws BeansException {
        return ctx.getBean(beanName, beanClass);
    }

    public <T> Collection<T> getBeans(Class<T> clazz) {
        return ctx.getBeansOfType(clazz).values();
    }

    public boolean containsBean(String beanName) {
        return ctx.containsBean(beanName);
    }
}
