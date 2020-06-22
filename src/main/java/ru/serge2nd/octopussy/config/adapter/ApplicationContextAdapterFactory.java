package ru.serge2nd.octopussy.config.adapter;

import org.springframework.context.ApplicationContext;

import java.util.function.Function;

public interface ApplicationContextAdapterFactory extends Function<ApplicationContext, ApplicationContextAdapter> {
}
