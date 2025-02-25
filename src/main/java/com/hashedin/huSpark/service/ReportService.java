package com.hashedin.huSpark.service;

import com.hashedin.huSpark.dto.ReportDto;
import com.hashedin.huSpark.dto.ReportRequest;
import com.hashedin.huSpark.entity.Post;
import com.hashedin.huSpark.entity.Report;
import com.hashedin.huSpark.entity.ReportStatus;
import com.hashedin.huSpark.entity.User;
import com.hashedin.huSpark.exception.ResourceNotFoundException;
import com.hashedin.huSpark.exception.UnauthorizedException;
import com.hashedin.huSpark.repository.PostRepository;
import com.hashedin.huSpark.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Service for report-related operations
 */
@Service
public class ReportService {
    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserService userService;

    /**
     * Report a post
     * @param reportRequest Report request
     * @param reporterId ID of user reporting the post
     * @return Created report
     */
    @Transactional
    @CacheEvict(value = {"reports", "reportStats"}, allEntries = true)
    public Report reportPost(ReportRequest reportRequest, Long reporterId) {
        Post post = postRepository.findById(reportRequest.getPostId())
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + reportRequest.getPostId()));

        User reporter = userService.findById(reporterId);

        Report report = Report.builder()
                .post(post)
                .reporter(reporter)
                .justification(reportRequest.getJustification())
                .status(ReportStatus.PENDING)
                .build();

        return reportRepository.save(report);
    }

    /**
     * Find a report by ID
     * @param id ID of report to find
     * @return Report
     * @throws ResourceNotFoundException if report is not found
     */
    public Report findById(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + id));
    }

    /**
     * Moderate a report
     * @param id ID of report to moderate
     * @param approved Whether to approve or reject the report
     * @param moderatorId ID of moderator
     * @return Moderated report
     */
    @Transactional
    @CacheEvict(value = {"reports", "reportStats", "posts", "postStats"}, allEntries = true)
    public Report moderateReport(Long id, boolean approved, Long moderatorId) {
        Report report = findById(id);

        // Check if current user is an admin
        User moderator = userService.findById(moderatorId);
        if (!moderator.getIsAdmin()) {
            throw new UnauthorizedException("Only admins can moderate reports");
        }

        // Update report status
        report.setStatus(approved ? ReportStatus.APPROVED : ReportStatus.REJECTED);
        report.setModerator(moderator);
        report.setModeratedAt(new Date());

        // If approved, delete the post
        if (approved) {
            postRepository.delete(report.getPost());
        }

        return reportRepository.save(report);
    }

    /**
     * Get all reports
     * @param status Optional status filter
     * @param pageable Pagination parameters
     * @return Page of reports
     */
    public Page<Report> getAllReports(ReportStatus status, Pageable pageable) {
        if (status != null) {
            return reportRepository.findByStatus(status, pageable);
        }
        return reportRepository.findAll(pageable);
    }

    /**
     * Get reports for a post
     * @param postId ID of post
     * @return List of reports
     */
    public List<Report> getReportsByPost(Long postId) {
        return reportRepository.findByPostId(postId);
    }

    /**
     * Get reports by reporter
     * @param reporterId ID of reporter
     * @return List of reports
     */
    public List<Report> getReportsByReporter(Long reporterId) {
        return reportRepository.findByReporterId(reporterId);
    }

    /**
     * Get reports for posts by a user
     * @param userId ID of user
     * @return List of reports
     */
    public List<Report> getReportsByReportedUser(Long userId) {
        return reportRepository.findByReportedUserId(userId);
    }

    /**
     * Get report statistics by creation date
     * @return List of objects containing date and count
     */
    @Cacheable(value = "reportStats", key = "'byDate'")
    public List<Object[]> getReportStatsByDate() {
        return reportRepository.countReportsByCreationDate();
    }

    /**
     * Get report statistics by user
     * @return List of objects containing user ID and count
     */
    @Cacheable(value = "reportStats", key = "'byUser'")
    public List<Object[]> getReportStatsByUser() {
        return reportRepository.countReportsByUser();
    }

    /**
     * Get report statistics by file type
     * @return List of objects containing file type and count
     */
    @Cacheable(value = "reportStats", key = "'byFileType'")
    public List<Object[]> getReportStatsByFileType() {
        return reportRepository.countReportsByFileType();
    }
}
