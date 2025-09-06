// KafkaConsumer.java
package com.ticketsystem.kafka.broker;

import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.ticketsystem.kafka.handler.EventEnvelope;
import com.ticketsystem.kafka.service.KafkaUtilityService;
import org.springframework.messaging.Message;

@Component
public class KafkaConsumer<T, S> implements Consumer<Message<EventEnvelope<T>>> {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaConsumer.class);
    private final KafkaUtilityService<S> kafkaUtilityService;

    public KafkaConsumer(KafkaUtilityService<S> kafkaUtilityService) {
        this.kafkaUtilityService = kafkaUtilityService;
    }

    @Override
    public void accept(Message<EventEnvelope<T>> message) {
        EventEnvelope<T> avroMessage = message.getPayload();
        Object keyObject = message.getHeaders().get("kafka_receivedMessageKey");
        String keyStr = keyObject != null ? keyObject.toString() : null;

        LOG.debug("Got message with key [{}]: {}", keyStr, avroMessage);
        kafkaUtilityService.submitEventJob(avroMessage, keyStr);
    }

    public SseEmitter register(String key) {
        return kafkaUtilityService.consumer_registerSseEmitter(key, kafkaUtilityService.getEmitters());
    }

    public void unregister(String key) {
        kafkaUtilityService.consumer_unregisterSseEmitter(key, kafkaUtilityService.getEmitters());
    }
}
