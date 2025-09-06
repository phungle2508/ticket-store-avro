package com.ticketsystem.kafka.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketsystem.kafka.handler.EventEnvelope;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for converting objects to/from Avro format
 * This is a simplified version - replace with actual Avro implementation if
 * needed
 */
public class AvroConverter {

    private static final Logger log = LoggerFactory.getLogger(AvroConverter.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Create a generic event envelope
     */
    public static <T> EventEnvelope<T> createGenericEvent(String eventName, T payload) {
        log.debug("Creating generic event: {} with payload: {}", eventName, payload);
        return new EventEnvelope<>(eventName, payload);
    }

    /**
     * Convert object to Avro bytes (simplified - just JSON serialization)
     */
    public static byte[] toAvroBytes(Object object) {
        try {
            return objectMapper.writeValueAsBytes(object);
        } catch (Exception e) {
            log.error("Error converting to Avro bytes: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to convert to Avro bytes", e);
        }
    }

    /**
     * Convert Avro bytes to object (simplified - just JSON deserialization)
     */
    public static <T> T fromAvroBytes(byte[] bytes, Class<T> clazz) {
        try {
            return objectMapper.readValue(bytes, clazz);
        } catch (Exception e) {
            log.error("Error converting from Avro bytes: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to convert from Avro bytes", e);
        }
    }

    /**
     * Convert object to JSON string
     */
    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            log.error("Error converting to JSON: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to convert to JSON", e);
        }
    }

    /**
     * Convert JSON string to object
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            log.error("Error converting from JSON: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to convert from JSON", e);
        }
    }
}