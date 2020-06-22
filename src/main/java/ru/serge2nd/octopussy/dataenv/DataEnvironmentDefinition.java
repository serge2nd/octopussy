package ru.serge2nd.octopussy.dataenv;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import org.springframework.orm.jpa.vendor.Database;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import static ru.serge2nd.octopussy.config.CommonConfig.*;

@Value
@Builder
public class DataEnvironmentDefinition {
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
}
