package com.streamify.authentication;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationRequest {
    @NotEmpty(message = "FullName is mandatory")
    @NotBlank(message = "FullName is mandatory")
    private String fullName;

    @Email(message = "Email is not formatted")
    private String email;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone number must be valid (e.g., +1234567890)")
    private String phone;

    @NotEmpty(message = "Password is mandatory")
    @NotBlank(message = "Password is mandatory")
    @Size(min = 8, message = "Password should be 8 characters long minimum")
    private String password;

    @NotEmpty(message = "Username is mandatory")
    @NotBlank(message = "Username is mandatory")
    private String username;
}
