package ru.serge2nd.octopussy.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryRq {
    @NotBlank
    private String envId;

    @NotNull
    @Size(min = 1)
    private List<String> queries;
}
