// KafkaUtilityService.java
package com.ticketsystem.kafka.service;

import com.ticketsystem.kafka.handler.EventDispatcher;
import com.ticketsystem.kafka.handler.EventEnvelope;
import com.ticketsystem.kafka.runner.KafkaJobRunner;
import com.ticketsystem.kafka.util.AvroConverter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.MediaType;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class KafkaUtilityService<T> {

    private static final Logger PRODUCER_LOG = LoggerFactory.getLogger("KafkaProducerHelper");
    private static final Logger CONSUMER_LOG = LoggerFactory.getLogger("KafkaConsumerHelper");

    protected final KafkaJobRunner jobRunner;
    protected final StreamBridge streamBridge;
    protected final ObjectMapper objectMapper;
    protected final RetryTemplate retryTemplate;
    protected final EventDispatcher dispatcher;
    protected final Map<String, SseEmitter> emitters;

    @Value("${kafka.utility.service-name:default-service}-input.dlq")
    private String dlqTopic;

    @Value("${kafka.utility.dlq.enabled:true}")
    private boolean dlqEnabled;

    @Autowired
    public KafkaUtilityService(StreamBridge streamBridge, KafkaJobRunner jobRunner, RetryTemplate retryTemplate,
            EventDispatcher dispatcher, Map<String, SseEmitter> emitters) {
        this.jobRunner = jobRunner;
        this.streamBridge = streamBridge;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.retryTemplate = retryTemplate;
        this.dispatcher = dispatcher;
        this.emitters = emitters;
    }

    // --- Helper for KafkaProducer ---

    public <P> String producer_prepareAndAttemptDirectSendEvent(
            String eventName, P payload,
            String configuredOutputTopic,
            AtomicReference<P> messageToSupply, AtomicReference<String> keyToSupply) {
        try {
            P envelope = (P) AvroConverter.createGenericEvent(eventName, payload);
            messageToSupply.set(envelope);
            String uniqueKey = UUID.randomUUID().toString();
            keyToSupply.set(uniqueKey);
            PRODUCER_LOG.info("Generated unique key for Kafka message: {}", uniqueKey);
            return uniqueKey;
        } catch (Exception e) {
            PRODUCER_LOG.error("Error preparing event for Kafka: {}", e.getMessage(), e);
            messageToSupply.set(null);
            keyToSupply.set(null);
            return null;
        }
    }

    // --- Helpers for KafkaConsumer ---

    public <U> void submitEventJob(EventEnvelope<U> avroMessage, String keyStr) {
        // Convert to JsonNode format for the job runner
        EventEnvelope<JsonNode> jsonNodeMessage;
        try {
            if (avroMessage.getPayload() instanceof JsonNode) {
                jsonNodeMessage = (EventEnvelope<JsonNode>) avroMessage;
            } else {
                // Convert payload to JsonNode
                String payloadJson = objectMapper.writeValueAsString(avroMessage.getPayload());
                JsonNode payloadJsonNode = objectMapper.readTree(payloadJson);
                jsonNodeMessage = new EventEnvelope<>(avroMessage.getEventName(), payloadJsonNode);
            }
            jobRunner.submitEventJob(jsonNodeMessage, keyStr, () -> consumeAndProcessMessage(avroMessage));
        } catch (Exception e) {
            CONSUMER_LOG.error("Error converting message payload to JsonNode: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to submit event job", e);
        }
    }

    public <U> void consumeAndProcessMessage(EventEnvelope<U> avroMessage) {
        try {
            retryTemplate.execute(context -> {
                String eventName = null;
                T serviceDTO = null;

                try {
                    eventName = avroMessage.getEventName();
                    if (eventName == null) {
                        throw new IllegalArgumentException("Event name cannot be null");
                    }

                    if (avroMessage.getPayload() != null) {
                        // Generic processing - delegates to specific implementation
                        serviceDTO = consumer_mapToServiceDTO(avroMessage.getPayload());

                        if (serviceDTO != null) {
                            EventEnvelope<JsonNode> envelopeForDispatcher = consumer_prepareEventEnvelopeForDispatcher(
                                    eventName, serviceDTO);
                            if (envelopeForDispatcher != null) {
                                dispatcher.dispatch(envelopeForDispatcher);
                            } else {
                                CONSUMER_LOG.warn("Envelope for dispatcher was null after preparation for event: {}",
                                        eventName);
                            }
                        } else {
                            CONSUMER_LOG.warn("Service DTO was null after mapping for event: {}", eventName);
                        }
                    } else {
                        CONSUMER_LOG.warn("Unsupported event type or missing payload: {}", eventName);
                    }

                    // SSE dispatch
                    consumer_dispatchToSseClients(eventName, serviceDTO, emitters);

                    CONSUMER_LOG.info("Successfully processed message for event: {}", eventName);

                    // Send notification to completion topic
                    String completionTopic = "processing-complete";
                    streamBridge.send(completionTopic,
                            "Completed processing for event " + eventName + " with ID " + UUID.randomUUID());

                    return null;
                } catch (Exception e) {
                    CONSUMER_LOG.error("Error processing message (event: {}, attempt {}): {}",
                            (eventName != null ? eventName : "UNKNOWN_EVENT"),
                            context.getRetryCount() + 1,
                            e.getMessage(), e);
                    throw e;
                }
            }, context -> {
                CONSUMER_LOG.error("Message processing failed after {} attempts, sending to DLQ. Event: {}",
                        context.getRetryCount(),
                        (avroMessage.getEventName() != null ? avroMessage.getEventName() : "UNKNOWN_EVENT"));
                Exception exception = (Exception) context.getLastThrowable();
                consumer_sendToDlq(avroMessage, exception, dlqTopic, dlqEnabled, avroMessage.toString());
                throw exception;
            });
        } catch (Exception e) {
            CONSUMER_LOG.error(
                    "Fatal error in message processing: {}. Sending to DLQ. Event: {}",
                    e.getMessage(),
                    (avroMessage.getEventName() != null ? avroMessage.getEventName() : "UNKNOWN_EVENT"), e);
            consumer_sendToDlq(avroMessage, e, dlqTopic, dlqEnabled, avroMessage.toString());
            throw new RuntimeException("Fatal error in message processing, sent to DLQ: " + e.getMessage(), e);
        }
    }

    /**
     * Abstract method for mapping payload to service DTO - should be overridden by
     * specific implementations
     */
    public <U> T consumer_mapToServiceDTO(U avroPayload) {
        CONSUMER_LOG.debug("Mapping payload to service DTO: {}", avroPayload);
        // Default implementation returns null - should be overridden by specific
        // implementations
        return null;
    }

    public EventEnvelope<JsonNode> consumer_prepareEventEnvelopeForDispatcher(String eventName, T serviceDto) {
        if (serviceDto == null) {
            CONSUMER_LOG.warn("ServiceDTO is null, cannot prepare EventEnvelope for dispatcher for event: {}",
                    eventName);
            return new EventEnvelope<>(eventName, null);
        }
        try {
            String dtoJson = objectMapper.writeValueAsString(serviceDto);
            JsonNode dtoJsonNode = objectMapper.readTree(dtoJson);
            return new EventEnvelope<>(eventName, dtoJsonNode);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            CONSUMER_LOG.error("Error serializing/deserializing service DTO for dispatcher: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to prepare EventEnvelope for dispatcher", e);
        }
    }

    public void consumer_dispatchToSseClients(String eventName, T dto, Map<String, SseEmitter> emittersMap) {
        CONSUMER_LOG.debug("Dispatching event {} to {} SSE clients", eventName, emittersMap.size());
        emittersMap.forEach((key, emitter) -> {
            try {
                emitter.send(
                        SseEmitter.event()
                                .name(eventName)
                                .data(dto != null ? dto : "No payload", MediaType.APPLICATION_JSON));
                CONSUMER_LOG.trace("Successfully sent event {} to SSE client with key {}", eventName, key);
            } catch (IOException | IllegalStateException ex) {
                CONSUMER_LOG.debug("Failed to send to SSE client with key {}, removing emitter. Error: {}", key,
                        ex.getMessage());
                emittersMap.remove(key);
            }
        });
    }

    public SseEmitter consumer_registerSseEmitter(String key, Map<String, SseEmitter> emittersMap) {
        CONSUMER_LOG.debug("Registering sse client for {}", key);
        SseEmitter emitter = new SseEmitter();
        emitter.onCompletion(() -> {
            CONSUMER_LOG.debug("SSE Emitter completed for key {}. Removing from map.", key);
            emittersMap.remove(key);
        });
        emitter.onError((error) -> {
            CONSUMER_LOG.debug("SSE Emitter error for key {}. Removing from map. Error: {}", key, error.getMessage());
            emittersMap.remove(key);
        });
        emittersMap.put(key, emitter);
        return emitter;
    }

    public void consumer_unregisterSseEmitter(String key, Map<String, SseEmitter> emittersMap) {
        CONSUMER_LOG.debug("Unregistering sse emitter for: {}", key);
        SseEmitter emitter = emittersMap.get(key);
        if (emitter != null) {
            emitter.complete();
            emittersMap.remove(key);
        } else {
            CONSUMER_LOG.warn("No SSE emitter found for key {} to unregister.", key);
        }
    }

    public <U> void consumer_sendToDlq(U message, Throwable exception, String dlqTopic, boolean dlqEnabled,
            String originalMessageToString) {
        if (!dlqEnabled) {
            CONSUMER_LOG.info("DLQ is disabled. Not sending message for error: {}", exception.getMessage());
            return;
        }
        try {
            CONSUMER_LOG.info("Sending failed message to DLQ topic: {}", dlqTopic);

            Map<String, Object> dlqMessage = new HashMap<>();
            dlqMessage.put("originalMessage", originalMessageToString);
            dlqMessage.put("error", exception.getMessage());
            dlqMessage.put("errorType", exception.getClass().getName());
            dlqMessage.put("timestamp", System.currentTimeMillis());

            streamBridge.send(dlqTopic, dlqMessage);
            CONSUMER_LOG.info("Message sent to DLQ successfully");
        } catch (Exception e) {
            CONSUMER_LOG.error("Failed to send message to DLQ", e);
        }
    }

    public Map<String, SseEmitter> getEmitters() {
        return emitters;
    }
}
