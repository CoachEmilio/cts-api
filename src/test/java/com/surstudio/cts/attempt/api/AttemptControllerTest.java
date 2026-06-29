package com.surstudio.cts.attempt.api;

import com.surstudio.cts.attempt.application.AttemptService;
import com.surstudio.cts.attempt.domain.AttemptStatus;
import com.surstudio.cts.attempt.dto.*;
import com.surstudio.cts.common.ConflictException;
import com.surstudio.cts.common.ResourceNotFoundException;
import com.surstudio.cts.common.security.JwtService;
import com.surstudio.cts.common.security.SecurityConfig;
import com.surstudio.cts.common.security.UserDetailsConfig;
import com.surstudio.cts.identity.domain.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AttemptController.class)
@Import({SecurityConfig.class, UserDetailsConfig.class})
class AttemptControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean AttemptService service;
    @MockBean JwtService jwtService;
    @MockBean UserRepository userRepository;

    @Test
    @WithMockUser(roles = "CANDIDATE")
    void startAttempt_returns201() throws Exception {
        var response = new StartAttemptResponse(10L, 1L, AttemptStatus.IN_PROGRESS, Instant.now());
        when(service.startAttempt(eq(1L), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/tests/1/attempts"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.attemptId").value(10))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    void startAttempt_returns404WhenTestNotFound() throws Exception {
        when(service.startAttempt(eq(99L), any())).thenThrow(new ResourceNotFoundException("SkillTest not found: 99"));

        mockMvc.perform(post("/api/v1/tests/99/attempts"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    void startAttempt_returns409WhenTestInactive() throws Exception {
        when(service.startAttempt(eq(1L), any())).thenThrow(new ConflictException("Test 1 is not active"));

        mockMvc.perform(post("/api/v1/tests/1/attempts"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    void submitAnswer_returns201() throws Exception {
        var response = new SubmitAnswerResponse(100L, 4L, 14L);
        when(service.submitAnswer(eq(10L), any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/attempts/10/answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"questionId":4,"optionId":14}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.answerId").value(100))
                .andExpect(jsonPath("$.questionId").value(4))
                .andExpect(jsonPath("$.optionId").value(14));
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    void submitAnswer_returns400WhenBodyInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/attempts/10/answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    void submitAnswer_returns409WhenAlreadySubmitted() throws Exception {
        when(service.submitAnswer(eq(10L), any(), any()))
                .thenThrow(new ConflictException("Attempt 10 is already submitted"));

        mockMvc.perform(post("/api/v1/attempts/10/answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"questionId":4,"optionId":14}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    void submitAttempt_returns200WithResult() throws Exception {
        var response = new AttemptResultResponse(10L, java.math.BigDecimal.valueOf(75.0), 3, 4, AttemptStatus.SUBMITTED, Instant.now());
        when(service.submitAttempt(eq(10L), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/attempts/10/submit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scorePct").value(75.0))
                .andExpect(jsonPath("$.correctCount").value(3))
                .andExpect(jsonPath("$.totalCount").value(4))
                .andExpect(jsonPath("$.status").value("SUBMITTED"));
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    void submitAttempt_returns409WhenAlreadySubmitted() throws Exception {
        when(service.submitAttempt(eq(10L), any()))
                .thenThrow(new ConflictException("Attempt 10 is already submitted"));

        mockMvc.perform(post("/api/v1/attempts/10/submit"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void startAttempt_returns401WithoutAuth() throws Exception {
        mockMvc.perform(post("/api/v1/tests/1/attempts"))
                .andExpect(status().isUnauthorized());
    }
}