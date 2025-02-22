package com.streamify.phone;

import com.streamify.config.TwilioConfig;
import com.streamify.exception.OperationNotPermittedException;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class TwilioPhoneService implements PhoneService {
    private final TwilioConfig twilioConfig;
    private static final Logger LOGGER = LoggerFactory.getLogger(TwilioConfig.class);

    public TwilioPhoneService(TwilioConfig twilioConfig) {
        this.twilioConfig = twilioConfig;
    }

    @Override
    @Async
    public void sendMessage(SMSRequest request) throws IllegalArgumentException {
        System.out.println("Info: " + twilioConfig.getPhoneNumber() + " : " + twilioConfig.getAccountSid() + " : " + twilioConfig.getAuthToken());
        if (isValidNumber(request.getPhoneNumber())) {
            PhoneNumber to = new PhoneNumber(request.getPhoneNumber());
            PhoneNumber from = new PhoneNumber(twilioConfig.getPhoneNumber());
            MessageCreator messageCreator = Message.creator(to, from, request.getMessage());
            LOGGER.info("SMS sending response: {}", messageCreator.create().getStatus());
        } else {
            throw new
                    OperationNotPermittedException("Phone number [" + request.getPhoneNumber() + "] is invalid"); // todo -> make it better
        }
    }

    @Override
    public boolean isValidNumber(String number) {
        try {
            com.twilio.rest.lookups.v2.PhoneNumber phoneNumber =
                    com.twilio.rest.lookups.v2.PhoneNumber
                            .fetcher(number)
                            .fetch();
            return phoneNumber != null;
        } catch (ApiException e) {
            return false;
        }
    }
}
