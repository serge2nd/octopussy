package ru.serge2nd.octopussy.support;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import ru.serge2nd.octopussy.spi.DataEnvironment;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Map;

import static ru.serge2nd.octopussy.spi.DataEnvironment.errDataEnvUnwrap;

@Value
@Builder
public class DataEnvironmentDefinition implements DataEnvironment {
    @NotNull @Pattern(regexp = "[-_\\p{Alnum}]+")
    String envId;

    @NotNull @Singular
    Map<@NotBlank String, @NotNull Object> properties;

    public DataEnvironmentDefinition(@JsonProperty("envId")      String envId,
                                     @JsonProperty("properties") Map<String, Object> properties) {
        this.envId = envId;
        this.properties = properties;
    }

    @Override @JsonIgnore
    public DataEnvironmentDefinition getDefinition() { return this; }

    @Override @SuppressWarnings("unchecked")
    public <T> T unwrap(Class<T> t) {
        if (t.isAssignableFrom(this.getClass())) return (T)this;
        throw errDataEnvUnwrap(this.getClass(), t);
    }

    @Override @JsonIgnore
    public boolean isClosed() { return true; }
    @Override
    public void close() { /* NO-OP */ }
}
