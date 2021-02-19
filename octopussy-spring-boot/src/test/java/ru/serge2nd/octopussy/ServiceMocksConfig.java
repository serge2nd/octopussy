package ru.serge2nd.octopussy;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import ru.serge2nd.octopussy.spi.DataKitService;
import ru.serge2nd.octopussy.spi.NativeQueryAdapterProvider;

import static org.mockito.Answers.RETURNS_DEEP_STUBS;

@Configuration
@MockBean(classes = {
    DataKitService.class,
    NativeQueryAdapterProvider.class},
    answer = RETURNS_DEEP_STUBS)
public class ServiceMocksConfig {
}
