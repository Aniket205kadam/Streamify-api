package com.streamify.user;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private String id;
    private String username;
    private String fullName;
    private String bio;
    private String profilePictureUrl;
    private String website;
    private String avtar;
    private String gender;
    private int followerCount;
    private int followingCount;
    private int postsCount;
}
