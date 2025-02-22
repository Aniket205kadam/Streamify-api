package com.streamify.phone;

import com.twilio.rest.api.v2010.account.Message.Status;
import org.springframework.stereotype.Service;

@Service
public interface PhoneService {
    // send a verification code
    void sendMessage(SMSRequest request) throws Exception;

    // validate the phone number
    boolean isValidNumber(String to);

}
