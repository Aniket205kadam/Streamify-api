package com.streamify.authentication;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<?> register (
            @RequestBody @Valid RegistrationRequest request
    ) throws Exception {
        authenticationService.register(request);
        return ResponseEntity.accepted().build();
    }

    @PatchMapping("/date-of-birth")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> setDateOfBirth (
            @RequestParam("username") String username,
            @RequestParam("dateOfBirth") LocalDate dataOfBirth
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        authenticationService.setDateOfBirth(username, dataOfBirth)
                );
    }

    @PutMapping("/verification/{username}/{verification-code}")
    public ResponseEntity<?> accountVerification(
            @PathVariable("username") String username,
            @PathVariable("verification-code") String verificationCode
    ) throws Exception {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        authenticationService.accountVerification(username, verificationCode)
                );
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody @Valid AuthenticationRequest request
    ) {
        System.out.println("User try to login: " + request.getIdentifier());
        return ResponseEntity
                .ok(
                        authenticationService.authenticate(request)
                );
    }

    @GetMapping("/exists/email/{email-id}")
    public ResponseEntity<Boolean> isEmailAddressExist(
            @NotNull @NotBlank @Email @PathVariable("email-id") String email
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        authenticationService.isEmailAddressExist(email)
                );
    }

    @GetMapping("/exists/username/{username}")
    public ResponseEntity<Boolean> isUsernameExist(
            @NotNull @NotBlank @PathVariable("username") String username
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        authenticationService.isUsernameExist(username)
                );
    }

    @PostMapping("/forgot-password/{identifier}")
    public ResponseEntity<?> forgotPassword(
            @NotNull @NotBlank @Email @PathVariable("identifier") String identifier
    ) throws Exception {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        authenticationService.forgotPassword(identifier)
                );
    }
}
