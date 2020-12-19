package ru.serge2nd.octopussy.support;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import ru.serge2nd.octopussy.spi.DataKit;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Map;

/**
 * A static configuration sufficient to create a full-fledged {@link DataKit}.<br>
 * @implNote This implementation is immutable and not capable to query or update some storage.
 */
@Value
@Builder
public class DataKitDefinition implements DataKit {
    @NotNull @Pattern(regexp = "[-_\\p{Alnum}]+")
    String kitId;

    @NotNull @Singular
    Map<@NotBlank String, @NotNull Object> properties;

    public DataKitDefinition(@JsonProperty("kitId")      String kitId,
                             @JsonProperty("properties") Map<String, Object> properties) {
        this.kitId = kitId;
        this.properties = properties;
    }

    @Override @JsonIgnore
    public DataKitDefinition getDefinition() { return this; }

    @Override @JsonIgnore
    public boolean isClosed() { return true; }
    @Override
    public void close() { /* NO-OP */ }
}
