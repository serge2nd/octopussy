package ru.serge2nd.octopussy;

import org.h2.Driver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.test.context.ActiveProfiles;
import ru.serge2nd.octopussy.config.WebConfig;
import ru.serge2nd.octopussy.dataenv.DataEnvironment;
import ru.serge2nd.octopussy.dataenv.DataEnvironmentDefinition;
import ru.serge2nd.octopussy.dataenv.DataEnvironmentServiceImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;

@SpringBootTest(
        classes = DataEnvironmentServiceImplTest.Config.class,
        webEnvironment = NONE)
@ActiveProfiles("test")
class DataEnvironmentServiceImplTest {
    private static final String ID = "h2";

    @Autowired
    private DataEnvironmentServiceImpl dataEnvService;
    private DataEnvironment dataEnv;

    @BeforeEach
    void setUp() {
        if (dataEnv == null) {
            dataEnv = dataEnvService.create(DataEnvironment.builder()
                    .definition(DataEnvironmentDefinition.builder()
                            .envId(ID)
                            .database(Database.H2)
                            .driverClass(Driver.class.getName())
                            .url("jdbc:h2:mem:SAMPLE")
                            .build())
                    .build());
        }
    }

    @Test
    void test0001() {
        assertEquals(1, dataEnvService.getAll().size());
    }

    @Configuration
    @EnableAutoConfiguration
    @ComponentScan(value = {
            "ru.serge2nd.octopussy.config",
            "ru.serge2nd.octopussy.dataenv"},
            excludeFilters = @Filter(type = ASSIGNABLE_TYPE, value = WebConfig.class))
    static class Config {}
}
