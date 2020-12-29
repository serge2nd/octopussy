package ru.serge2nd.octopussy;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import ru.serge2nd.octopussy.config.ImmutableWrapperConfig;
import ru.serge2nd.octopussy.config.JpaConfig;
import ru.serge2nd.octopussy.config.JpaKitConfig;

@Configuration
@EnableConfigurationProperties
@Import({JpaConfig.class, JpaKitConfig.class, ImmutableWrapperConfig.class})
public class TestCommonConfig {
}
