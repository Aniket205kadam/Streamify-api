package com.streamify.mail;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
public class MailRequest<T> {
    private String to;
    private MailTemplateName mailTemplate;
    private String subject;
    private Map<String, T> properties;
}
