package com.streamify.authentication;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponse {
    private String token;
    private String username;

    @Deprecated
    private String profileUrl;
    private String avtar;
    private LocalDateTime createdAt;
    private Date validateAt;
}
