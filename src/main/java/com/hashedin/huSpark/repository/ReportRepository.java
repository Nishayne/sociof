package com.hashedin.huSpark.repository;

import com.hashedin.huSpark.entity.Report;
import com.hashedin.huSpark.entity.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository for Report entity
 */
@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByPostId(Long postId);

    List<Report> findByReporterId(Long reporterId);

    Page<Report> findByStatus(ReportStatus status, Pageable pageable);

    @Query("SELECT r FROM Report r JOIN r.post p WHERE p.user.id = :userId")
    List<Report> findByReportedUserId(@Param("userId") Long userId);

    @Query("SELECT DATE(r.createdAt) as date, COUNT(r) as count FROM Report r GROUP BY DATE(r.createdAt) ORDER BY date")
    List<Object[]> countReportsByCreationDate();

    @Query("SELECT r.post.user.id as userId, COUNT(r) as count FROM Report r GROUP BY r.post.user.id ORDER BY count DESC")
    List<Object[]> countReportsByUser();

    @Query("SELECT r.post.fileType as fileType, COUNT(r) as count FROM Report r WHERE r.post.fileType IS NOT NULL GROUP BY r.post.fileType ORDER BY count DESC")
    List<Object[]> countReportsByFileType();
}
