package ru.serge2nd.octopussy.support;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import ru.serge2nd.octopussy.BaseContextTest;
import ru.serge2nd.octopussy.ServiceMocksConfig;
import ru.serge2nd.octopussy.SpringBootSoftTest;
import ru.serge2nd.octopussy.TestWebConfig;
import ru.serge2nd.test.util.Resources;

import javax.validation.Validator;
import java.io.IOException;
import java.util.stream.Stream;

import static java.lang.invoke.MethodHandles.lookup;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static ru.serge2nd.octopussy.support.DataKitDefinition.builder;
import static ru.serge2nd.octopussy.support.DataKitDefinitionTest.DEF;
import static ru.serge2nd.octopussy.support.DataKitDefinitionTest.ID;
import static ru.serge2nd.test.match.AssertThat.assertThat;
import static ru.serge2nd.test.util.CustomMatchers.equalToJson;

@SpringBootSoftTest
@ContextHierarchy({
    @ContextConfiguration(classes = {TestWebConfig.class, ServiceMocksConfig.class})
})
@TestInstance(Lifecycle.PER_CLASS)
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class DataKitDefinitionValidationTest implements BaseContextTest {
    static final String J = "data_kit.json";

    @Autowired Validator validator;
    JacksonTester<DataKitDefinition> tester;

    static Stream<Arguments> validDataKitsProvider() { return Stream.of(
            arguments("no props"      , builder().kitId(ID).build()),
            arguments("empty prop val", builder().kitId(ID).property("key", "").build()),
            arguments("simple props"  , DEF)); }
    static Stream<Arguments> invalidDataKitsProvider() { return Stream.of(
            arguments("null ID"       , builder().build()),
            arguments("empty ID"      , builder().kitId(" \t\n").build()),
            arguments("illegal ID"    , builder().kitId("db?").build()),
            arguments("null props"    , new DataKitDefinition(ID, null)),
            arguments("null prop key" , builder().kitId(ID).property(null, "val").build()),
            arguments("empty prop key", builder().kitId(ID).property(" \t\n", "val").build()),
            arguments("null prop val" , builder().kitId(ID).property("key", null).build())); }

    @ParameterizedTest(name = "{0}")
    @MethodSource("validDataKitsProvider")
    void testValidDataKits(String title, DataKitDefinition dataKit) {
        // WHEN
        int nViolations = validator.validate(dataKit).size();

        // THEN
        assertEquals(0, nViolations, "expected valid");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidDataKitsProvider")
    void testInvalidDataKits(String title, DataKitDefinition dataKit) {
        // WHEN
        int nViolations = validator.validate(dataKit).size();

        // THEN
        assertEquals(1, nViolations, "expected one violation");
    }

    @Test
    void testRead() throws IOException {
        // WHEN
        DataKitDefinition result = tester.read(J).getObject();

        // THEN
        assertEquals(DEF, result, "wrong data kit definition read");
    }

    @Test
    void testWrite() throws IOException {
        assertThat(json(DEF), equalToJson(str(J)));
    }

    String json(DataKitDefinition d) throws IOException { return tester.write(d).getJson(); }

    static String str(String name, Object... args) { return Resources.asString(name, lookup(), args); }
}
