package com.cognizant.storeops.reports.service;

import com.cognizant.storeops.reports.dto.StoreSummaryResponse;

/**
 * Business logic for the reports module.
 *
 * Every method here is an aggregation. Rule 5: this module holds no write
 * path into activities, programmes, or staff — the only thing it persists
 * is its own Report record.
 */
public interface ReportService {

    StoreSummaryResponse generateStoreSummary(String storeId);
}
