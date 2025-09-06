package com.ticketsystem.kafka.handler;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Event dispatcher for handling different types of events.
 * Routes events to appropriate handlers based on event name.
 */
@Component
public class EventDispatcher implements ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(EventDispatcher.class);

    private final Map<String, Consumer<EventEnvelope<JsonNode>>> eventHandlers;
    private ApplicationContext applicationContext;

    /**
     * Default constructor
     */
    public EventDispatcher() {
        this.eventHandlers = new ConcurrentHashMap<>();
        log.info("EventDispatcher initialized");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        // Auto-discover handlers if needed
        discoverEventHandlers();
    }

    /**
     * Register an event handler for a specific event type
     */
    public void registerHandler(String eventName, Consumer<EventEnvelope<JsonNode>> handler) {
        log.info("Registering handler for event: {}", eventName);
        eventHandlers.put(eventName, handler);
    }

    /**
     * Unregister an event handler
     */
    public void unregisterHandler(String eventName) {
        log.info("Unregistering handler for event: {}", eventName);
        eventHandlers.remove(eventName);
    }

    /**
     * Dispatch an event to the appropriate handler
     */
    public void dispatch(EventEnvelope<JsonNode> eventEnvelope) {
        if (eventEnvelope == null) {
            log.warn("Cannot dispatch null event envelope");
            return;
        }

        String eventName = eventEnvelope.getEventName();
        if (eventName == null || eventName.trim().isEmpty()) {
            log.warn("Cannot dispatch event with null or empty event name");
            return;
        }

        log.debug("Dispatching event: {} with payload: {}", eventName, eventEnvelope.getPayload());

        Consumer<EventEnvelope<JsonNode>> handler = eventHandlers.get(eventName);
        if (handler != null) {
            try {
                long startTime = System.currentTimeMillis();
                handler.accept(eventEnvelope);
                long processingTime = System.currentTimeMillis() - startTime;
                log.debug("Event {} processed successfully in {}ms", eventName, processingTime);
            } catch (Exception e) {
                log.error("Error processing event: {} - {}", eventName, e.getMessage(), e);
                handleDispatchError(eventEnvelope, e);
            }
        } else {
            log.warn("No handler registered for event: {}", eventName);
            handleUnhandledEvent(eventEnvelope);
        }
    }

    /**
     * Dispatch event asynchronously
     */
    public void dispatchAsync(EventEnvelope<JsonNode> eventEnvelope) {
        // For now, just call dispatch synchronously
        // In a more advanced implementation, you might use @Async or CompletableFuture
        dispatch(eventEnvelope);
    }

    /**
     * Handle dispatch errors - can be overridden by subclasses
     */
    protected void handleDispatchError(EventEnvelope<JsonNode> eventEnvelope, Exception error) {
        log.error("Failed to dispatch event: {}", eventEnvelope.getEventName(), error);
        // Could implement retry logic, DLQ, or error notifications here
    }

    /**
     * Handle events with no registered handlers - can be overridden by subclasses
     */
    protected void handleUnhandledEvent(EventEnvelope<JsonNode> eventEnvelope) {
        log.info("Unhandled event: {} - consider registering a handler", eventEnvelope.getEventName());
        // Could implement default handling, logging, or forwarding here
    }

    /**
     * Get all registered event types
     */
    public Map<String, Consumer<EventEnvelope<JsonNode>>> getRegisteredHandlers() {
        return new ConcurrentHashMap<>(eventHandlers);
    }

    /**
     * Check if a handler is registered for an event
     */
    public boolean hasHandler(String eventName) {
        return eventHandlers.containsKey(eventName);
    }

    /**
     * Get the number of registered handlers
     */
    public int getHandlerCount() {
        return eventHandlers.size();
    }

    /**
     * Clear all handlers
     */
    public void clearHandlers() {
        log.info("Clearing all event handlers");
        eventHandlers.clear();
    }

    /**
     * Auto-discover event handlers from Spring context
     * Override this method to implement custom handler discovery
     */
    protected void discoverEventHandlers() {
        if (applicationContext == null) {
            return;
        }

        // Example: Find beans that implement EventHandler interface
        // Map<String, EventHandler> handlers =
        // applicationContext.getBeansOfType(EventHandler.class);
        // for (Map.Entry<String, EventHandler> entry : handlers.entrySet()) {
        // EventHandler handler = entry.getValue();
        // registerHandler(handler.getEventType(), handler::handle);
        // }

        log.debug("Event handler discovery completed");
    }

    /**
     * Register a simple lambda handler
     */
    public void on(String eventName, Consumer<JsonNode> payloadHandler) {
        registerHandler(eventName, envelope -> {
            if (envelope.getPayload() != null) {
                payloadHandler.accept(envelope.getPayload());
            }
        });
    }

    /**
     * Get dispatcher statistics
     */
    public DispatcherStats getStats() {
        return new DispatcherStats(eventHandlers.size(), eventHandlers.keySet());
    }

    /**
     * Dispatcher statistics
     */
    public static class DispatcherStats {
        private final int handlerCount;
        private final java.util.Set<String> registeredEvents;

        public DispatcherStats(int handlerCount, java.util.Set<String> registeredEvents) {
            this.handlerCount = handlerCount;
            this.registeredEvents = new java.util.HashSet<>(registeredEvents);
        }

        public int getHandlerCount() {
            return handlerCount;
        }

        public java.util.Set<String> getRegisteredEvents() {
            return registeredEvents;
        }

        @Override
        public String toString() {
            return String.format("DispatcherStats{handlers=%d, events=%s}", handlerCount, registeredEvents);
        }
    }
}
