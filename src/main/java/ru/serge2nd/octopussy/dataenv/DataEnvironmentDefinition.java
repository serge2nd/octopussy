package ru.serge2nd.octopussy.dataenv;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import org.springframework.orm.jpa.vendor.Database;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

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

    public DataEnvironmentDefinition(@JsonProperty("envId")
                                             String envId,
                                     @JsonProperty("database")
                                             Database database,
                                     @JsonProperty("driverClass")
                                             String driverClass,
                                     @JsonProperty("url")
                                             String url,
                                     @JsonProperty("login")
                                             String login,
                                     @JsonProperty("password")
                                             String password) {
        this.envId = envId;
        this.database = database;
        this.driverClass = driverClass;
        this.url = url;
        this.login = login;
        this.password = password;
    }
}
