package com.ticketsystem.kafka.handler;

// wraps any payload with an eventName

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Generic event envelope that wraps event data with metadata
 */
public class EventEnvelope<T> {

    private final String eventName;
    private final T payload;
    private final long timestamp;
    private final String eventId;

    @JsonCreator
    public EventEnvelope(
            @JsonProperty("eventName") String eventName,
            @JsonProperty("payload") T payload) {
        this(eventName, payload, System.currentTimeMillis(), java.util.UUID.randomUUID().toString());
    }

    public EventEnvelope(String eventName, T payload, long timestamp, String eventId) {
        this.eventName = eventName;
        this.payload = payload;
        this.timestamp = timestamp;
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public T getPayload() {
        return payload;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getEventId() {
        return eventId;
    }

    @Override
    public String toString() {
        return String.format("EventEnvelope{eventName='%s', eventId='%s', timestamp=%d, payload=%s}",
                eventName, eventId, timestamp, payload);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        EventEnvelope<?> that = (EventEnvelope<?>) o;

        if (timestamp != that.timestamp)
            return false;
        if (!eventName.equals(that.eventName))
            return false;
        if (!eventId.equals(that.eventId))
            return false;
        return payload != null ? payload.equals(that.payload) : that.payload == null;
    }

    @Override
    public int hashCode() {
        int result = eventName.hashCode();
        result = 31 * result + (payload != null ? payload.hashCode() : 0);
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + eventId.hashCode();
        return result;
    }
}
