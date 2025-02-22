package com.streamify.authentication;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AuthenticationRequest {
    @NotEmpty(message = "Please provide your identifier (email, phone, or username) to proceed!")
    @NotBlank(message = "Please provide your identifier (email, phone, or username) to proceed!")
    private String identifier;

    @NotEmpty(message = "Please enter your password to continue!")
    @NotBlank(message = "Please enter your password to continue!")
    private String password;

    private boolean isInternal;
}
