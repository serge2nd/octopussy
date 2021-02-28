package ru.serge2nd.octopussy;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.boot.test.json.JacksonTester.initFields;

@ActiveProfiles({"dev","test"})
public interface BaseContextTest {
    ThreadLocal<ApplicationContext> applicationContext = new ThreadLocal<>();

    @Test
    default void contextLoads() { assertNotNull(applicationContext.get(), "app context not loaded"); }

    @Autowired
    default void setApplicationContext(ApplicationContext ctx) {
        applicationContext.set(ctx);
        ctx.getBeanProvider(ObjectMapper.class).ifAvailable(mapper -> initFields(this, mapper));
    }
}
