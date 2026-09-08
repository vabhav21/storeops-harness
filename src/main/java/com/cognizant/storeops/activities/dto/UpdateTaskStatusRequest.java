package com.cognizant.storeops.activities.dto;

/** Request body for a status transition on an existing activity. */
public record UpdateTaskStatusRequest(String status) {
}
