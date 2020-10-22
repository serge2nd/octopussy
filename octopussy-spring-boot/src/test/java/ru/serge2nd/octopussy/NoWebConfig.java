package ru.serge2nd.octopussy;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import ru.serge2nd.octopussy.config.WebConfig;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;

@Configuration
@EnableAutoConfiguration
@ComponentScan(value = {
        "ru.serge2nd.octopussy.config"},
        excludeFilters = @ComponentScan.Filter(type = ASSIGNABLE_TYPE, value = WebConfig.class))
public class NoWebConfig {

    @Retention(RetentionPolicy.RUNTIME)
    @SpringBootTest(classes = NoWebConfig.class, webEnvironment = NONE)
    public @interface NoWebSpringBootTest {}
}
