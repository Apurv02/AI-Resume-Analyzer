package com.apurv.resumeanalyzer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AiAnalysisServiceTest {
    @Test
    void usesLocalFallbackWhenApiKeyIsNotConfigured() {
        AiAnalysisService service = new AiAnalysisService(new ObjectMapper(), "", "gpt-4.1-mini");
        var result = service.analyze("Java Spring Boot MongoDB REST APIs", "Required Java Spring Boot and MongoDB experience");
        assertTrue(result.score() >= 0 && result.score() <= 100);
        assertFalse(result.aiPowered());
        assertNotNull(result.feedback());
    }
}
