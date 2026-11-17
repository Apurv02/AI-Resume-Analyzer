package com.apurv.resumeanalyzer.repository;

import com.apurv.resumeanalyzer.model.ResumeAnalysis;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface ResumeAnalysisRepository extends MongoRepository<ResumeAnalysis, String> {
    List<ResumeAnalysis> findByUserIdOrderByCreatedAtDesc(String userId);
    Optional<ResumeAnalysis> findByIdAndUserId(String id, String userId);
    long countByUserId(String userId);
    List<ResumeAnalysis> findAllByOrderByCreatedAtDesc();
}
