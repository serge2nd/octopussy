package ru.serge2nd.octopussy;

import org.springframework.boot.test.mock.mockito.MockBean;
import ru.serge2nd.octopussy.spi.DataKitService;
import ru.serge2nd.octopussy.spi.NativeQueryAdapterProvider;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.mockito.Answers.RETURNS_DEEP_STUBS;

@Retention(RetentionPolicy.RUNTIME)
@MockBean(classes = {
    DataKitService.class,
    NativeQueryAdapterProvider.class},
    answer = RETURNS_DEEP_STUBS)
public @interface MockServiceLayer {
}
