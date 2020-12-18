package ru.serge2nd.octopussy.support;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.context.annotation.Import;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import ru.serge2nd.test.util.Resources;

import javax.validation.Validator;
import java.io.IOException;
import java.util.stream.Stream;

import static java.lang.invoke.MethodHandles.lookup;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static ru.serge2nd.octopussy.support.DataEnvironmentDefinition.builder;
import static ru.serge2nd.octopussy.support.DataEnvironmentDefinitionTest.DEF;
import static ru.serge2nd.octopussy.support.DataEnvironmentDefinitionTest.ID;
import static ru.serge2nd.test.match.AssertThat.assertThat;
import static ru.serge2nd.test.util.CustomMatchers.equalToJson;

@JsonTest @Import(LocalValidatorFactoryBean.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DataEnvironmentDefinitionValidationTest {
    static final String J = "data_env.json";

    @Autowired JacksonTester<DataEnvironmentDefinition> tester;
    @Autowired Validator validator;

    static Stream<Arguments> validDataEnvsProvider() { return Stream.of(
            arguments("no props", builder().envId(ID).build()),
            arguments("empty prop val", builder().envId(ID).property("key", "").build()),
            arguments("simple props", DEF)); }
    static Stream<Arguments> invalidDataEnvsProvider() { return Stream.of(
            arguments("null ID", builder().build()),
            arguments("empty ID", builder().envId(" \t\n").build()),
            arguments("illegal ID", builder().envId("db?").build()),
            arguments("null props", new DataEnvironmentDefinition(ID, null)),
            arguments("null prop key", builder().envId(ID).property(null, "val").build()),
            arguments("empty prop key", builder().envId(ID).property(" \t\n", "val").build()),
            arguments("null prop val", builder().envId(ID).property("key", null).build())); }

    @ParameterizedTest(name = "{0}")
    @MethodSource("validDataEnvsProvider")
    void testValidDataEnvs(String title, DataEnvironmentDefinition dataEnv) {
        // WHEN
        int nViolations = validator.validate(dataEnv).size();

        // THEN
        assertEquals(0, nViolations, "expected valid");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidDataEnvsProvider")
    void testInvalidDataEnvs(String title, DataEnvironmentDefinition dataEnv) {
        // WHEN
        int nViolations = validator.validate(dataEnv).size();

        // THEN
        assertEquals(1, nViolations, "expected one violation");
    }

    @Test
    void testRead() throws IOException {
        // WHEN
        DataEnvironmentDefinition result = tester.read(J).getObject();

        // THEN
        assertEquals(DEF, result, "wrong data env definition read");
    }

    @Test
    void testWrite() throws IOException {
        assertThat(json(DEF), equalToJson(str(J)));
    }

    String json(DataEnvironmentDefinition d) throws IOException { return tester.write(d).getJson(); }

    static String str(String name, Object... args) { return Resources.asString(name, lookup().lookupClass(), args); }
}
