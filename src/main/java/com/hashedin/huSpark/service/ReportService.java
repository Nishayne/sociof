package com.hashedin.huSpark.service;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hashedin.huSpark.dto.ReportRequest;
import com.hashedin.huSpark.entity.Post;
import com.hashedin.huSpark.entity.Report;
import com.hashedin.huSpark.entity.ReportStatus;
import com.hashedin.huSpark.entity.User;
import com.hashedin.huSpark.exception.ResourceNotFoundException;
import com.hashedin.huSpark.exception.UnauthorizedException;
import com.hashedin.huSpark.repository.PostRepository;
import com.hashedin.huSpark.repository.ReportRepository;

/**
 * Service for report-related operations.
 */
@Service
public class ReportService {

    private final Logger log = LoggerFactory.getLogger(ReportService.class);

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserService userService;

    /**
     * Report a post.
     * @param reportRequest Report request
     * @param reporterId ID of user reporting the post
     * @return Created report
     */
    @Transactional
    @CacheEvict(value = {"reports", "reportStats"}, allEntries = true)
    public Report reportPost(ReportRequest reportRequest, Long reporterId) {
        log.info("ReportService: reportPost: ReporterId: {}, PostId: {}", reporterId, reportRequest.getPostId());

        Post post = postRepository.findById(reportRequest.getPostId())
                .orElseThrow(() -> {
                    log.warn("Post not found with id: {}", reportRequest.getPostId());
                    return new ResourceNotFoundException("Post not found with id: " + reportRequest.getPostId());
                });

        User reporter = userService.findById(reporterId);

        Report report = Report.builder()
                .post(post)
                .reporter(reporter)
                .justification(reportRequest.getJustification())
                .status(ReportStatus.PENDING)
                .build();

        Report savedReport = reportRepository.saveAndFlush(report); // Changed from save to saveAndFlush
        log.info("Report created successfully: ReportId: {}", savedReport.getId());
        return savedReport;
    }

    /**
     * Find a report by ID.
     * @param id ID of report to find
     * @return Report
     * @throws ResourceNotFoundException if report is not found
     */
    public Report findById(Long id) {
        log.info("ReportService: findById: ReportId: {}", id);
        return reportRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Report not found with id: {}", id);
                    return new ResourceNotFoundException("Report not found with id: " + id);
                });
    }

    /**
     * Moderate a report.
     * @param id ID of report to moderate
     * @param approved Whether to approve or reject the report
     * @param moderatorId ID of moderator
     * @return Moderated report
     */
    @Transactional
    @CacheEvict(value = {"reports", "reportStats", "posts", "postStats"}, allEntries = true)
    public Report moderateReport(Long id, boolean approved, Long moderatorId) {
        log.info("ReportService: moderateReport: ReportId: {}, Approved: {}, ModeratorId: {}", id, approved, moderatorId);

        Report report = findById(id);

        // Check if current user is an admin
        User moderator = userService.findById(moderatorId);
        if (!moderator.getIsAdmin()) {
            log.warn("User {} is not authorized to moderate reports.", moderatorId);
            throw new UnauthorizedException("Only admins can moderate reports");
        }

        // Update report status
        report.setStatus(approved ? ReportStatus.APPROVED : ReportStatus.REJECTED);
        report.setModerator(moderator);
        report.setModeratedAt(new Date());

        // If approved, delete the post
        if (approved) {
            postRepository.delete(report.getPost());
            log.info("Post {} deleted due to report approval.", report.getPost().getId());
        }

        Report moderatedReport = reportRepository.saveAndFlush(report); // Changed from save to saveAndFlush
        log.info("Report moderated successfully: ReportId: {}", moderatedReport.getId());
        return moderatedReport;
    }

    /**
     * Get all reports.
     * @param status Optional status filter
     * @param pageable Pagination parameters
     * @return Page of reports
     */
    public Page<Report> getAllReports(ReportStatus status, Pageable pageable) {
        log.info("ReportService: getAllReports: Status: {}, Pageable: {}", status, pageable);
        Page<Report> reports;
        if (status != null) {
            reports = reportRepository.findByStatus(status, pageable);
        } else {
            reports = reportRepository.findAll(pageable);
        }
        log.info("Found {} reports.", reports.getTotalElements());
        return reports;
    }

    /**
     * Get reports for a post.
     * @param postId ID of post
     * @return List of reports
     */
    public List<Report> getReportsByPost(Long postId) {
        log.info("ReportService: getReportsByPost: PostId: {}", postId);
        List<Report> reports = reportRepository.findByPostId(postId);
        log.info("Found {} reports for post {}.", reports.size(), postId);
        return reports;
    }

    /**
     * Get reports by reporter.
     * @param reporterId ID of reporter
     * @return List of reports
     */
    public List<Report> getReportsByReporter(Long reporterId) {
        log.info("ReportService: getReportsByReporter: ReporterId: {}", reporterId);
        List<Report> reports = reportRepository.findByReporterId(reporterId);
        log.info("Found {} reports by reporter {}.", reports.size(), reporterId);
        return reports;
    }

    /**
     * Get reports for posts by a user.
     * @param userId ID of user
     * @return List of reports
     */
    public List<Report> getReportsByReportedUser(Long userId) {
        log.info("ReportService: getReportsByReportedUser: UserId: {}", userId);
        List<Report> reports = reportRepository.findByReportedUserId(userId);
        log.info("Found {} reports for posts by user {}.", reports.size(), userId);
        return reports;
    }

    /**
     * Get report statistics by creation date.
     * @return List of objects containing date and count
     */
    @Cacheable(value = "reportStats", key = "'byDate'")
    public List<Object[]> getReportStatsByDate() {
        log.info("ReportService: getReportStatsByDate");
        List<Object[]> stats = reportRepository.countReportsByCreationDate();
        log.info("Found {} report stats by date.", stats.size());
        return stats;
    }

    /**
     * Get report statistics by user.
     * @return List of objects containing user ID and count
     */
    @Cacheable(value = "reportStats", key = "'byUser'")
    public List<Object[]> getReportStatsByUser() {
        log.info("ReportService: getReportStatsByUser");
        List<Object[]> stats = reportRepository.countReportsByUser();
        log.info("Found {} report stats by user.", stats.size());
        return stats;
    }

    /**
     * Get report statistics by file type.
     * @return List of objects containing file type and count
     */
    @Cacheable(value = "reportStats", key = "'byFileType'")
    public List<Object[]> getReportStatsByFileType() {
        log.info("ReportService: getReportStatsByFileType");
        List<Object[]> stats = reportRepository.countReportsByFileType();
        log.info("Found {} report stats by file type.", stats.size());
        return stats;
    }
}