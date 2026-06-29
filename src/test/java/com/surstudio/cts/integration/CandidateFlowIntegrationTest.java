package com.surstudio.cts.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.surstudio.cts.identity.domain.Role;
import com.surstudio.cts.identity.domain.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full E2E flow against a real PostgreSQL container (Testcontainers).
 * Validates Flyway migrations, JPA mappings, security rules, and server-side scoring.
 */
class CandidateFlowIntegrationTest extends AbstractIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;

    private String candidateToken;
    private String adminToken;
    private String recruiterToken;

    @BeforeEach
    void setUp() throws Exception {
        candidateToken = registerAndGetToken("cand_" + System.nanoTime() + "@test.com");

        String adminEmail = "adm_" + System.nanoTime() + "@test.com";
        adminToken = registerAndGetToken(adminEmail);
        userRepository.findByEmail(adminEmail).ifPresent(u -> {
            u.setRole(Role.ADMIN);
            userRepository.save(u);
        });

        String recruiterEmail = "rec_" + System.nanoTime() + "@test.com";
        recruiterToken = registerAndGetToken(recruiterEmail);
        userRepository.findByEmail(recruiterEmail).ifPresent(u -> {
            u.setRole(Role.RECRUITER);
            userRepository.save(u);
        });
    }

    @Test
    void fullCandidateFlow_returnsHonestScore() throws Exception {
        // Admin creates a test
        MvcResult createResult = mockMvc.perform(post("/api/v1/tests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content(objectMapper.writeValueAsString(
                                Map.of("skill", "acrobacia", "title", "Test M5"))))
                .andExpect(status().isCreated())
                .andReturn();

        Map<String, Object> test = parseBody(createResult);
        Long testId = toLong(test.get("id"));

        // Admin adds a question with 3 options (position 1 = correct)
        mockMvc.perform(post("/api/v1/tests/" + testId + "/questions")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + adminToken)
                .content(objectMapper.writeValueAsString(Map.of(
                        "text", "¿Cuántas fases tiene un salto acrobático?",
                        "position", 0,
                        "options", List.of(
                                Map.of("text", "2 fases", "correct", false, "position", 0),
                                Map.of("text", "3 fases", "correct", true,  "position", 1),
                                Map.of("text", "4 fases", "correct", false, "position", 2)
                        )))));

        // Candidate views test — trust boundary: response must NOT contain "correct"
        MvcResult testView = mockMvc.perform(get("/api/v1/tests/" + testId)
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(testView.getResponse().getContentAsString()).doesNotContain("correct");

        // Candidate starts attempt
        MvcResult attemptResult = mockMvc.perform(post("/api/v1/tests/" + testId + "/attempts")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isCreated())
                .andReturn();
        Long attemptId = toLong(parseBody(attemptResult).get("attemptId"));

        // Find the correct option (position = 1 → "3 fases")
        Map<String, Object> fullTest = parseBody(testView);
        var questions = (List<Map<String, Object>>) fullTest.get("questions");
        Long questionId = toLong(questions.get(0).get("id"));
        Long correctOptionId = ((List<Map<String, Object>>) questions.get(0).get("options"))
                .stream()
                .filter(o -> Integer.valueOf(1).equals(o.get("position")))
                .map(o -> toLong(o.get("id")))
                .findFirst().orElseThrow();

        // Candidate submits the correct answer
        mockMvc.perform(post("/api/v1/attempts/" + attemptId + "/answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + candidateToken)
                        .content(objectMapper.writeValueAsString(
                                Map.of("questionId", questionId, "optionId", correctOptionId))))
                .andExpect(status().isCreated());

        // Submit → server calculates score (1/1 = 100%)
        mockMvc.perform(post("/api/v1/attempts/" + attemptId + "/submit")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correctCount").value(1))
                .andExpect(jsonPath("$.totalCount").value(1))
                .andExpect(jsonPath("$.status").value("SUBMITTED"));

        // Skill history shows the verified result
        mockMvc.perform(get("/api/v1/users/me/skill-history")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].skill").value("acrobacia"));
    }

    @Test
    void adminEndpoint_returns403ForCandidate() throws Exception {
        mockMvc.perform(post("/api/v1/tests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + candidateToken)
                        .content(objectMapper.writeValueAsString(Map.of("skill", "acrobacia", "title", "T"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void listTests_returns401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/tests"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void recruiterSearch_returnsSubmittedCandidates() throws Exception {
        // Admin creates test and candidate completes it
        MvcResult createResult = mockMvc.perform(post("/api/v1/tests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content(objectMapper.writeValueAsString(
                                Map.of("skill", "ritmica", "title", "Recruiter Test"))))
                .andExpect(status().isCreated()).andReturn();
        Long testId = toLong(parseBody(createResult).get("id"));

        mockMvc.perform(post("/api/v1/tests/" + testId + "/questions")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + adminToken)
                .content(objectMapper.writeValueAsString(Map.of(
                        "text", "Pregunta 1", "position", 0,
                        "options", List.of(
                                Map.of("text", "Sí", "correct", true, "position", 0),
                                Map.of("text", "No", "correct", false, "position", 1))))));

        MvcResult attemptResult = mockMvc.perform(post("/api/v1/tests/" + testId + "/attempts")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isCreated()).andReturn();
        Long attemptId = toLong(parseBody(attemptResult).get("attemptId"));

        MvcResult testView = mockMvc.perform(get("/api/v1/tests/" + testId)
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk()).andReturn();
        var questions = (List<Map<String, Object>>) parseBody(testView).get("questions");
        Long questionId = toLong(questions.get(0).get("id"));
        Long optionId = toLong(((List<Map<String, Object>>) questions.get(0).get("options")).get(0).get("id"));

        mockMvc.perform(post("/api/v1/attempts/" + attemptId + "/answers")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + candidateToken)
                .content(objectMapper.writeValueAsString(
                        Map.of("questionId", questionId, "optionId", optionId))));

        mockMvc.perform(post("/api/v1/attempts/" + attemptId + "/submit")
                .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk());

        // Recruiter can discover the candidate
        mockMvc.perform(get("/api/v1/recruiter/candidates")
                        .param("skill", "ritmica")
                        .param("minScore", "0")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].skill").value("ritmica"));

        // Candidate cannot access recruiter endpoint
        mockMvc.perform(get("/api/v1/recruiter/candidates")
                        .param("skill", "ritmica")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void idor_candidateBCannotAccessCandidateAAttempt() throws Exception {
        String candidateBToken = registerAndGetToken("cand_b_" + System.nanoTime() + "@test.com");

        // Admin creates test with one question
        MvcResult createResult = mockMvc.perform(post("/api/v1/tests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content(objectMapper.writeValueAsString(
                                Map.of("skill", "tumbling", "title", "IDOR Test"))))
                .andExpect(status().isCreated()).andReturn();
        Long testId = toLong(parseBody(createResult).get("id"));

        mockMvc.perform(post("/api/v1/tests/" + testId + "/questions")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + adminToken)
                .content(objectMapper.writeValueAsString(Map.of(
                        "text", "Pregunta IDOR", "position", 0,
                        "options", List.of(
                                Map.of("text", "A", "correct", true, "position", 0),
                                Map.of("text", "B", "correct", false, "position", 1))))));

        // Candidate A starts attempt
        MvcResult attemptResult = mockMvc.perform(post("/api/v1/tests/" + testId + "/attempts")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isCreated()).andReturn();
        Long attemptId = toLong(parseBody(attemptResult).get("attemptId"));

        // Get option ID to send a valid body
        MvcResult testView = mockMvc.perform(get("/api/v1/tests/" + testId)
                        .header("Authorization", "Bearer " + candidateBToken))
                .andExpect(status().isOk()).andReturn();
        var questions = (List<Map<String, Object>>) parseBody(testView).get("questions");
        Long questionId = toLong(questions.get(0).get("id"));
        Long optionId = toLong(((List<Map<String, Object>>) questions.get(0).get("options")).get(0).get("id"));

        // Candidate B tries to submit an answer to Candidate A's attempt → 404
        mockMvc.perform(post("/api/v1/attempts/" + attemptId + "/answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + candidateBToken)
                        .content(objectMapper.writeValueAsString(
                                Map.of("questionId", questionId, "optionId", optionId))))
                .andExpect(status().isNotFound());

        // Candidate B tries to submit Candidate A's attempt → 404
        mockMvc.perform(post("/api/v1/attempts/" + attemptId + "/submit")
                        .header("Authorization", "Bearer " + candidateBToken))
                .andExpect(status().isNotFound());
    }

    // --- helpers ---

    private String registerAndGetToken(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", email, "password", "secret123"))))
                .andExpect(status().isCreated())
                .andReturn();
        return (String) parseBody(result).get("token");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseBody(MvcResult result) throws Exception {
        return objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<Map<String, Object>>() {});
    }

    private Long toLong(Object val) {
        if (val instanceof Integer i) return i.longValue();
        if (val instanceof Long l) return l;
        return Long.valueOf(val.toString());
    }
}
