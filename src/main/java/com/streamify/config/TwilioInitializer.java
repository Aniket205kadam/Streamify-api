package com.streamify.config;

import com.twilio.Twilio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TwilioInitializer {
    private final static Logger LOGGER = LoggerFactory.getLogger(TwilioInitializer.class);

    private final TwilioConfig twilioConfig;

    public TwilioInitializer(TwilioConfig twilioConfig) {
        this.twilioConfig = twilioConfig;
        Twilio.init(
                this.twilioConfig.getAccountSid(),
                this.twilioConfig.getAuthToken()
        );
        LOGGER.info("Twilio configuration is completed with account sid is: {}", this.twilioConfig.getAccountSid());
    }
}
