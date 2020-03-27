package ru.serge2nd.octopussy.dataenv;

import org.h2.Driver;
import org.junit.jupiter.api.AfterEach;
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

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;

@SpringBootTest(
        classes = DataEnvironmentServiceImplContextTest.Config.class,
        webEnvironment = NONE)
@ActiveProfiles("test")
class DataEnvironmentServiceImplContextTest {
    private static final String URL_PREFIX = "jdbc:h2:mem:";
    private static final String ID1 = "simpledb1";
    private static final String ID2 = "simpledb2";

    @Autowired
    private DataEnvironmentServiceImpl dataEnvService;

    @BeforeEach
    void setUp() {
        createDataEnv(ID1, URL_PREFIX + ID1);
    }

    @AfterEach
    void tearDown() {
        dataEnvService.getAll().forEach(dataEnv ->
                dataEnvService.delete(dataEnv.getDefinition().getEnvId()));
    }

    @Test
    void testCreateOne() throws SQLException {
        // THEN
        DataEnvironment dataEnv = dataEnvService.get(ID1);
        assertEquals(ID1, dataEnv.getDefinition().getEnvId());
        // AND
        assertNotNull(dataEnv.getDataSource());
        assertFalse(dataEnv.getDataSource().getConnection().isClosed());
        // AND
        assertNotNull(dataEnv.getEntityManagerFactory());
        assertTrue(dataEnv.getEntityManagerFactory().isOpen());
        // AND
        assertNotNull(dataEnv.getTransactionManager());
    }

    private void createDataEnv(String envId, String url) {
        dataEnvService.create(DataEnvironment.builder()
                .definition(DataEnvironmentDefinition.builder()
                        .envId(envId)
                        .database(Database.H2)
                        .driverClass(Driver.class.getName())
                        .url(url).login("").password("")
                        .build())
                .build());
    }

    @Configuration
    @EnableAutoConfiguration
    @ComponentScan(value = {
            "ru.serge2nd.octopussy.config",
            "ru.serge2nd.octopussy.dataenv"},
            excludeFilters = @Filter(type = ASSIGNABLE_TYPE, value = WebConfig.class))
    static class Config {}
}
