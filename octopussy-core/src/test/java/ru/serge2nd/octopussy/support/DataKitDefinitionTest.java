package ru.serge2nd.octopussy.support;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import ru.serge2nd.octopussy.spi.DataKit;
import ru.serge2nd.octopussy.spi.JpaKit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static ru.serge2nd.octopussy.service.Matchers.isClosed;
import static ru.serge2nd.octopussy.support.DataKitDefinition.builder;
import static ru.serge2nd.test.match.AssertThat.assertThat;
import static ru.serge2nd.test.match.CoreMatch.illegalArgument;
import static ru.serge2nd.test.match.CoreMatch.sameAs;

@TestInstance(Lifecycle.PER_CLASS)
public class DataKitDefinitionTest {
    public static final String ID = "db1000";
    public static final DataKitDefinition DEF = builder()
            .kitId(ID)
            .property("url", "jdbc:h2:mem:db1000")
            .property("login", "serge")
            .build();

    @Test void testGetDefinition() { assertThat(DEF.getDefinition(), sameAs(DEF)); }
    @Test void testIsClosed()      { assertThat(DEF, isClosed()); }
    @Test void testClose()         { assertDoesNotThrow(DEF::close, "noisy close()"); }

    @Test void testUnwrap() { assertThat(
        DEF.unwrap(DataKitDefinition.class), sameAs(DEF),
        DEF.unwrap(DataKit.class)          , sameAs(DEF),
        ()->DEF.unwrap(JpaKit.class)       , illegalArgument());
    }
}