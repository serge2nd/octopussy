package ru.serge2nd.octopussy.service;

import org.hamcrest.Matcher;
import ru.serge2nd.octopussy.spi.DataKit;
import ru.serge2nd.test.match.builder.MatcherBuilder;

import static ru.serge2nd.test.match.CommonMatch.sameAs;
import static ru.serge2nd.test.match.MatchAssist.not;

public class Matchers {

    public static Matcher<DataKit> isClosed() {
        return new MatcherBuilder<DataKit>(){}.matchIf(DataKit::isClosed).append("data track is closed").alert("was open").build();
    }
    public static Matcher<DataKit> isOpen() {
        return new MatcherBuilder<DataKit>(){}.matchIf(not(DataKit::isClosed)).append("data track is open").alert("was closed").build();
    }

    public static Matcher<DataKitProxy> hasTarget(DataKit expected) {
        return new MatcherBuilder<DataKitProxy>(){}.then(proxy -> proxy.target, sameAs(expected)).build();
    }
    public static Matcher<DataKitProxy> noTarget() { return hasTarget(null); }

    public static Matcher<DataKitProxy> extractsTarget(DataKit expected) {
        return new MatcherBuilder<DataKitProxy>(){}.then(DataKitProxy::getTarget, sameAs(expected)).build();
    }
}
