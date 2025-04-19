package com.streamify.authentication;

import com.streamify.Storage.FileUtils;
import com.streamify.exception.OperationNotPermittedException;
import com.streamify.mail.*;
import com.streamify.phone.PhoneService;
import com.streamify.phone.SMSRequest;
import com.streamify.security.JwtService;
import com.streamify.user.*;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final MailService mailService;
    private final PhoneService phoneService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Value("${application.mailing.activation-url}")
    private String activationUrl;

    @Value("${application.Security.jwt.expiration}")
    private long jwtExpiration;

    public AuthenticationService(UserRepository authenticationRepository, TokenRepository tokenRepository, MailService mailService, PhoneService phoneService, AuthenticationManager authenticationManager, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.userRepository = authenticationRepository;
        this.tokenRepository = tokenRepository;
        this.mailService = mailService;
        this.phoneService = phoneService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(RegistrationRequest request) throws Exception {
        // todo - add the role feature
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .username(request.getUsername())
                .isVerified(false)
                .isPrivate(false)
                .allowMessageRequest(true)
                .twoFactorEnable(false)
                .accountStatus(AccountStatus.DEACTIVATED)
                .build();
        userRepository.save(user);
        sendValidationToken(user);
    }

    private void sendValidationToken(User user) throws Exception {
        String newToken = generateAndSaveActivationToken(user);
        String finalActivationUrl = activationUrl + File.separator + newToken;
        // check where send the otp
        if (user.getEmail() != null && user.getPhone() == null) {
            mailService.sendMail(
                    MailConfirmationRequest
                            .builder()
                            .to(user.getEmail())
                            .mailTemplate(MailTemplateName.ACTIVATE_ACCOUNT)
                            .verificationUrl(finalActivationUrl)
                            .verificationCode(newToken)
                            .subject("Account activation")
                            .build()
            );
            return;
        }
        if (user.getPhone() != null && user.getEmail() == null) {
            String msg = String.format(
                    """
                            🔐 Your Streamify verification code is: %s

                            Welcome to Streamify! 🎉

                            Enter this code to verify your account and start exploring amazing content.

                            Never share this code with anyone.

                            Happy Streaming! 🚀""",
                    newToken
            );
            SMSRequest request = SMSRequest.builder()
                    .phoneNumber(user.getPhone())
                    .message(msg)
                    .build();
            phoneService.sendMessage(request);
            return;
        }
        throw new OperationNotPermittedException("Both email and phone are missing or both are provided. One must be specified for OTP delivery.");
    }

    private String generateAndSaveActivationToken(User user) {
        String generatedToken = generateActivationCode(6);
        Token token = Token.builder()
                .token(generatedToken)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .user(user)
                .build();
        tokenRepository.save(token);
        return tokenRepository.save(token).getToken();
    }

    private String generateActivationCode(int length) {
        String characters = "0123456789";
        StringBuilder codeBuilder = new StringBuilder();
        SecureRandom secureRandom = new SecureRandom();
        for (int i = 0; i < length; i++) {
            int randomIdx = secureRandom.nextInt(characters.length());
            codeBuilder.append(characters.charAt(randomIdx));
        }
        return codeBuilder.toString();
    }

    public String setDateOfBirth(
            @NonNull String username,
            @NonNull LocalDate dataOfBirth) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new EntityNotFoundException("User is not found with username: " + username)
                );
        if (dataOfBirth.isAfter(LocalDate.now())) {
            throw new OperationNotPermittedException("Date of Birth cannot be in the future.");
        }
        if (Period.between(dataOfBirth, LocalDate.now()).getYears() < 16) {
            throw new OperationNotPermittedException("User must be at least 16 years old.");
        }
        user.setDateOfBirth(dataOfBirth);
        return userRepository.save(user).getId();
    }

    public String accountVerification(String username, String verificationCode) throws Exception {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new EntityNotFoundException("User is not found with username: " + username)
                );
        Token token = tokenRepository.findByToken(username, verificationCode)
                .orElseThrow(() ->
                        new EntityNotFoundException("Token is not found with username: " + username + " and verificationCode: " + verificationCode)
                );
        if (user.getDateOfBirth() == null) {
            throw new OperationNotPermittedException("Enter your date of birth first to receive the verification code.");
        }
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            sendValidationToken(user);
            throw new OperationNotPermittedException("Verification token expired! We've sent a new one to your email. Please check your inbox.");
        }
        if (token.getValidatedAt() != null) {
            throw new OperationNotPermittedException("A new token has been sent! The previous one is already validated.");
        }
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setVerified(true);
        userRepository.save(user);

        token.setValidatedAt(LocalDateTime.now());
        tokenRepository.save(token);
        AuthenticationRequest request = AuthenticationRequest.builder()
                .identifier(user.getUsername())
                .password(user.getPassword())
                .build();
        return user.getId();
    }

    private String getImageType(String fileName) {
        if (fileName == null || fileName.isEmpty())
            throw new RuntimeException("Filename is needed to find the file extensions!");
        return fileName.substring(fileName.lastIndexOf("."));
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        User user = null;
        Map<String, Object> claims = new HashMap<>();
        if (request.getIdentifier().contains("@")) {
            user = userRepository.findByEmail(request.getIdentifier())
                    .orElseThrow(() -> new EntityNotFoundException("User is not found with Email: " + request.getIdentifier()));
        } else if (request.getIdentifier().matches("^(\\+\\d{1,3}[- ]?)?\\(?\\d{1,4}\\)?[- ]?\\d{1,4}[- ]?\\d{1,4}$")) {
            user = userRepository.findByPhone(request.getIdentifier())
                    .orElseThrow(() -> new EntityNotFoundException("User is not found with Phone: " + request.getIdentifier()));
        } else {
            user = userRepository.findByUsername(request.getIdentifier())
                    .orElseThrow(() -> new EntityNotFoundException("User is not found with username: " + request.getIdentifier()));
        }
        claims.put("username", user.getUsername());
        if (request.isInternal()) {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), request.getPassword())
            );
        } else {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), request.getPassword())
            );
        }

        String jwtToken = jwtService.generateJwtToken(claims, user);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String avtar;
        if (user.getProfilePictureUrl() == null) {
            avtar = "data:image/" + "png" + ";base64," + Base64.getEncoder().encodeToString(FileUtils.readFileFromLocation("D:\\Spring Boot Project\\streamify\\profile-assets\\common-avatar\\no-profile-image.jpg"));
        } else {
            avtar = "data:image/" + getImageType(user.getProfilePictureUrl()) + ";base64," + Base64.getEncoder().encodeToString(FileUtils.readFileFromLocation(user.getProfilePictureUrl()));
        }

        return AuthenticationResponse
                .builder()
                .token(jwtToken)
                .username(user.getUsername())
                .profileUrl(user.getProfilePictureUrl())
                .avtar(avtar)
                .createdAt(LocalDateTime.now())
                .validateAt(new Date(System.currentTimeMillis() + jwtExpiration))
                .build();
    }

    public Boolean isEmailAddressExist(String email) {
        return userRepository.existsByEmail(email);
    }

    public Boolean isUsernameExist(String username) {
        return userRepository.existsByUsername(username);
    }

    public String forgotPassword(String identifier) throws Exception {
        User user = null;
        if (identifier.contains("@")) {
            user = userRepository.findByEmail(identifier)
                    .orElseThrow(() ->
                            new EntityNotFoundException("We couldn't find a user with the email: " + identifier +". Please check again or register!")
                    );
            return generateNewPasswordAndUpdatePreviousByMail(user);
        } else if (identifier.matches("^(\\+\\d{1,3}[- ]?)?\\(?\\d{1,4}\\)?[- ]?\\d{1,4}[- ]?\\d{1,4}$")) {
            user = userRepository.findByPhone(identifier)
                    .orElseThrow(() ->
                            new EntityNotFoundException("We couldn't find a user with the phone: " + identifier +". Please check again or register!")
                    );
            return generateNewPasswordAndUpdatePreviousByPhone(user);
        } else {
            user = userRepository.findByUsername(identifier)
                    .orElseThrow(() ->
                            new EntityNotFoundException("We couldn't find a user with the username: " + identifier +". Please check again or register!")
                    );
            if (user.getEmail() != null) {
                return generateNewPasswordAndUpdatePreviousByMail(user);
            }
            return generateNewPasswordAndUpdatePreviousByPhone(user);
        }
    }

    private String generateNewPassword() {
        final String ALL_CHARACTERS = shuffleAll("abcdefghijklmnopqrstuvwxyz" + "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "0123456789" + "!@#$%^&*()-_=+[{]}\\|;:'\",<.>/?");
        StringBuilder newPassword = new StringBuilder();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < 8; i++) {
            int randomIdx = random.nextInt();
            newPassword.append(ALL_CHARACTERS.charAt(randomIdx));
        }
        return newPassword.toString();
    }

    private String generateNewPasswordAndUpdatePreviousByPhone(User user) throws Exception {
        String newPassword = generateNewPassword();
        String msg = String.format(
                """
                        🔑 Your new Streamify password is: %s
        
                        Welcome back to Streamify! 🎉
        
                        Use this password to log in and start enjoying your favorite content.
        
                        To change your password, go to Settings > Reset Password.
        
                        Happy Streaming! 🚀
                """,
                newPassword
        );
        phoneService.sendMessage(
                SMSRequest
                        .builder()
                        .phoneNumber(user.getPhone())
                        .message(msg)
                        .build()
        );
        return resetPassword(user, newPassword);
    }

    private String generateNewPasswordAndUpdatePreviousByMail(User user) throws MessagingException {
        String newPassword = generateNewPassword();
        Map<String, String> properties = new HashMap<>();
        properties.put("newPassword", newPassword);
        mailService.sendMail(
                MailRequest.<String>builder()
                        .to(user.getEmail())
                        .subject("Reset Password")
                        .mailTemplate(MailTemplateName.FORGOT_PASSWORD)
                        .properties(properties)
                        .build()
        );
        return resetPassword(user, newPassword);
    }

    private String resetPassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user).getId();
    }

    private String shuffleAll(String string) {
        List<Character> characters = new ArrayList<>();
        for (char character : string.toCharArray()) {
            characters.add(character);
        }
        Collections.shuffle(characters);
        StringBuilder shuffledString = new StringBuilder();
        for (char character : characters) {
            shuffledString.append(character);
        }
        return shuffledString.toString();
    }
}
