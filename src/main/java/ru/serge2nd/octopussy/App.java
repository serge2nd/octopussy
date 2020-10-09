package ru.serge2nd.octopussy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;

@SpringBootApplication
public class App {
    public static final String QUERY_ADAPTERS_CACHE = "nativeQueryAdapters";

    public static final String DATA_ENV_ID = "octps.data.env.id";
    public static final String DATA_ENV_PU = "octps.data.env.pu";
    public static final String DATA_ENV_PKGS = "octps.data.env.pkgs";

    public static final String DATA_ENV_DB = "database";
    public static final String DATA_ENV_DRIVER_CLASS = "driverClass";
    public static final String DATA_ENV_URL = "url";
    public static final String DATA_ENV_LOGIN = "login";
    public static final String DATA_ENV_PASSWORD = "password";

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(App.class);
        app.addListeners(new ApplicationPidFileWriter("octopussy.pid"));
        app.run(args);
    }
}
