package com.streamify.user;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AboutAccount {
    private String id;
    private String username;
    private String joinedDate;
    private String accountBasedOn;
}
