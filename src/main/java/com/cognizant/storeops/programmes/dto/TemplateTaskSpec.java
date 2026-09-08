package com.cognizant.storeops.programmes.dto;

/**
 * One task definition inside a planogram task template. Carried on the
 * PROGRAMME_TEMPLATE_APPLIED event payload — the activities module listens
 * for this event and creates the corresponding Task rows itself. programmes
 * never writes directly to the activities repository.
 */
public record TemplateTaskSpec(
        String title,
        String department,
        String priority,
        String category
) {
}
