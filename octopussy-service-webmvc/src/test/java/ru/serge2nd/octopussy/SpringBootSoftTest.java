package ru.serge2nd.octopussy;

import org.springframework.boot.test.autoconfigure.SpringBootDependencyInjectionTestExecutionListener;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.TestExecutionListeners;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest
@TestExecutionListeners({
    SpringBootDependencyInjectionTestExecutionListener.class,
    MockitoTestExecutionListener.class})
public @interface SpringBootSoftTest {
    @AliasFor(annotation = SpringBootTest.class, value = "properties")
    String[] value() default {};

    @AliasFor(annotation = SpringBootTest.class, value = "properties")
    String[] properties() default {};
}
