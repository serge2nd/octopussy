package ru.serge2nd.octopussy.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
@ConfigurationProperties("spring.jpa.properties")
public class JpaProperties extends Properties {
}
