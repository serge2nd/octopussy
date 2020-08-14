package ru.serge2nd.octopussy.support;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import org.springframework.transaction.PlatformTransactionManager;
import ru.serge2nd.octopussy.App;
import ru.serge2nd.octopussy.spi.DataEnvironment;

import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Value
@Builder
public class DataEnvironmentDefinition implements DataEnvironment {
    @NotBlank
    @Pattern(regexp = "[-_\\p{Alnum}]+")
    String envId;

    @NotNull
    @Singular
    Map<@NotBlank String, @NotBlank String> properties;

    @Override
    public DataEnvironmentDefinition getDefinition() { return this; }

    public DataEnvironmentDefinition(@JsonProperty(App.DATA_ENV_ID)
                                             String envId,
                                     @JsonProperty(App.DATA_ENV_PROPS)
                                             Map<String, String> properties) {
        this.envId = envId;
        this.properties = properties;
    }

    @Override public void close() { throw new UnsupportedOperationException(); }
    @Override public DataSource getDataSource() { throw new UnsupportedOperationException(); }
    @Override public EntityManagerFactory getEntityManagerFactory() { throw new UnsupportedOperationException(); }
    @Override public PlatformTransactionManager getTransactionManager() { throw new UnsupportedOperationException(); }
}
