package com.cognizant.storeops.reports.repository;

import com.cognizant.storeops.reports.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/** Data-access layer for Report — the only table the reports module writes to (Rule 5). */
public interface ReportRepository extends JpaRepository<Report, UUID> {

    List<Report> findByScopeId(String scopeId);
}
