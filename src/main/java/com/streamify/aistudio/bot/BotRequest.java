package com.streamify.aistudio.bot;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BotRequest {

    @NotNull(message = "First name cannot be null")
    @NotEmpty(message = "First name cannot be empty")
    private String firstname;

    @NotNull(message = "Last name cannot be null")
    @NotEmpty(message = "Last name cannot be empty")
    private String lastname;

    @NotNull(message = "Profession cannot be null")
    @NotEmpty(message = "Profession cannot be empty")
    private String profession;

    @Min(value = 18, message = "Age must be at least 18")
    @Max(value = 100, message = "Age must not be more than 100")
    private int age;

    @NotNull(message = "Gender cannot be null")
    @NotEmpty(message = "Gender cannot be empty")
    private String gender;

    @NotNull(message = "Ethnicity cannot be null")
    @NotEmpty(message = "Ethnicity cannot be empty")
    private String ethnicity;

    @NotNull(message = "Bio cannot be null")
    @NotEmpty(message = "Bio cannot be empty")
    private String bio;

    @NotNull(message = "Interests cannot be null")
    @NotEmpty(message = "Interests cannot be empty")
    private List<String> interests;

    @NotNull(message = "Avatar URL cannot be null")
    @NotEmpty(message = "Avatar URL cannot be empty")
    private String avtar;

    @NotNull(message = "Personality cannot be null")
    @NotEmpty(message = "Personality cannot be empty")
    private String personality;
}
