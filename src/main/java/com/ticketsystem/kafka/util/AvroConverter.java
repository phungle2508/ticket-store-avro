package com.ticketsystem.kafka.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AvroConverter {

    private static final Logger LOG = LoggerFactory.getLogger(AvroConverter.class);

    // Private constructor to prevent instantiation of utility class
    private AvroConverter() {
    }

    /**
     * Creates a generic Avro EventEnvelope for any event.
     * 
     * @param eventName   The name of the event
     * @param avroPayload The Avro payload object
     * @return The created Avro EventEnvelope
     */
    public static <T> Object createGenericEvent(String eventName, T avroPayload) {
        LOG.debug("Creating generic Avro EventEnvelope for eventName: {}, payload: {}", eventName, avroPayload);

        // This would need to be adapted based on your actual Avro schema
        // For now, creating a simple wrapper
        return new GenericEventEnvelope(eventName, avroPayload);
    }

    /**
     * Converts an Avro payload to a service-specific DTO.
     *
     * @param <T>         The target service DTO type
     * @param <S>         The source Avro payload type
     * @param avroPayload The Avro payload
     * @param targetClass The target DTO class
     * @param converter   Function to convert from Avro to service DTO
     * @return The corresponding service DTO, or null if avroPayload is null
     */
    public static <T, S> T convertToServiceDTO(S avroPayload, Class<T> targetClass,
            java.util.function.Function<S, T> converter) {
        if (avroPayload == null) {
            LOG.warn("Attempted to convert a null Avro payload to service DTO of type: {}",
                    targetClass.getSimpleName());
            return null;
        }

        LOG.debug("Converting Avro payload to service DTO of type: {}", targetClass.getSimpleName());

        try {
            T result = converter.apply(avroPayload);
            LOG.debug("Successfully converted Avro payload to service DTO: {}", result);
            return result;
        } catch (Exception e) {
            LOG.error("Error converting Avro payload to service DTO of type {}: {}",
                    targetClass.getSimpleName(), e.getMessage(), e);
            throw new RuntimeException("Failed to convert Avro payload to service DTO", e);
        }
    }

    /**
     * Generic EventEnvelope wrapper
     */
    public static class GenericEventEnvelope {
        private final String eventName;
        private final Object payload;

        public GenericEventEnvelope(String eventName, Object payload) {
            this.eventName = eventName;
            this.payload = payload;
        }

        public String getEventName() {
            return eventName;
        }

        public Object getPayload() {
            return payload;
        }

        @Override
        public String toString() {
            return "GenericEventEnvelope{eventName='" + eventName + "', payload=" + payload + "}";
        }
    }
}