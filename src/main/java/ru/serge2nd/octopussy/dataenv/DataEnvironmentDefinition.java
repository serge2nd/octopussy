package ru.serge2nd.octopussy.dataenv;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Value
@Builder
public class DataEnvironmentDefinition {
    @NotBlank
    @Pattern(regexp = "[-_\\p{Alnum}]+")
    String envId;
    @NotBlank
    String database;
    @NotBlank
    String driverClassName;
    @NotBlank
    String url;
    @NotBlank
    String login;
    @NotBlank
    String password;

    public DataEnvironmentDefinition(@JsonProperty("envId")
                                             String envId,
                                     @JsonProperty("database")
                                             String database,
                                     @JsonProperty("driverClassName")
                                             String driverClassName,
                                     @JsonProperty("url")
                                             String url,
                                     @JsonProperty("login")
                                             String login,
                                     @JsonProperty("password")
                                             String password) {
        this.envId = envId;
        this.database = database;
        this.driverClassName = driverClassName;
        this.url = url;
        this.login = login;
        this.password = password;
    }
}
