package com.streamify.post;

import lombok.Getter;

@Getter
public enum PostVisibility {
    PUBLIC("public"),
    PRIVATE("private"),
    FRIENDS_ONLY("friends-only")
    ;

    private final String name;

    PostVisibility(String name) {
        this.name = name;
    }
}
