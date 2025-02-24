package com.hashedin.huSpark.service;

import com.hashedin.huSpark.model.Post;
import com.hashedin.huSpark.model.Report;
import com.hashedin.huSpark.model.ReportStatus;
import com.hashedin.huSpark.model.User;
import com.hashedin.huSpark.repository.PostRepository;
import com.hashedin.huSpark.repository.ReportRepository;
import com.hashedin.huSpark.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReportService {
    private final ReportRepository reportRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    public ReportService(ReportRepository reportRepository,PostRepository postRepository,UserRepository userRepository){
        this.reportRepository=reportRepository;
        this.postRepository=postRepository;
        this.userRepository=userRepository;
    }
    public Report createReport(Long postId,Long userId, String reason){
        Post post=postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
        User user=userRepository.findById(userId).orElseThrow(() ->  new RuntimeException("User not found"));
        Report report=Report.builder()
                .post(post)
                .reportedBy(user)
                .reason(reason)
                .status(ReportStatus.PENDING)
                .reportedAt(LocalDateTime.now())
                .build();
        return reportRepository.save(report);
    }

    public List<Report> getPendingReports(){
        return reportRepository.findByStatus(ReportStatus.PENDING);
    }

    public void reviewReport(Long reportId,ReportStatus status){
        Report report=reportRepository.findById(reportId).orElseThrow(()->new RuntimeException("Report not found"));
        report.setStatus(status);
        reportRepository.save(report);
    }

}
