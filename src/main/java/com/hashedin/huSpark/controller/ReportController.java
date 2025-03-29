package com.hashedin.huSpark.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

/**
 * Controller for report operations.
 * Handles reporting posts, moderating reports, and retrieving report
 * information.
 */
@RestController
@RequestMapping("/api/reports")
@Api(tags = "Reports")
public class ReportController {

    private final Logger log = LoggerFactory.getLogger(ReportController.class);
    private final ReportService reportService;
    private final ModelMapper modelMapper = new ModelMapper();

    /**
     * Constructor for ReportController.
     * 
     * @param reportService Service for report operations.
     */
    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Reports a post.
     * 
     * @param reportRequest Report request.
     * @param currentUser   Current authenticated user.
     * @return ResponseEntity containing the created ReportDto or an error response.
     */
    @PostMapping
    @ApiOperation("Report a post")
    public ResponseEntity<ReportDto> reportPost(
            @Valid @RequestBody ReportRequest reportRequest,
            @CurrentUser UserPrincipal currentUser) {
        log.info("ReportController: reportPost: UserId: " + currentUser.getId());
        try {
            Report report = reportService.reportPost(reportRequest, currentUser.getId());
            return ResponseEntity.ok(modelMapper.map(report, ReportDto.class));
        } catch (Exception e) {
            log.error("Failed to report post: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Retrieves a report by its ID. 
     * @param id          Report ID.
     * @param currentUser current authenticated/login user
     * @return ResponseEntity containing the ReportDto or an error response.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiOperation("Get a report by ID (Admin only)")
    public ResponseEntity<ReportDto> getReportById(@PathVariable Long id, @CurrentUser UserPrincipal currentUser) {
        log.info("ReportController: getReportById: ReportId: " + id);
        try {
            Report report = reportService.findById(id, currentUser.getId());
            return ResponseEntity.ok(modelMapper.map(report, ReportDto.class));
        } catch (Exception e) {
            log.error("Failed to get report by ID: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Moderates a report.
     * @param id          Report ID.
     * @param approved    Whether to approve or reject the report.
     * @param currentUser Current authenticated user.
     * @return ResponseEntity containing the moderated ReportDto or an error
     *         response.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiOperation("Moderate a report (Admin only)")
    public ResponseEntity<ReportDto> moderateReport(
            @PathVariable Long id,
            @RequestParam boolean approved,
            @CurrentUser UserPrincipal currentUser) {
        log.info("ReportController: moderateReport: ReportId: " + id);
        try {
            Report report = reportService.moderateReport(id, approved, currentUser.getId());
            return ResponseEntity.ok(modelMapper.map(report, ReportDto.class));
        } catch (Exception e) {
            log.error("Failed to moderate report: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Retrieves all reports. 
     * @param status      Optional status filter.
     * @param pageable    Pagination parameters.
     * @param currentUser current authenticated/login user
     * @return ResponseEntity containing a page of ReportDto or an error response.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiOperation("Get all reports (Admin only)")
    public ResponseEntity<Page<ReportDto>> getAllReports(
            @RequestParam(required = false) ReportStatus status,
            Pageable pageable, @CurrentUser UserPrincipal currentUser) {
        log.info("ReportController: getAllReports: Status: " + status);
        try {
            Page<Report> reports = reportService.getAllReports(status, pageable, currentUser.getId());
            // Page<ReportDto> reportDtoPage = reports.map(report -> modelMapper.map(report,
            // ReportDto.class));

            List<ReportDto> reportDtos = reports.stream().map(report -> {
                try {
                    Hibernate.initialize(report.getPost().getComments());
                } catch (Exception e) {
                }
                try {
                    Hibernate.initialize(report.getPost().getLikes());
                } catch (Exception e) {
                }
                return modelMapper.map(report, ReportDto.class);
            }).collect(Collectors.toList());

            // Return correctly paginated response
            Page<ReportDto> reportDtoPage = new PageImpl<>(reportDtos, pageable, reports.getTotalElements());

            return ResponseEntity.ok(reportDtoPage);
        } catch (Exception e) {
            log.error("Failed to get all reports: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Retrieves reports for a post.
     * @param postId      Post ID.
     * @param currentUser current authenticated/login user
     * @return ResponseEntity containing a list of ReportDto or an error response.
     */
    @GetMapping("/post/{postId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiOperation("Get reports for a post (Admin only)")
    public ResponseEntity<List<ReportDto>> getReportsByPost(@PathVariable Long postId,
            @CurrentUser UserPrincipal currentUser) {
        log.info("ReportController: getReportsByPost: PostId: " + postId);
        try {
            List<Report> reports = reportService.getReportsByPost(postId, currentUser.getId());
            List<ReportDto> reportDtos = reports.stream()
                    .map(report -> modelMapper.map(report, ReportDto.class))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(reportDtos);
        } catch (Exception e) {
            log.error("Failed to get reports by post: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Retrieves report statistics by date.
     * @param currentUser current authenticated/login user
     * @return ResponseEntity containing a list of objects containing date and count
     *         or an error response.
     */
    @GetMapping("/stats/by-date")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiOperation("Get report statistics by date (Admin only)")
    public ResponseEntity<List<Object[]>> getReportStatsByDate(@CurrentUser UserPrincipal currentUser) {
        log.info("ReportController: getReportStatsByDate");
        try {
            List<Object[]> stats = reportService.getReportStatsByDate(currentUser.getId());
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Failed to get report statistics by date: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Retrieves report statistics by user.
     * @param currentUser current authenticated/login user
     * @return ResponseEntity containing a list of objects containing user ID and
     *         count or an error response.
     */
    @GetMapping("/stats/by-user")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiOperation("Get report statistics by user (Admin only)")
    public ResponseEntity<List<Object[]>> getReportStatsByUser(@CurrentUser UserPrincipal currentUser) {
        log.info("ReportController: getReportStatsByUser");
        try {
            List<Object[]> stats = reportService.getReportStatsByUser(currentUser.getId());
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Failed to get report statistics by user: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Retrieves report statistics by file type.
     * @param currentUser current authenticated/login user
     * @return ResponseEntity containing a list of objects containing file type and
     *         count or an error response.
     */
    @GetMapping("/stats/by-file-type")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiOperation("Get report statistics by file type (Admin only)")
    public ResponseEntity<List<Object[]>> getReportStatsByFileType(@CurrentUser UserPrincipal currentUser) {
        log.info("ReportController: getReportStatsByFileType");
        try {
            List<Object[]> stats = reportService.getReportStatsByFileType(currentUser.getId());
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Failed to get report statistics by file type: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}