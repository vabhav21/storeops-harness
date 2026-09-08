package com.cognizant.storeops.activities.model;

/** Lifecycle status of an operational activity. DONE is terminal — see TaskServiceImpl.updateStatus. */
public enum TaskStatus {
    TODO,
    IN_PROGRESS,
    DONE,
    BLOCKED
}
