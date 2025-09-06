
// KafkaProducer.java
package com.ticketsystem.kafka.broker;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import com.ticketsystem.kafka.service.KafkaUtilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.KafkaHeaders;

@Component("kafkaProducer")
public class KafkaProducer<T, S> implements Supplier<Message<T>> {

    private final KafkaUtilityService<S> kafkaUtilityService;
    private final AtomicReference<T> messageToSupply = new AtomicReference<>();
    private final AtomicReference<String> keyToSupply = new AtomicReference<>();
    private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);

    @Value("${spring.cloud.stream.bindings.kafkaProducer-out-0.destination}")
    private String configuredOutputTopic;

    public KafkaProducer(KafkaUtilityService<S> kafkaUtilityService) {
        this.kafkaUtilityService = kafkaUtilityService;
    }

    public <P> String send(String eventName, P payload) {
        log.info("KafkaProducer: Preparing event and generating unique key.");
        return kafkaUtilityService.producer_prepareAndAttemptDirectSendEvent(
                eventName, payload, configuredOutputTopic,
                (AtomicReference<P>) messageToSupply, keyToSupply);
    }

    @Override
    public Message<T> get() {
        T event = messageToSupply.getAndSet(null);
        String key = keyToSupply.getAndSet(null);

        if (event != null) {
            log.info("Supplying event with unique key: {} to kafkaProducer-out-0 binding", key);
            return MessageBuilder.withPayload(event)
                    .setHeader(KafkaHeaders.KEY, key)
                    .build();
        } else {
            log.trace("No event to supply, returning null.");
            return null;
        }
    }
}