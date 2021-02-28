package ru.serge2nd.test.util;

import lombok.SneakyThrows;
import org.hamcrest.Matcher;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import ru.serge2nd.test.match.builder.MatcherBuilder;

import static org.skyscreamer.jsonassert.JSONCompareMode.LENIENT;
import static ru.serge2nd.test.Env.EOL;

public class CustomMatchers {

    public static Matcher<String> equalToJson(String expected) {
        return equalToJson(expected, LENIENT);
    }

    public static Matcher<String> equalToJson(String expected, JSONCompareMode mode) {
        return new MatcherBuilder<String>(){}
                .then(json -> compareJson(expected, json, mode))
                .matchIf(JSONCompareResult::passed)
                .append("JSONs are equal" + EOL)
                .alert(r -> EOL + "mismatch: " + r.getMessage())
                .build();
    }

    @SneakyThrows
    static JSONCompareResult compareJson(String expected, String actual, JSONCompareMode mode) {
        return JSONCompare.compareJSON(expected, actual, mode);
    }
}
