package ru.serge2nd.octopussy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;

@SpringBootApplication
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
        SpringApplication app = new SpringApplication(App.class);
        app.addListeners(new ApplicationPidFileWriter("octopussy.pid"));
        app.run(args);
    }
}
