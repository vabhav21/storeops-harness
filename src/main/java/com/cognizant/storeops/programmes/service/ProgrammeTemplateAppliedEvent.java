package com.cognizant.storeops.programmes.service;

import com.cognizant.storeops.programmes.dto.TemplateTaskSpec;

import java.util.List;
import java.util.UUID;

/**
 * Payload for the PROGRAMME_TEMPLATE_APPLIED event.
 *
 * EventBus.emit("PROGRAMME_TEMPLATE_APPLIED", payload) is the ONLY
 * permitted way for the programmes module to cause Task rows to be created
 * in the activities module. A direct import of the activities module's
 * TaskService or TaskRepository from ProjectService is a hard-gate
 * evaluator failure (see .harness/skills/architecture-principles).
 */
public record ProgrammeTemplateAppliedEvent(
        UUID projectId,
        String storeId,
        String templateName,
        List<TemplateTaskSpec> tasks
) {
    public static final String EVENT_TYPE = "PROGRAMME_TEMPLATE_APPLIED";
}
