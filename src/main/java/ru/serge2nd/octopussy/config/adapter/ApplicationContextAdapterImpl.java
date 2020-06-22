package ru.serge2nd.octopussy.config.adapter;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import java.util.Collection;

public class ApplicationContextAdapterImpl implements ApplicationContextAdapter {
    private final GenericApplicationContext ctx;

    public ApplicationContextAdapterImpl(ApplicationContext ctx) {
        this.ctx = (GenericApplicationContext) ctx;
    }

    public <T> void addBean(BeanCfg beanCfg) throws BeanDefinitionStoreException {
        AbstractBeanDefinition bd = BeanDefinitionBuilder
                .genericBeanDefinition(beanCfg.getBeanClass())
                .setInitMethodName(beanCfg.getInitMethod())
                .setDestroyMethodName(beanCfg.getDestroyMethod())
                .setLazyInit(beanCfg.isLazyInit())
                .setScope(beanCfg.getScope())
                .getRawBeanDefinition();
        bd.setInstanceSupplier(beanCfg.getSupplier());

        ctx.registerBeanDefinition(beanCfg.getName(), bd);
    }

    public void removeBean(String beanName) throws NoSuchBeanDefinitionException {
        ctx.removeBeanDefinition(beanName);
    }

    public <T> T getBean(String beanName, Class<T> beanClass) throws BeansException {
        return ctx.getBean(beanName, beanClass);
    }

    public <T> Collection<T> getBeans(Class<T> clazz) throws BeansException {
        return ctx.getBeansOfType(clazz, false, false).values();
    }
}
