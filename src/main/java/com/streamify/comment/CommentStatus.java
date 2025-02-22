package com.streamify.comment;

import lombok.Getter;

@Getter
public enum CommentStatus {
    ACTIVE("active"),
    DELETED("deleted"),
    HIDDEN("hidden")
    ;

    private final String name;

    CommentStatus(String name) {
        this.name = name;
    }
}
