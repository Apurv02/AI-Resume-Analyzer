package com.apurv.resumeanalyzer.service;

import com.apurv.resumeanalyzer.model.ResumeAnalysis;
import com.apurv.resumeanalyzer.repository.ResumeAnalysisRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@Service
public class ResumeAnalysisService {
    private final ResumeAnalysisRepository analyses;
    private final PdfTextService pdfTextService;
    private final AiAnalysisService ai;

    public ResumeAnalysisService(ResumeAnalysisRepository analyses, PdfTextService pdfTextService, AiAnalysisService ai) {
        this.analyses = analyses; this.pdfTextService = pdfTextService; this.ai = ai;
    }

    public ResumeAnalysis create(String userId, String userEmail, String name, String jobDescription,
                                 MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Please choose a PDF resume.");
        if (!file.getOriginalFilename().toLowerCase().endsWith(".pdf")
                || (file.getContentType() != null && !file.getContentType().equalsIgnoreCase("application/pdf"))) {
            throw new IllegalArgumentException("Only PDF files are accepted.");
        }
        if (file.getSize() > 5 * 1024 * 1024) throw new IllegalArgumentException("Maximum PDF size is 5 MB.");
        String resumeText = pdfTextService.extractText(file);
        AiAnalysisService.AnalysisResult result = ai.analyze(resumeText, jobDescription);
        ResumeAnalysis record = new ResumeAnalysis();
        record.setUserId(userId);
        record.setUserEmail(userEmail);
        record.setResumeName(name.trim());
        record.setOriginalFileName(file.getOriginalFilename());
        record.setJobDescription(jobDescription.trim());
        record.setScore(result.score());
        record.setFeedback(result.feedback());
        record.setSkills(result.skills());
        return analyses.save(record);
    }

    public List<ResumeAnalysis> history(String userId) {
        return analyses.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public void delete(String id, String userId) {
        ResumeAnalysis record = analyses.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Analysis not found."));
        analyses.delete(record);
    }
}
