package com.cognizant.storeops.shared.events;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Publishes StoreOps domain events via Spring's ApplicationEventPublisher.
 * Listeners in other modules (e.g. alerts, reports) subscribe with
 * {@code @EventListener} — they never import the emitting module's service
 * or repository directly.
 */
@Component
public class InMemoryEventBus implements EventBus {

    private final ApplicationEventPublisher publisher;

    public InMemoryEventBus(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void emit(String eventType, Object payload) {
        publisher.publishEvent(new StoreOpsEvent(eventType, payload));
    }

    public static class StoreOpsEvent {
        private final String eventType;
        private final Object payload;

        public StoreOpsEvent(String eventType, Object payload) {
            this.eventType = eventType;
            this.payload = payload;
        }

        public String getEventType() {
            return eventType;
        }

        public Object getPayload() {
            return payload;
        }
    }
}
