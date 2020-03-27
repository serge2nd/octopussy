package ru.serge2nd.octopussy.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
@ConfigurationProperties("spring.datasource.hikari")
public class DataSourceProperties extends Properties {
}
