package ru.serge2nd.octopussy.support;

import org.h2.Driver;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import ru.serge2nd.octopussy.App;
import ru.serge2nd.octopussy.BaseContextTest;
import ru.serge2nd.octopussy.SpringBootSoftTest;
import ru.serge2nd.octopussy.TestCommonConfig;
import ru.serge2nd.octopussy.spi.DataKit;
import ru.serge2nd.octopussy.spi.DataKitService;
import ru.serge2nd.octopussy.spi.NativeQueryAdapter;
import ru.serge2nd.octopussy.spi.NativeQueryAdapterProvider;
import ru.serge2nd.test.util.Resources;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static ru.serge2nd.octopussy.support.DataKitDefinitionTest.ID;
import static ru.serge2nd.test.util.CustomAssertions.assertStrictlyEquals;

@SpringBootSoftTest
@ContextHierarchy({
    @ContextConfiguration(classes = TestCommonConfig.class)
})
@TestInstance(Lifecycle.PER_CLASS)
class NativeQueryAdapterImplTest implements BaseContextTest {
    static final String URL_PREFIX = "jdbc:h2:mem:";

    NativeQueryAdapter queryAdapter;

    @Autowired void init(DataKitService service, NativeQueryAdapterProvider provider) {
        queryAdapter = provider.getQueryAdapter(createDataKit(service));
    }

    static Stream<Arguments> queriesProvider() { return Stream.of(
        arguments("empty" , str("tuples.sql") + "\nLIMIT 0" , emptyList()),
        arguments("scalar", str("scalars.sql") + "\nLIMIT 1", SCALARS.subList(0, 1)),
        arguments("column", str("scalars.sql")              , SCALARS),
        arguments("row"   , str("tuples.sql") + "\nLIMIT 1" , TUPLES.subList(0, 1)),
        arguments("table" , str("tuples.sql")               , TUPLES)); }
    @ParameterizedTest(name = "{0}")
    @MethodSource("queriesProvider")
    void testExecuteQuery(String title, String query, List<?> expected) {
        // WHEN
        List<?> result = queryAdapter.execute(query, emptyMap());

        // THEN
        assertStrictlyEquals(expected, result);
    }

    DataKit createDataKit(DataKitService service) {
        return service.create(DataKitDefinition.builder()
            .kitId(ID)
            .property(App.DATA_KIT_DB, "H2")
            .property(App.DATA_KIT_DRIVER_CLASS, Driver.class.getName())
            .property(App.DATA_KIT_URL, URL_PREFIX + ID)
            .property(App.DATA_KIT_LOGIN, "")
            .property(App.DATA_KIT_PASSWORD, "")
            .build());
    }

    static String str(String name, Object... args) { return Resources.asString(name, lookup(), args); }

    static final List<Byte> SCALARS = asList((byte)5, (byte)7);

    static final List<List<?>> TUPLES = asList(
            asList(Boolean.FALSE, (byte)-128, (short)-32768, -2147483648, BigInteger.valueOf(-9223372036854775808L), new BigDecimal("-9223372036854775807.65"),
                    -999.9999f, -99999999.99999999, LocalTime.parse("21:30:16"), LocalDate.parse("2020-03-22"), LocalDateTime.parse("2019-01-09T15:08:00"),
                    new byte[] {-2, -36, -70}, "fedcba", new byte[] {-2, -36, -70}, "fedcba"),
            asList(Boolean.TRUE, (byte)127, (short)32767, 2147483647, BigInteger.valueOf(9223372036854775807L), new BigDecimal("9223372036854775807.65"),
                    999.9999f, 99999999.99999999, LocalTime.parse("15:30:16"), LocalDate.parse("2018-09-24"), LocalDateTime.parse("2019-01-01T01:45:38"),
                    new byte[] {-85, -51, -17}, "abcdef", new byte[] {-85, -51, -17}, "abcdef"));
}