package ru.serge2nd.octopussy;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("dev,test")
public interface BaseContextTest {

    @Test default void contextLoads() {}
}
