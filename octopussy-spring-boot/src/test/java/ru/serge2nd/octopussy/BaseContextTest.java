package ru.serge2nd.octopussy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles({"dev","test"})
public interface BaseContextTest {
    ThreadLocal<ApplicationContext> applicationContext = new ThreadLocal<>();

    @Test
    default void contextLoads() { assertNotNull(applicationContext.get(), "app context not found"); }

    @Autowired
    default void setApplicationContext(ApplicationContext ctx) { applicationContext.set(ctx); }
}
