package com.streamify.mail;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MailConfirmationRequest {
    private String to;
    private MailTemplateName mailTemplate;
    private String verificationUrl;
    private String verificationCode;
    private String subject;
}
