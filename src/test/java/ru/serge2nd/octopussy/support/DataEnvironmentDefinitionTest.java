package ru.serge2nd.octopussy.support;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.context.annotation.Import;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import ru.serge2nd.octopussy.BaseContextTest;

import javax.validation.Validator;
import java.io.IOException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static ru.serge2nd.octopussy.support.DataEnvironmentDefinition.builder;

@JsonTest @Import(LocalValidatorFactoryBean.class)
@TestInstance(Lifecycle.PER_CLASS)
public class DataEnvironmentDefinitionTest implements BaseContextTest {
    static final String J = "data_env.json";

    public static final String ID = "db1000";
    public static final DataEnvironmentDefinition DEF = builder()
            .envId(ID)
            .property("url", "jdbc:h2:mem:db1000")
            .property("login", "serge")
            .build();

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
        assertThat(tester.write(DEF)).isEqualToJson(J);
    }
}