package com.surstudio.cts.assessment.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.surstudio.cts.assessment.application.SkillTestService;
import com.surstudio.cts.assessment.domain.Skill;
import com.surstudio.cts.assessment.dto.*;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SkillTestController.class)
@Import({SecurityConfig.class, UserDetailsConfig.class})
class SkillTestControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean SkillTestService service;
    @MockBean JwtService jwtService;
    @MockBean UserRepository userRepository;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createTest_returns201WithBody() throws Exception {
        var response = new SkillTestAdminResponse(1L, Skill.ACROBACIA, "Test Básico", true, 10, List.of());
        when(service.createTest(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/tests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"skill":"acrobacia","title":"Test Básico"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.skill").value("acrobacia"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.questions").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createTest_returns400WhenSkillInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/tests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"skill":"INVALID_SKILL","title":"Test"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value(
                        org.hamcrest.Matchers.containsString("Invalid skill 'INVALID_SKILL'")));

        verifyNoInteractions(service);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createTest_caseInsensitiveSkill_accepted() throws Exception {
        var response = new SkillTestAdminResponse(1L, Skill.RITMICA, "Test Rítmica", true, 10, List.of());
        when(service.createTest(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/tests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"skill":"Ritmica","title":"Test Rítmica"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.skill").value("ritmica"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createTest_returns400WhenBodyMissesRequiredFields() throws Exception {
        mockMvc.perform(post("/api/v1/tests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    void getTest_returns200WithCandidateView() throws Exception {
        var optionDto = new SkillTestCandidateView.OptionDto(1L, "Impulso, vuelo y aterrizaje", 0);
        var questionDto = new SkillTestCandidateView.QuestionDto(1L, "¿Cuántas fases?", 0, List.of(optionDto));
        var view = new SkillTestCandidateView(1L, Skill.ACROBACIA, "Test", 10, false, List.of(questionDto));
        when(service.getTestForCandidate(eq(1L), any())).thenReturn(view);

        mockMvc.perform(get("/api/v1/tests/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.questions[0].options[0].text").value("Impulso, vuelo y aterrizaje"));
    }

    // Trust boundary: the HTTP response for a candidate must never contain "correct".
    @Test
    @WithMockUser(roles = "CANDIDATE")
    void getTest_responseBodyNeverContainsCorrectField() throws Exception {
        var optionDto = new SkillTestCandidateView.OptionDto(1L, "Option A", 0);
        var questionDto = new SkillTestCandidateView.QuestionDto(1L, "Q?", 0, List.of(optionDto));
        var view = new SkillTestCandidateView(1L, Skill.ACROBACIA, "T", 10, false, List.of(questionDto));
        when(service.getTestForCandidate(eq(1L), any())).thenReturn(view);

        var body = mockMvc.perform(get("/api/v1/tests/1"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(body).doesNotContain("correct");
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    void getTest_returns404AsProblemDetail() throws Exception {
        when(service.getTestForCandidate(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("SkillTest not found: 99"));

        mockMvc.perform(get("/api/v1/tests/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("SkillTest not found: 99"));
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    void listTests_returns200WithArray() throws Exception {
        when(service.listActiveTests(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/tests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateTest_returns200WithUpdatedBody() throws Exception {
        var response = new SkillTestAdminResponse(1L, Skill.RITMICA, "Updated Title", true, 10, List.of());
        when(service.updateTest(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/api/v1/tests/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"skill":"ritmica","title":"Updated Title"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.skill").value("ritmica"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteQuestion_returns204() throws Exception {
        doNothing().when(service).deleteQuestion(1L);

        mockMvc.perform(delete("/api/v1/questions/1"))
                .andExpect(status().isNoContent());

        verify(service).deleteQuestion(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteQuestion_returns404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Question not found: 99"))
                .when(service).deleteQuestion(99L);

        mockMvc.perform(delete("/api/v1/questions/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
