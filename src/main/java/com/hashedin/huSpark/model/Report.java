package com.hashedin.huSpark.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="post_id",nullable=false)
    private Post post;

    @ManyToOne
    @JoinColumn(name="reported_by",nullable = false)
    private  User reportedBy;

    @Column(nullable=false)
    private String reason;

    @Enumerated(EnumType.STRING)
    private ReportStatus status;

    private LocalDateTime reportedAt;
}
