package com.cognizant.storeops.reports.routes;

import com.cognizant.storeops.reports.dto.StoreSummaryResponse;
import com.cognizant.storeops.reports.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Routes layer for reports. Read-only by construction — no write verbs are exposed (Rule 5). */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/store/{storeId}")
    public ResponseEntity<StoreSummaryResponse> storeSummary(@PathVariable String storeId) {
        return ResponseEntity.ok(reportService.generateStoreSummary(storeId));
    }
}
