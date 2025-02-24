package com.hashedin.huSpark.controller;

import com.hashedin.huSpark.model.Report;
import com.hashedin.huSpark.model.ReportStatus;
import com.hashedin.huSpark.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reports")
public class ReportController {
    private final ReportService reportService;
    public ReportController(ReportService reportService){
        this.reportService=reportService;
    }
    @PostMapping("/create")
    public ResponseEntity<Report> createReport(@RequestBody Map<String, Object> request){
        Long postId=((Number) request.get("postId")).longValue();
        Long userId=((Number) request.get("userId")).longValue();
        String reason=(String) request.get("reason");
        return ResponseEntity.ok(reportService.createReport(postId,userId,reason));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Report>> getPendingReports(){
        return ResponseEntity.ok(reportService.getPendingReports());
    }

    @PostMapping("/review/{reportId}")
    public ResponseEntity<String> reviewReport(@PathVariable Long reportId, @RequestBody Map<String, String> request){
        ReportStatus status = ReportStatus.valueOf(request.get("status"));
        reportService.reviewReport(reportId,status);
        return ResponseEntity.ok("Report reviewed successfully");
    }
}
