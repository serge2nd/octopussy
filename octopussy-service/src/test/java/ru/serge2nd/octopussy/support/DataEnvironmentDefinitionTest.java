package ru.serge2nd.octopussy.support;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import ru.serge2nd.octopussy.spi.DataEnvironment;
import ru.serge2nd.octopussy.spi.JpaEnvironment;

import static org.junit.jupiter.api.Assertions.*;
import static ru.serge2nd.octopussy.support.DataEnvironmentDefinition.builder;

@TestInstance(Lifecycle.PER_CLASS)
public class DataEnvironmentDefinitionTest {
    public static final String ID = "db1000";
    public static final DataEnvironmentDefinition DEF = builder()
            .envId(ID)
            .property("url", "jdbc:h2:mem:db1000")
            .property("login", "serge")
            .build();

    @Test void testGetDefinition() { assertSame(DEF, DEF.getDefinition(), "expected this definition"); }
    @Test void testIsClosed()      { assertTrue(DEF.isClosed(), "not always closed"); }
    @Test void testClose()         { assertDoesNotThrow(DEF::close, "noisy close()"); }

    @Test void testUnwrap() { assertAll(() ->
        assertSame(DEF, DEF.unwrap(DataEnvironmentDefinition.class), "must unwrap itself"), () ->
        assertSame(DEF, DEF.unwrap(DataEnvironment.class), "must unwrap " + DataEnvironment.class.getName()), () ->
        assertThrows(IllegalArgumentException.class, ()->DEF.unwrap(JpaEnvironment.class), "expected illegal unwrap"));
    }
}