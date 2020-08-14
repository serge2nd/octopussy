package ru.serge2nd.octopussy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App {
    public static final String QUERY_ADAPTERS_CACHE = "nativeQueryAdapters";

    public static final String DATA_ENV_ID = "envId";
    public static final String DATA_ENV_PROPS = "properties";
    
    public static final String DATA_ENV_DB = "database";
    public static final String DATA_ENV_DRIVER_CLASS = "driverClass";
    public static final String DATA_ENV_URL = "url";
    public static final String DATA_ENV_LOGIN = "login";
    public static final String DATA_ENV_PASSWORD = "password";
    

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
