package com.streamify.phone;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SMSRequest {
    private String phoneNumber;
    private String message;
}
