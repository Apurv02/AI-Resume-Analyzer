package com.apurv.resumeanalyzer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.*;
import java.util.regex.*;

@Service
public class AiAnalysisService {
    private final RestClient restClient;
    private final ObjectMapper mapper;
    private final String apiKey;
    private final String model;

    public AiAnalysisService(ObjectMapper mapper,
            @Value("${app.openai.api-key:}") String apiKey,
            @Value("${app.openai.model:gpt-4.1-mini}") String model) {
        this.mapper = mapper; this.apiKey = apiKey; this.model = model;
        this.restClient = RestClient.builder().baseUrl("https://api.openai.com/v1").build();
    }

    public AnalysisResult analyze(String resumeText, String jobDescription) {
        if (apiKey == null || apiKey.isBlank()) {
            return fallback(resumeText, jobDescription,
                    "AI provider is not configured. This result is a local keyword-based estimate, not an AI assessment.");
        }
        try {
            String prompt = """
                Compare this resume against the job description as a fair ATS-style reviewer.
                Return ONLY JSON with keys: score (integer 0-100), feedback (string with concise actionable advice),
                skills (array of up to 12 relevant skill strings).
                Do not invent qualifications. Mention important missing skills and strengths.
                JOB DESCRIPTION:
                %s
                RESUME:
                %s
                """.formatted(jobDescription, resumeText);
            Map<String,Object> body = Map.of(
                "model", model,
                "messages", List.of(
                    Map.of("role","system","content","You are a careful resume analysis assistant. Return valid JSON only."),
                    Map.of("role","user","content",prompt)
                ),
                "response_format", Map.of("type","json_object"),
                "temperature", 0.2
            );
            JsonNode response = restClient.post().uri("/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON).body(body).retrieve().body(JsonNode.class);
            String content = response.path("choices").path(0).path("message").path("content").asText();
            JsonNode result = mapper.readTree(content);
            int score = Math.max(0, Math.min(100, result.path("score").asInt(0)));
            String feedback = result.path("feedback").asText("No feedback returned.");
            List<String> skills = new ArrayList<>();
            result.path("skills").forEach(n -> { if (n.isTextual()) skills.add(n.asText()); });
            return new AnalysisResult(score, feedback, skills, true);
        } catch (Exception ex) {
            return fallback(resumeText, jobDescription,
                    "AI service could not complete the analysis (" + safeMessage(ex.getMessage()) +
                    "). A local keyword estimate is shown instead. Check the API key, model and network.");
        }
    }

    private AnalysisResult fallback(String resume, String jd, String note) {
        Set<String> jdTerms = tokenize(jd);
        Set<String> resumeTerms = tokenize(resume);
        Set<String> matches = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        matches.addAll(resumeTerms);
        matches.retainAll(jdTerms);
        int score = jdTerms.isEmpty() ? 0 : (int)Math.round(100.0 * matches.size() / jdTerms.size());
        score = Math.min(score, 100);
        List<String> skills = matches.stream().filter(s -> s.length() > 2).limit(12).toList();
        String feedback = note + " Matched terms: " +
                (skills.isEmpty() ? "none detected" : String.join(", ", skills)) +
                ". Tailor the resume to the role's required skills and provide evidence through projects or experience.";
        return new AnalysisResult(score, feedback, skills, false);
    }

    private Set<String> tokenize(String text) {
        Set<String> words = new HashSet<>();
        Matcher matcher = Pattern.compile("[A-Za-z][A-Za-z+#.]{2,}").matcher(text.toLowerCase(Locale.ROOT));
        Set<String> stop = Set.of("the","and","for","with","you","your","are","our","will","this","that","from","have","has","job","role","work","using","years","experience","required","skills");
        while (matcher.find()) if (!stop.contains(matcher.group())) words.add(matcher.group());
        return words;
    }
    private String safeMessage(String message) {
        if (message == null || message.isBlank()) return "unknown error";
        return message.length() > 180 ? message.substring(0,180) : message;
    }
    public record AnalysisResult(int score, String feedback, List<String> skills, boolean aiPowered) {}
}
