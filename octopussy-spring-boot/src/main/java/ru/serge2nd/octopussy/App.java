package ru.serge2nd.octopussy;

import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;
import ru.serge2nd.octopussy.api.Router;
import ru.serge2nd.octopussy.config.ImmutableWrapperConfig;
import ru.serge2nd.octopussy.config.JpaConfig;
import ru.serge2nd.octopussy.config.JpaKitConfig;
import ru.serge2nd.octopussy.config.WebConfig;

public class App {
    public static final String QUERY_ADAPTERS_CACHE = "nativeQueryAdapters";

    public static final String DATA_KIT_ID = "octps.data.kit.id";
    public static final String DATA_KIT_PKGS = "octps.data.kit.pkgs";

    public static final String DATA_KIT_DB = "database";
    public static final String DATA_KIT_DRIVER_CLASS = "driverClass";
    public static final String DATA_KIT_URL = "url";
    public static final String DATA_KIT_LOGIN = "login";
    public static final String DATA_KIT_PASSWORD = "password";

    public static void main(String[] args) {
        new SpringApplicationBuilder(
                ImmutableWrapperConfig.class,
                JpaConfig.class, JpaKitConfig.class,
                WebConfig.class, Router.class,
                ServletWebServerFactoryAutoConfiguration.class,
                DispatcherServletAutoConfiguration.class)
                .listeners(new ApplicationPidFileWriter("octopussy.pid"))
                .build()
                .run(args);
    }
}
