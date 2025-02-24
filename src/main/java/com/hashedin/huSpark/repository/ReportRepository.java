package com.hashedin.huSpark.repository;

import com.hashedin.huSpark.model.Report;
import com.hashedin.huSpark.model.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report,Long> {
    List<Report> findByStatus(ReportStatus status);
}
