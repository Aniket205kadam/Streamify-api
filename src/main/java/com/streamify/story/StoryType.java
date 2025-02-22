package com.streamify.story;

import lombok.Getter;

@Getter
public enum StoryType {
    IMAGE("image"),
    VIDEO("video")
    ;

    private final String name;

    StoryType(String name) {
        this.name = name;
    }
}
