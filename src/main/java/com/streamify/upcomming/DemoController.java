package com.streamify.upcomming;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@RestController
@RequestMapping("/Demo")
public class DemoController {
    private int counter = 0;
    private final SimpMessagingTemplate messagingTemplate;

    public DemoController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping("/counter")
    @ResponseBody
    public CounterMessage getCurrentCount() {
        return new CounterMessage(counter);
    }

    // WebSocket endpoint to increment the counter
    @MessageMapping("/increment")
    public void incrementCounter(CounterMessage message) {
        int newCount = counter + 1;

        // Broadcast the new count to all subscribers
        messagingTemplate.convertAndSend("/user/counter", new CounterMessage(newCount));
    }

    // Message payload class
    public static class CounterMessage {
        private int count;

        public CounterMessage() {
        }

        public CounterMessage(int count) {
            this.count = count;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }
}
