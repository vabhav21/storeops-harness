package com.cognizant.storeops.shared.events;

/**
 * The only permitted mechanism for a module to trigger a side effect in
 * another module. Direct service-to-service imports across module
 * boundaries for side effects (e.g. programmes -> alerts) are a hard-gate
 * failure in the harness evaluator — see .harness/skills/architecture-principles.
 */
public interface EventBus {
    void emit(String eventType, Object payload);
}
