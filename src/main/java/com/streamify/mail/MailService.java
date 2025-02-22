package com.streamify.mail;

import jakarta.mail.MessagingException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public interface MailService {
    // send confirmation mail
    @Async
    void sendMail(MailConfirmationRequest request) throws MessagingException;

    // send mail
    @Async
    void sendMail(MailRequest request) throws MessagingException;
}
