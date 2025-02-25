package com.hashedin.huSpark.controller;

import com.hashedin.huSpark.dto.ReportDto;
import com.hashedin.huSpark.dto.ReportRequest;
import com.hashedin.huSpark.entity.Report;
import com.hashedin.huSpark.entity.ReportStatus;
import com.hashedin.huSpark.security.CurrentUser;
import com.hashedin.huSpark.security.UserPrincipal;
import com.hashedin.huSpark.service.ReportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for report operations
 */
@RestController
@RequestMapping("/api/reports")
@Api(tags = "Reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * Report a post
     * @param reportRequest Report request
     * @param currentUser Current authenticated user
     * @return Created report
     */
    @PostMapping
    @ApiOperation("Report a post")
    public ResponseEntity<Report> reportPost(
            @Valid @RequestBody ReportRequest reportRequest,
            @CurrentUser UserPrincipal currentUser) {
        Report report = reportService.reportPost(reportRequest, currentUser.getId());
        return ResponseEntity.ok(report);
    }

    /**
     * Get a report by ID
     * @param id Report ID
     * @return Report
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation("Get a report by ID (Admin only)")
    public ResponseEntity<Report> getReportById(@PathVariable Long id) {
        Report report = reportService.findById(id);
        return ResponseEntity.ok(report);
    }

    /**
     * Moderate a report
     * @param id Report ID
     * @param approved Whether to approve or reject the report
     * @param currentUser Current authenticated user
     * @return Moderated report
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation("Moderate a report (Admin only)")
    public ResponseEntity<Report> moderateReport(
            @PathVariable Long id,
            @RequestParam boolean approved,
            @CurrentUser UserPrincipal currentUser) {
        Report report = reportService.moderateReport(id, approved, currentUser.getId());
        return ResponseEntity.ok(report);
    }

    /**
     * Get all reports
     * @param status Optional status filter
     * @param pageable Pagination parameters
     * @return Page of reports
     */
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation("Get all reports (Admin only)")
    public ResponseEntity<Page<Report>> getAllReports(
            @RequestParam(required = false) ReportStatus status,
            Pageable pageable) {
        Page<Report> reports = reportService.getAllReports(status, pageable);
        return ResponseEntity.ok(reports);
    }

    /**
     * Get reports for a post
     * @param postId Post ID
     * @return List of reports
     */
    @GetMapping("/post/{postId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation("Get reports for a post (Admin only)")
    public ResponseEntity<List<Report>> getReportsByPost(@PathVariable Long postId) {
        List<Report> reports = reportService.getReportsByPost(postId);
        return ResponseEntity.ok(reports);
    }

    /**
     * Get report statistics by date
     * @return List of objects containing date and count
     */
    @GetMapping("/stats/by-date")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation("Get report statistics by date (Admin only)")
    public ResponseEntity<List<Object[]>> getReportStatsByDate() {
        List<Object[]> stats = reportService.getReportStatsByDate();
        return ResponseEntity.ok(stats);
    }

    /**
     * Get report statistics by user
     * @return List of objects containing user ID and count
     */
    @GetMapping("/stats/by-user")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation("Get report statistics by user (Admin only)")
    public ResponseEntity<List<Object[]>> getReportStatsByUser() {
        List<Object[]> stats = reportService.getReportStatsByUser();
        return ResponseEntity.ok(stats);
    }

    /**
     * Get report statistics by file type
     * @return List of objects containing file type and count
     */
    @GetMapping("/stats/by-file-type")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation("Get report statistics by file type (Admin only)")
    public ResponseEntity<List<Object[]>> getReportStatsByFileType() {
        List<Object[]> stats = reportService.getReportStatsByFileType();
        return ResponseEntity.ok(stats);
    }
}