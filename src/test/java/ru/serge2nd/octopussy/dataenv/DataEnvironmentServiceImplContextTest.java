package ru.serge2nd.octopussy.dataenv;

import com.zaxxer.hikari.HikariDataSource;
import org.h2.Driver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.test.context.ActiveProfiles;
import ru.serge2nd.octopussy.config.WebConfig;
import ru.serge2nd.octopussy.data.NativeQueryAdapter;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.lang.System.identityHashCode;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;
import static ru.serge2nd.octopussy.config.CommonConfig.QUERY_ADAPTERS_CACHE;

@SpringBootTest(
        classes = DataEnvironmentServiceImplContextTest.Config.class,
        webEnvironment = NONE)
@ActiveProfiles("test")
class DataEnvironmentServiceImplContextTest {
    private static final String URL_PREFIX = "jdbc:h2:mem:";
    private static final String LOGIN_PREFIX = "L";
    private static final String PASSWORD_PREFIX = "P";
    private static final String ID1 = "empty1";
    private static final String ID2 = "empty2";

    @Autowired
    private DataEnvironmentServiceImpl dataEnvService;
    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        dataEnvService.getAll().forEach(dataEnv ->
                dataEnvService.delete(dataEnv.getDefinition().getEnvId()));
    }

    @Test
    void testCreateOne() throws SQLException {
        // WHEN
        createDataEnvs(ID1);

        // THEN
        Collection<DataEnvironment> all = dataEnvService.getAll();
        checkConsistency(ID1, all);
    }

    @Test
    void testCreateTwo() throws SQLException {
        // WHEN
        createDataEnvs(ID1, ID2);

        // THEN
        Collection<DataEnvironment> all = dataEnvService.getAll();
        checkConsistency(ID1, all);
        checkConsistency(ID2, all);
        // AND
        assertEquals(all.size(), all.stream()
                .mapToInt(de -> identityHashCode(de.getEntityManagerFactory()))
                .distinct().count(), "same EMFs found");
        assertEquals(all.size(), all.stream()
                .mapToInt(de -> identityHashCode(de.getTransactionManager()))
                .distinct().count(), "same TMs found");
    }

    static Stream<Arguments> envsToRemoveProvider() {
        return Stream.of(
                arguments("one", singletonList(ID1), ID1),
                arguments("first", asList(ID1, ID2), ID1),
                arguments("second", asList(ID1, ID2), ID2)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("envsToRemoveProvider")
    void testRemoveCommon(String title, List<String> toCreate, String toDelete) throws SQLException {
        // GIVEN
        createDataEnvs(toCreate.toArray(new String[0]));
        DataEnvironment deleted = dataEnvService.get(toDelete);

        // WHEN
        dataEnvService.delete(toDelete);

        // THEN
        Collection<DataEnvironment> all = dataEnvService.getAll();
        assertEquals(toCreate.size() - 1, all.size(), format("should remain %s", toCreate.size() - 1));
        assertThrows(
                DataEnvironmentNotFoundException.class,
                () -> dataEnvService.get(toDelete),
                "is still presented");
        // AND
        HikariDataSource dataSource = deleted.getDataSource().unwrap(HikariDataSource.class);
        assertTrue(dataSource.isClosed(), "data source is still opened");
        assertFalse(deleted.getEntityManagerFactory().isOpen(), "EMF is still opened");
        // AND
        for (String envId : toCreate) {
            if (!toDelete.equals(envId)) {
                checkConsistency(envId, all);
            }
        }
    }

    @Test
    void testRemoveCacheEvict() {
        // GIVEN
        createDataEnvs(ID1);
        Cache cache = requireNonNull(
                cacheManager.getCache(QUERY_ADAPTERS_CACHE),
                "query adapters cache not found");
        cache.put(ID1, mock(NativeQueryAdapter.class));

        // WHEN
        dataEnvService.delete(ID1);

        // THEN
        assertNull(cache.get(ID1), "cached still exists");
    }

    private void createDataEnvs(String... envIds) {
        Arrays.stream(envIds).forEach(envId ->
            dataEnvService.create(DataEnvironment.builder()
                .definition(DataEnvironmentDefinition.builder()
                        .envId(envId)
                        .database(Database.H2)
                        .driverClass(Driver.class.getName())
                        .url(URL_PREFIX + envId)
                        .login(LOGIN_PREFIX + envId)
                        .password(PASSWORD_PREFIX + envId)
                        .build())
                .build()));
    }

    private void checkConsistency(String envId, Collection<DataEnvironment> all) throws SQLException {
        DataEnvironment dataEnv = dataEnvService.get(envId);
        assertEquals(envId, dataEnv.getDefinition().getEnvId(), format("expected env %s", envId));
        // AND
        HikariDataSource dataSource = dataEnv.getDataSource().unwrap(HikariDataSource.class);
        assertEquals(URL_PREFIX + envId, dataSource.getJdbcUrl(), format("wrong URL of %s", envId));
        assertEquals(LOGIN_PREFIX + envId, dataSource.getUsername(), format("wrong login of %s", envId));
        assertEquals(PASSWORD_PREFIX + envId, dataSource.getPassword(), format("wrong pass of %s", envId));
        assertFalse(dataEnv.getDataSource().getConnection().isClosed(), format("inactive connection of %s", envId));
        // AND
        assertNotNull(dataEnv.getEntityManagerFactory(), format("no EMF for %s", envId));
        assertTrue(dataEnv.getEntityManagerFactory().isOpen(), format("inactive EMF for %s", envId));
        // AND
        assertNotNull(dataEnv.getTransactionManager(), format("no TM for %s", envId));
        // AND
        DataEnvironment[] matched = all.stream()
                .filter(elem -> dataEnv == elem)
                .toArray(DataEnvironment[]::new);
        assertEquals(1, matched.length, format("not exactly one %s", envId));
        assertSame(dataEnv, matched[0], format("not same as returned by id %s", envId));
    }

    @Configuration
    @EnableAutoConfiguration
    @ComponentScan(value = {
            "ru.serge2nd.octopussy.config",
            "ru.serge2nd.octopussy.dataenv"},
            excludeFilters = @Filter(type = ASSIGNABLE_TYPE, value = WebConfig.class))
    static class Config {}
}
