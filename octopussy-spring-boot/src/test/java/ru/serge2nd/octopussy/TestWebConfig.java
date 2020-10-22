package ru.serge2nd.octopussy;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.serge2nd.octopussy.config.WebConfig;
import ru.serge2nd.octopussy.spi.DataEnvironmentService;
import ru.serge2nd.octopussy.spi.NativeQueryAdapterProvider;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;

@Configuration
@Import(WebConfig.class)
@ComponentScan(basePackages = "ru.serge2nd.octopussy.api")
@MockBean(classes = {
        DataEnvironmentService.class,
        NativeQueryAdapterProvider.class},
        answer = RETURNS_DEEP_STUBS)
public class TestWebConfig {
    @Bean
    MockMvc mockMvc(WebApplicationContext ctx) {
        return MockMvcBuilders.webAppContextSetup(ctx)
                .addFilter((request, response, chain) -> {
                    MockHttpServletRequest r = (MockHttpServletRequest)request;
                    r.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    r.setCharacterEncoding(UTF_8.name());
                    chain.doFilter(request, response); })
                .alwaysDo(MockMvcResultHandlers.print())
                .build();
    }
}
