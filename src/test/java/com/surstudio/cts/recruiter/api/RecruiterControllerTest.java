package com.surstudio.cts.recruiter.api;

import com.surstudio.cts.assessment.domain.Skill;
import com.surstudio.cts.common.security.JwtService;
import com.surstudio.cts.common.security.SecurityConfig;
import com.surstudio.cts.common.security.UserDetailsConfig;
import com.surstudio.cts.identity.domain.UserRepository;
import com.surstudio.cts.recruiter.application.RecruiterService;
import com.surstudio.cts.recruiter.dto.CandidateMatchDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecruiterController.class)
@Import({SecurityConfig.class, UserDetailsConfig.class})
class RecruiterControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean RecruiterService recruiterService;
    @MockBean JwtService jwtService;
    @MockBean UserRepository userRepository;

    @Test
    @WithMockUser(roles = "RECRUITER")
    void searchCandidates_returnsMatchList() throws Exception {
        var match = new CandidateMatchDto(
                42L, "María García", Skill.ACROBACIA, "Test Básico",
                new BigDecimal("85.00"), 17, 20, Instant.now());
        when(recruiterService.searchCandidates(any(), any())).thenReturn(List.of(match));

        mockMvc.perform(get("/api/v1/recruiter/candidates")
                        .param("skill", "acrobacia")
                        .param("minScore", "70"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(42))
                .andExpect(jsonPath("$[0].displayName").value("María García"))
                .andExpect(jsonPath("$[0].skill").value("acrobacia"))
                .andExpect(jsonPath("$[0].scorePct").value(85.00));
    }

    @Test
    @WithMockUser(roles = "RECRUITER")
    void searchCandidates_caseInsensitiveSkill_accepted() throws Exception {
        when(recruiterService.searchCandidates(any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/recruiter/candidates")
                        .param("skill", "ACROBACIA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = "RECRUITER")
    void searchCandidates_invalidSkill_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/recruiter/candidates")
                        .param("skill", "INVALID_SKILL"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value(
                        org.hamcrest.Matchers.containsString("Invalid skill 'INVALID_SKILL'")));
    }

    @Test
    @WithMockUser(roles = "RECRUITER")
    void searchCandidates_defaultMinScore_returnsAll() throws Exception {
        when(recruiterService.searchCandidates(any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/recruiter/candidates").param("skill", "acrobacia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    void searchCandidates_forbiddenForCandidate() throws Exception {
        mockMvc.perform(get("/api/v1/recruiter/candidates").param("skill", "acrobacia"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchCandidates_forbiddenForAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/recruiter/candidates").param("skill", "acrobacia"))
                .andExpect(status().isForbidden());
    }

    @Test
    void searchCandidates_returns401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/recruiter/candidates").param("skill", "acrobacia"))
                .andExpect(status().isUnauthorized());
    }
}