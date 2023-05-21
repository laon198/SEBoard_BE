package com.seproject.seboard.domain.repository.report;

import com.seproject.seboard.domain.model.common.ReportThreshold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ReportThresholdRepository extends JpaRepository<ReportThreshold, Long> {
    @Query("select rt from ReportThreshold rt where rt.thresholdType = 'POST'")
    Optional<ReportThreshold> findPostThreshold();
    @Query("select rt from ReportThreshold rt where rt.thresholdType = 'COMMENT'")
    Optional<ReportThreshold> findCommentThreshold();

}