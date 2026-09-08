package com.cognizant.storeops.programmes.model;

/** Lifecycle status of a store programme. CLOSED triggers a STORE_SUMMARY report via the event bus. */
public enum ProjectStatus {
    DRAFT,
    ACTIVE,
    CLOSED
}
