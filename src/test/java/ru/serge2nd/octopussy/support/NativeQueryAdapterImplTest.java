package ru.serge2nd.octopussy.support;

import org.h2.Driver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.test.context.ActiveProfiles;
import ru.serge2nd.octopussy.config.WebConfig;
import ru.serge2nd.octopussy.spi.DataEnvironment;
import ru.serge2nd.octopussy.spi.DataSourceProvider;
import ru.serge2nd.octopussy.spi.NativeQueryAdapter;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;
import static ru.serge2nd.test.CustomAssertions.assertStrictlyEquals;

@SpringBootTest(
        classes = NativeQueryAdapterImplTest.Config.class,
        webEnvironment = NONE)
@ActiveProfiles("test")
class NativeQueryAdapterImplTest {
    static final String URL_PREFIX = "jdbc:h2:mem:";
    static final String ID = "simpledb";

    final NativeQueryAdapterProviderImpl queryAdapterProvider = new NativeQueryAdapterProviderImpl();
    @Autowired DataSourceProvider provider;
    NativeQueryAdapter queryAdapter;
    DataEnvironment dataEnv;

    @BeforeEach void setUp() { createDataEnv(); }
    @AfterEach void tearDown() { if (dataEnv != null) dataEnv.close(); }

    static Stream<Arguments> queriesProvider() {
        return Stream.of(
                arguments("empty", "SELECT * FROM (" + H2Queries.FIRST_ROW + ") WHERE C1", emptyList()),
                arguments("scalar", H2Queries.FIRST_SCALAR, H2Queries.SCALAR_VALUES.subList(0, 1)),
                arguments("column", H2Queries.TWO_SCALARS, H2Queries.SCALAR_VALUES),
                arguments("row", H2Queries.FIRST_ROW, H2Queries.VALUES.subList(0, 1)),
                arguments("table", H2Queries.TWO_ROWS, H2Queries.VALUES)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("queriesProvider")
    void testExecuteQuery(String title, String query, List<?> expected) {
        // WHEN
        List<?> result = queryAdapter.execute(query);

        // THEN
        assertStrictlyEquals(expected, result);
    }

    void createDataEnv() {
        dataEnv = new DataEnvironmentImpl(DataEnvironmentDefinition.builder()
                .envId(ID)
                .database(Database.H2)
                .driverClass(Driver.class.getName())
                .url(URL_PREFIX + ID)
                .login("").password("")
                .build(), provider);
        queryAdapter = queryAdapterProvider.getQueryAdapter(dataEnv);
    }

    @Configuration
    @EnableAutoConfiguration
    @ComponentScan(value = {
            "ru.serge2nd.octopussy.config"},
            excludeFilters = @Filter(type = ASSIGNABLE_TYPE, value = WebConfig.class))
    static class Config {}
}