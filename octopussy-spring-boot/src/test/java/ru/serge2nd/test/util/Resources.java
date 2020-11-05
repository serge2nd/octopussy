package ru.serge2nd.test.util;

import java.io.IOException;
import java.io.InputStream;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.util.StreamUtils.copyToString;

public class Resources {

    public static String asString(String name, Class<?> testClass, Object... args) {
        try (InputStream is = testClass.getResourceAsStream(name)) {
            return format(copyToString(is, UTF_8), args);
        } catch (IOException e) {
            fail("cannot get resource " + name + " as string", e);
        } return null;
    }
}
