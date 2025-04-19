package com.streamify.aistudio.bot;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BotResponse {
    private String id;
    private String name;
    private String avtar;
}
