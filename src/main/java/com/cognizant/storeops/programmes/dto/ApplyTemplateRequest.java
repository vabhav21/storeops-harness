package com.cognizant.storeops.programmes.dto;

import java.util.List;

/** Request body for cloning a planogram task template into a store programme. */
public record ApplyTemplateRequest(
        String templateName,
        List<TemplateTaskSpec> tasks
) {
}
