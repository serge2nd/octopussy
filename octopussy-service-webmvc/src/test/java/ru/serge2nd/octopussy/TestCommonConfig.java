package ru.serge2nd.octopussy;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import ru.serge2nd.octopussy.config.JpaConfig;
import ru.serge2nd.octopussy.config.JpaKitConfig;

@Configuration
@EnableConfigurationProperties
@Import({App.class, JpaConfig.class, JpaKitConfig.class})
public class TestCommonConfig {
}
