package ru.serge2nd.octopussy.config.adapter;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinitionCustomizer;

import java.util.Collection;
import java.util.List;

public interface ApplicationContextAdapter {

    void addBean(String beanName, Object bean, BeanDefinitionCustomizer... customizers) throws BeanDefinitionStoreException;

    void removeBean(String beanName) throws NoSuchBeanDefinitionException;

    <T> T getBean(String beanName, Class<T> beanClass) throws BeansException;

    <T> Collection<T> getBeans(Class<T> clazz);

    boolean containsBean(String beanName);
}
