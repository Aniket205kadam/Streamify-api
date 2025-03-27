package com.streamify.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {
    private String id;
    private String username;
    private String fullName;
    private int followerCount;
    private Boolean isFollowedByCurrentUser;

    @Deprecated
    private String avtarUrl;

    private String avtar;
}
