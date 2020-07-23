package ru.serge2nd.octopussy.support;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.transaction.PlatformTransactionManager;
import ru.serge2nd.octopussy.spi.DataEnvironment;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import static ru.serge2nd.octopussy.config.CommonConfig.*;

@Value
@Builder
public class DataEnvironmentDefinition implements DataEnvironment {
    @NotBlank
    @Pattern(regexp = "[-_\\p{Alnum}]+")
    String envId;
    @NotBlank
    Database database;
    @NotBlank
    String driverClass;
    @NotBlank
    String url;
    @NotBlank
    String login;
    @NotBlank
    String password;
    @Override
    public DataEnvironmentDefinition getDefinition() { return this; }

    public DataEnvironmentDefinition(@JsonProperty(DATA_ENV_ID)
                                             String envId,
                                     @JsonProperty(DATA_ENV_DB)
                                             Database database,
                                     @JsonProperty(DATA_ENV_DRIVER_CLASS)
                                             String driverClass,
                                     @JsonProperty(DATA_ENV_URL)
                                             String url,
                                     @JsonProperty(DATA_ENV_LOGIN)
                                             String login,
                                     @JsonProperty(DATA_ENV_PASSWORD)
                                             String password) {
        this.envId = envId;
        this.database = database;
        this.driverClass = driverClass;
        this.url = url;
        this.login = login;
        this.password = password;
    }

    @Override public void close() { throw new UnsupportedOperationException(); }
    @Override public DataSource getDataSource() { throw new UnsupportedOperationException(); }
    @Override public EntityManagerFactory getEntityManagerFactory() { throw new UnsupportedOperationException(); }
    @Override public PlatformTransactionManager getTransactionManager() { throw new UnsupportedOperationException(); }
}
