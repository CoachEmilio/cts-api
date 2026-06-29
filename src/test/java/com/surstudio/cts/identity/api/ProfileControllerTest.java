package com.surstudio.cts.identity.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.surstudio.cts.assessment.domain.Skill;
import com.surstudio.cts.common.security.JwtService;
import com.surstudio.cts.common.security.SecurityConfig;
import com.surstudio.cts.common.security.UserDetailsConfig;
import com.surstudio.cts.identity.application.ProfileService;
import com.surstudio.cts.identity.domain.UserRepository;
import com.surstudio.cts.identity.dto.ProfileResponse;
import com.surstudio.cts.identity.dto.SkillHistoryEntry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfileController.class)
@Import({SecurityConfig.class, UserDetailsConfig.class})
class ProfileControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean ProfileService profileService;
    @MockBean JwtService jwtService;
    @MockBean UserRepository userRepository;

    @Test
    @WithMockUser(roles = "CANDIDATE")
    void getProfile_returns200() throws Exception {
        var response = new ProfileResponse(1L, "coach@test.com", "Coach Name", "Bio here", Instant.now());
        when(profileService.getProfile(any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/users/me/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("coach@test.com"))
                .andExpect(jsonPath("$.displayName").value("Coach Name"));
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    void upsertProfile_returns200() throws Exception {
        var response = new ProfileResponse(1L, "coach@test.com", "Updated Name", "Updated bio", Instant.now());
        when(profileService.upsertProfile(any(), any())).thenReturn(response);

        mockMvc.perform(put("/api/v1/users/me/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"displayName":"Updated Name","bio":"Updated bio"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Updated Name"));
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    void upsertProfile_returns400WhenDisplayNameTooLong() throws Exception {
        mockMvc.perform(put("/api/v1/users/me/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"" + "x".repeat(101) + "\",\"bio\":\"bio\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    void getSkillHistory_returns200WithList() throws Exception {
        var entry = new SkillHistoryEntry(10L, Skill.ACROBACIA, "Test Básico",
                BigDecimal.valueOf(75.0), 3, 4, Instant.now());
        when(profileService.getSkillHistory(any())).thenReturn(List.of(entry));

        mockMvc.perform(get("/api/v1/users/me/skill-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].attemptId").value(10))
                .andExpect(jsonPath("$[0].skill").value("acrobacia"))
                .andExpect(jsonPath("$[0].scorePct").value(75.0));
    }

    @Test
    void getProfile_returns401WithoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/users/me/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getProfile_returns403ForAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/users/me/profile"))
                .andExpect(status().isForbidden());
    }
}